package com.bct.gpstracker.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.*;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;

/**
 * Created by HH
 * Date: 2015/7/17 0017
 * Time: 下午 5:26
 */
public class TrackerReceiver extends BroadcastReceiver {
    private MediaPlayer mediaPlayer=null;
    private Boolean isPlaying = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_NEW_MSG.equals(intent.getAction())) {
            long id = intent.getLongExtra("id", 0);
            int type = intent.getIntExtra("type", 0);
            if (id == 0) {
                return;
            }
            switch (type) {
                case 99:
                    ChatActivity activity = ChatActivity.getChatActivity();
                    if (activity != null) {
                        ChatActivity.ChatHandler handler = activity.getHandler();
                        Message cmsg = Message.obtain(handler, activity.UPDATE_CHAT_FAILED);
                        cmsg.obj = id;
                        handler.sendMessage(cmsg);
                    }
                    break;
                case 1:
                    AppContext.newChatMsgIds.add(id);
                    showNotify(context, id);
                    mediaPlayerSoundAndVibrate(context);
                    break;
            }
        }
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (Utils.isNetworkConnected(context) && CommunicationService.get() != null) {
                CommunicationService.get().connect();
            }
        }
    }

    private static void showNotify(Context context, Long id) {
        try {
            ChatMsg chatMsg = AppContext.db.findById(ChatMsg.class, id);
            AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_UNREAD_DATA);
            ManaRelation abstractUser = Session.getInstance().getAbstractUser(chatMsg.getImei());
            String title = null;
            if (abstractUser != null) {
                if (abstractUser instanceof Device) {
                    Device device = (Device) abstractUser;
                    title = CommUtil.toStr(device.getName());
                } else if (abstractUser instanceof Keeper) {
                    Keeper keeper = (Keeper) abstractUser;
                    title = CommUtil.toStr(keeper.getNickName());
                }

            }
            if (CommUtil.isBlank(title)) {
                title = context.getString(R.string.stranger);
            }
            String cont = Utils.getPrettyDescribe(context, ContType.getType(chatMsg.getType()), chatMsg.getContent(), 20);

            Intent intent = new Intent(context, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(Constants.REFRESH_FLAG, true);
            MapEntity mEntity = Session.getInstance().getMapEntityByImei(chatMsg.getImei());
            intent.putExtra("chat", mEntity);
            intent.putExtra("id",id);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            Notification.Builder builder = new Notification.Builder(context).setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis()).setContentTitle(title).setContentText(cont)
                    .setContentIntent(pendingIntent).setLights(0xffff0000, 1000, 2000);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = builder.getNotification();
            manager.notify(Constants.NOTIFIACTION_ID + 99, notification);

            ChatActivity activity = ChatActivity.getChatActivity();
            if (activity != null) {
                ChatActivity.ChatHandler handler = activity.getHandler();
                Message cmsg = Message.obtain(handler, activity.UPDATE_CHAT_UI);
                cmsg.obj = intent;
                handler.sendMessage(cmsg);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "保存消息失败", e);
        }
    }

    public static void clearNotification(int type) {
        NotificationManager manager = (NotificationManager) AppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Constants.NOTIFIACTION_ID + type);
    }

    private void mediaPlayerSoundAndVibrate(Context context) {
        if (!isPlaying) {
            isPlaying = true;
            if (Utils.getPreferences(context).getBoolean(Constants.MSG_VOICE, true)) {
                //获取系统默认铃声的Uri
                Uri actualDefaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(context, actualDefaultRingtoneUri);
                    mediaPlayer.setLooping(false);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            mediaPlayer.release();
                            mediaPlayer = null;
                            isPlaying = false;
                        }
                    });
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (Utils.getPreferences(context).getBoolean(Constants.MSG_VIBRATE, true)) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                //设置震动时长，停50毫秒，震动100毫秒，震动两次
                vibrator.vibrate(new long[]{100, 300, 100, 300}, -1);
            }
        }
    }
}
