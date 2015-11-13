package com.bct.gpstracker.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Cmd;
import com.bct.gpstracker.vo.CmdType;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;

/**
 * Created by longchao on 2015/8/31 0031.
 * 音量调整的对话框
 */
public class VolumeAdjustmentDialog {

    private static final String TAG = VolumeAdjustmentDialog.class.getSimpleName();
    private static VolumeAdjustmentDialog volumeAdjustmentDialog = null;
    private AlertDialog alertDialog;


    public static synchronized VolumeAdjustmentDialog getInstance() {
        if (volumeAdjustmentDialog == null) {
            volumeAdjustmentDialog = new VolumeAdjustmentDialog();
        }
        return volumeAdjustmentDialog;
    }

    private VolumeAdjustmentDialog() {
    }


    public void showDialog(final Context context, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = View.inflate(context, R.layout.dialog_volume_adjustment, null);
        final SeekBar volSeekbar = (SeekBar) view.findViewById(R.id.vol_Seekbar);
        Button comfirmBtn = (Button) view.findViewById(R.id.comfirm_Btn);
        final TextView volTextView = (TextView) view.findViewById(R.id.vol_TV);

        int volume = Utils.getPreferences(context).getInt(Constants.VOLUME, 0);
        volSeekbar.setProgress(volume);
        volTextView.setText(volume+"");

        volSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volTextView.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        comfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cmd cmd = new Cmd(CmdType.VO.getType(),volSeekbar.getProgress()+"", ChatActivity.mEntityImei,true);
                CommUtil.sendMsg(CommHandler.SEND_COMMAND,cmd);

                ChatMsg chatMsg = createSysChatMsg(ChatActivity.mEntityImei);
                int voice = volSeekbar.getProgress();
                chatMsg.setContent(context.getString(R.string.setup_auio_control) + " " + context.getString(R.string.send_success)+" "+context.getString(R.string.voice_setting)+voice+context.getString(R.string.ji));
//                chatMsg.setIcon(R.drawable.remote_control);
//                chatMsg.setTitle(context.getString(R.string.sys_msg));
                AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                try {
                    AppContext.db.saveBindingId(chatMsg);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "保存聊天信息到数据库失败！", e);
                }

                //需要返回一个结果
                SharedPreferences preferences = Utils.getPreferences(context);
                preferences.edit().putInt(Constants.VOLUME, voice).apply();
                alertDialog.dismiss();
                alertDialog.cancel();
                alertDialog = null;
            }
        });
        builder.setTitle(context.getResources().getString(R.string.volume_adjust));
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setCancelable(cancelable);
        alertDialog.show();
    }

    private ChatMsg createSysChatMsg(String imei) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setIsSend(false);
        chatMsg.setImei(imei);
        chatMsg.setType(ContType.MSG_SYS.getType());
        chatMsg.setTermType(Session.getInstance().getMapEntityByImei(imei).getTermType().getType());
        chatMsg.setUserId(Session.getInstance().getLoginedUserId());
        chatMsg.setTime(System.currentTimeMillis());
        chatMsg.setSucc(1);
        return chatMsg;
    }




}
