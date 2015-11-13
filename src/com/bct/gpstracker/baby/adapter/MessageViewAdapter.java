package com.bct.gpstracker.baby.adapter;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.msg.MsgMainFragment;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.ui.ImageZoomActivity;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CircleImageView;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;
import com.nostra13.universalimageloader.core.ImageLoader;
import pl.droidsonroids.gif.GifImageView;

public class MessageViewAdapter extends BaseAdapter {
    private List<ChatMsg> messages;
    private ChatActivity activity;
    private LayoutInflater mInflater;
    private String mPortrait;
    private Pattern emojiPattern = Pattern.compile(Constants.REGX_EMOJI);
    private PopupWindow menuWindow;
    private TextView delMsg;

    public MessageViewAdapter(ChatActivity activity, List<ChatMsg> list, String portrait) {
        this.activity = activity;
        this.messages = list;
        this.mPortrait = portrait;
        mInflater = LayoutInflater.from(activity);
    }

    private interface MsgType {
        int MSG_COMM_LEFT = 0, MSG_COMM_RIGHT = 1, MSG_SYS_LEFT = 2, MSG_SYS_RIGHT = 3;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMsg chatMsg = messages.get(position);
        ContType tp = ContType.getType(chatMsg.getType());
        boolean isSend = chatMsg.getIsSend();
        if (ContType.MSG_SYS == tp && isSend) {
            return MsgType.MSG_SYS_RIGHT;
        } else if (ContType.isSysType(tp)) {
            return MsgType.MSG_SYS_LEFT;
        } else if (isSend) {
            return MsgType.MSG_COMM_RIGHT;
        } else {
            return MsgType.MSG_COMM_LEFT;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == MsgType.MSG_SYS_RIGHT || getItemViewType(position) == MsgType.MSG_SYS_LEFT) {
            convertView = getSysMsgView(position, convertView);
        } else {
            convertView = getCommonMsgView(position, convertView);
        }
        return convertView;
    }

    private View getSysMsgView(int position, View convertView) {
        ChatMsg chatMsg = messages.get(position);
        MsgSysViewHolder viewHolder;
        if (convertView == null) {
            if (chatMsg.getIsSend()) {
                convertView = mInflater.inflate(R.layout.message_sys_item_right, null);
            } else {
                convertView = mInflater.inflate(R.layout.message_sys_item_left, null);
            }
            viewHolder = new MsgSysViewHolder();
            viewHolder.msgSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.msgTitle = (TextView) convertView.findViewById(R.id.msg_title);
            viewHolder.msgContent = (TextView) convertView.findViewById(R.id.msg_content);
            viewHolder.msgMainContent = (TextView) convertView.findViewById(R.id.msg_main_content);
            viewHolder.msgIcon = (ImageView) convertView.findViewById(R.id.msg_icon);
            viewHolder.photoView = (CircleImageView) convertView.findViewById(R.id.iv_userhead);
            viewHolder.multipartLayout = (LinearLayout) convertView.findViewById(R.id.multipart);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MsgSysViewHolder) convertView.getTag();
        }
        viewHolder.multipartLayout.setVisibility(View.GONE);
        viewHolder.msgContent.setVisibility(View.VISIBLE);

        if(ContType.SOS_ALERT.getType() == chatMsg.getType()){
            viewHolder.multipartLayout.setVisibility(View.VISIBLE);
            viewHolder.msgContent.setVisibility(View.GONE);

            viewHolder.msgTitle.setText(R.string.latest_position);
            viewHolder.msgIcon.setImageResource(R.drawable.map_icon);
            viewHolder.msgMainContent.setText(chatMsg.getContent());
        }

        viewHolder.msgSendTime.setText(CommUtil.getDateTime(new Date(chatMsg.getTime())));
        viewHolder.msgContent.setText(chatMsg.getContent());
        return convertView;
    }

    private View getCommonMsgView(int position, View convertView) {
        ChatMsg chatMsg = messages.get(position);
        CommViewHolder viewHolder;
        if (convertView == null) {
            if (chatMsg.getIsSend()) {
                convertView = mInflater.inflate(R.layout.message_item_right, null);
            } else {
                convertView = mInflater.inflate(R.layout.message_item_left, null);
            }
            viewHolder = new CommViewHolder();
            viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.photoView = (CircleImageView) convertView.findViewById(R.id.iv_userhead);
            viewHolder.playView = (ImageView) convertView.findViewById(R.id.playIV);
            viewHolder.loadingText = (TextView) convertView.findViewById(R.id.message_htv_loading_text);
            viewHolder.picIV = (ImageView) convertView.findViewById(R.id.picIV);
            viewHolder.msgFailed = (ImageView) convertView.findViewById(R.id.msg_failed);
            viewHolder.contentLayout = (LinearLayout) convertView.findViewById(R.id.contentLayout);
            viewHolder.fileFrame = (FrameLayout) convertView.findViewById(R.id.file_frame);
            viewHolder.voiceDuration = (TextView) convertView.findViewById(R.id.voice_duration);
            viewHolder.gifView = (GifImageView) convertView.findViewById(R.id.gif_View);
            viewHolder.isSendMsg = chatMsg.getIsSend();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CommViewHolder) convertView.getTag();
        }

        viewHolder.tvContent.setText(null);
        viewHolder.playView.setVisibility(View.GONE);
        viewHolder.voiceDuration.setVisibility(View.GONE);
        viewHolder.msgFailed.setVisibility(View.GONE);
        viewHolder.loadingText.setVisibility(View.GONE);
        viewHolder.fileFrame.setVisibility(View.GONE);
        viewHolder.gifView.setVisibility(View.GONE);

        viewHolder.contentLayout.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = viewHolder.contentLayout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        ContType tp = ContType.getType(chatMsg.getType());
        if (ContType.TXT == tp) {
            //文字
            String content = chatMsg.getContent();
            Matcher m = emojiPattern.matcher(content);
            if (m.matches()) {
                //gif图片
                viewHolder.contentLayout.setVisibility(View.GONE);
                viewHolder.gifView.setVisibility(View.VISIBLE);
                //优先加载新表情
                if (MsgMainFragment.mEmoticonsUri.size() != 0) {
                    Uri uri = MsgMainFragment.mEmoticonsUri.get(content);
                    if (uri == null) {
                        //普通文字消息
                        viewHolder.tvContent.setText(chatMsg.getContent());
                    } else {
                        viewHolder.gifView.setImageURI(uri);
                    }
                } else if (MainActivity.mEmoticonsId.size() != 0) {
                    Integer integer = MainActivity.mEmoticonsId.get(content);
                    if (integer == null) {
                        //普通文字消息
                        viewHolder.tvContent.setText(chatMsg.getContent());
                    } else {
                        viewHolder.gifView.setImageResource(integer);
                    }
                }
            } else {
                //普通文字消息
                viewHolder.gifView.setVisibility(View.GONE);
                viewHolder.tvContent.setText(chatMsg.getContent());
            }
        } else if (ContType.PIC == tp) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(AppContext.getContext().getFilesDir() + "/" + chatMsg.getLocalUrl(), options);

            viewHolder.picIV.setImageBitmap(bitmap);
            viewHolder.fileFrame.setVisibility(View.VISIBLE);
        } else if (ContType.AUDIO == tp) {
            viewHolder.playView.setVisibility(View.VISIBLE);
            if (chatMsg.getDuration() != null && chatMsg.getDuration() > 0) {
                viewHolder.voiceDuration.setText(chatMsg.getDuration() + "''");
                viewHolder.voiceDuration.setVisibility(View.VISIBLE);
                //根据语音时间长度显示长短不一的条
                params.width = Utils.dp2px(activity, 160 / Constants.VOICE_MAX_TIME * chatMsg.getDuration() + 40);
            }
        }
        viewHolder.contentLayout.setLayoutParams(params);

        viewHolder.tvSendTime.setText(CommUtil.getDateTime(new Date(chatMsg.getTime())));
        viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        viewHolder.contentLayout.setOnClickListener(new ContClickListener(position));
        ContLongClickListener listener=new ContLongClickListener(position);
        viewHolder.contentLayout.setOnLongClickListener(listener);
        viewHolder.gifView.setOnLongClickListener(listener);
        if (chatMsg.getIsSend()) {
            String portrait = Session.getInstance().getUser().getPortrait();
            if (!"".equals(portrait)) {
                ImageLoader.getInstance().displayImage(portrait,viewHolder.photoView);
            } else {
                viewHolder.photoView.setImageResource(R.drawable.user_no_photo);
            }
        } else {
            Device device = Session.getInstance().getDevice(chatMsg.getImei());
            if (device != null && CommUtil.isNotBlank(device.getPortrait())) {
                ImageLoader.getInstance().displayImage(device.getPortrait(),viewHolder.photoView);
            } else if (CommUtil.isNotBlank(mPortrait)) {
                ImageLoader.getInstance().displayImage(mPortrait,viewHolder.photoView);
            } else {
                viewHolder.photoView.setImageResource(R.drawable.user_no_photo);
            }
        }
        if (chatMsg.getSucc() != null && chatMsg.getSucc() == 0) {
            viewHolder.msgFailed.setVisibility(View.VISIBLE);
        }
        return convertView;
    }



    public static class CommViewHolder {
        public TextView tvSendTime;
        public TextView tvContent;
        public TextView loadingText;
        public TextView voiceDuration;
        public CircleImageView photoView;
        public boolean isSendMsg = true;
        public ImageView playView, picIV, msgFailed;
        public LinearLayout contentLayout;
        public FrameLayout fileFrame;
        public GifImageView gifView;
    }

    public static class MsgSysViewHolder {
        public TextView msgSendTime,
                msgTitle,
                msgContent,
                msgMainContent;
        public ImageView msgIcon;
        public CircleImageView photoView;
        public LinearLayout multipartLayout;
    }

    private class ContLongClickListener implements View.OnLongClickListener {
        private int position;

        public ContLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            showPopMenu(v,position);
            return false;
        }
    }

    private class ContClickListener implements View.OnClickListener {
        private int position;

        public ContClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            ChatMsg chat = messages.get(position);
            ContType tp = ContType.getType(chat.getType());
            if (CommUtil.isNotBlank(chat.getLocalUrl()) && ContType.AUDIO == tp) {
                playVoice(chat.getLocalUrl());
            } else if (ContType.PIC == tp) {
                Intent intent = new Intent(activity, ImageZoomActivity.class);
                String path = activity.getFilesDir() + "/" + chat.getLocalUrl();
                intent.putExtra(Constants.PIC_PATH, path);
                activity.startActivity(intent);
            }
        }
    }

    private void showPopMenu(View parent, final int position) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int width=dm.widthPixels;
        int height=activity.getWindow().getDecorView().getMeasuredHeight();
        if (menuWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) AppContext.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View menuView = layoutInflater.inflate(R.layout.menu_chat_msg_opt, null);
            delMsg = (TextView) menuView.findViewById(R.id.del_msg);

            menuWindow = new PopupWindow(menuView, width, height);
        }
        // 使其聚集
        menuWindow.setFocusable(true);
        // 设置允许在外点击消失
        menuWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        ColorDrawable drawable=new ColorDrawable(Color.parseColor("#33000000"));
        menuWindow.setBackgroundDrawable(drawable);
        menuWindow.showAsDropDown(activity.getWindow().getDecorView(),0,-height);
        delMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMsg chatMsg=messages.get(position);
                try {
                    AppContext.db.delete(chatMsg);
                    messages.remove(chatMsg);
                    CommUtil.showMsgShort(activity.getString(R.string.delete_success));
                } catch (Exception e) {
                    CommUtil.showMsgShort(activity.getString(R.string.delete_failed));
                }
                notifyDataSetChanged();
                if (menuWindow != null) {
                    menuWindow.dismiss();
                }
            }
        });
    }


    /**
     * 播放语音文件
     *
     * @param filePath
     */
    MediaPlayer mediaPlayer;

    private void playVoice(String filePath) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // System.out.println("已经在播放了");
            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(activity.getFilesDir() + "/" + filePath);
//				    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
//                if (viewHolder!=null)
//                viewHolder.playView.setBackgroundColor(Color.BLACK);
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        if (viewHolder != null)
//                            viewHolder.contentLayout.setSelected(false);
//                    }
//                });
//				 mediaPlayer.stop();  
//				 mediaPlayer.release();  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示进度
     *
     * @param view
     * @param index
     * @param progress
     */
    public synchronized void updateProgress(View view, int index, double progress) {
        ChatMsg chatMsg;
        if (view == null || index < 0 || (chatMsg = messages.get(index)) == null) {
            return;
        }
        CommViewHolder viewHolder = (CommViewHolder) view.getTag();
        if (viewHolder == null) {
            return;
        }

        ContType tp = ContType.getType(chatMsg.getType());
        if (ContType.AUDIO != tp && ContType.TXT != tp && progress != -1d) {
            viewHolder.loadingText.setVisibility(View.VISIBLE);
            viewHolder.loadingText.setText(((int) progress) + "%");
        } else {
            viewHolder.loadingText.setVisibility(View.GONE);
        }
    }
}
