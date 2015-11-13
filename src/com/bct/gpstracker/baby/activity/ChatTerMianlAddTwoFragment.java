package com.bct.gpstracker.baby.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.activity.AlarmActivity;
import com.bct.gpstracker.my.activity.TerminalEmojiSet;
import com.bct.gpstracker.my.activity.WifiActivity;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/8/27 0027.
 *
 */
public class ChatTerMianlAddTwoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ChatTerMianlAddTwoFragment.class.getSimpleName();

    @ViewInject(R.id.add_overstep_police)
    private LinearLayout addOverstepPolice;
    @ViewInject(R.id.add_low_power)
    private LinearLayout addLowPower;
    @ViewInject(R.id.add_home_wifi)
    private LinearLayout addHomeWifi;
    @ViewInject(R.id.add_remote_shutdown)
    private LinearLayout addRemoteShutdown;
    @ViewInject(R.id.add_alarm)
    private LinearLayout addAlarm;
    @ViewInject(R.id.add_set_terminal_emoji)
    private LinearLayout addSetTerminalEmoji;

    private View view;
    private Context mContext;
    private Intent intentMsg;
    private Map<String, Long> countTime = new HashMap<>();


    //创建Fragment对象
    public static ChatTerMianlAddTwoFragment newInstance() {
        ChatTerMianlAddTwoFragment newFragment = new ChatTerMianlAddTwoFragment();
        return newFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext =getActivity();

        countTime.put("remote_shutdown", 0L);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_two, container, false);
        ViewUtils.inject(this, view);
        initEvent();
        return view;
    }

    private void initEvent() {
        addHomeWifi.setOnClickListener(this);
        addRemoteShutdown.setOnClickListener(this);
        addOverstepPolice.setOnClickListener(this);
        addLowPower.setOnClickListener(this);
        addAlarm.setOnClickListener(this);
        addSetTerminalEmoji.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        Session.getInstance().setSetupDevice(ChatActivity.device);
        switch (v.getId()) {
            case R.id.add_set_terminal_emoji:
                //设置终端表情
                startActivity(new Intent(mContext,TerminalEmojiSet.class));
                break;
            case R.id.add_overstep_police:
                //越界报警
               Toast.makeText(mContext,"越界报警",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_low_power:
                //低电报警
                Toast.makeText(mContext,"低电报警",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add_home_wifi:
                //家庭WIFI
                intentMsg = new Intent(mContext, WifiActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.add_remote_shutdown:
                //远程关机
                if ((System.currentTimeMillis() - countTime.get("remote_shutdown")) > Constants.REQUEST_INTERVAL) {
                    sendCommand(MyConstants.REMOTE_CLOSE, R.string.setup_remote_close, R.drawable.power_layout_iv);
                    countTime.put("remote_shutdown", System.currentTimeMillis());
                } else {
                    Toast.makeText(mContext, R.string.foot_menu_prompt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_alarm:
                //家庭闹钟
                startActivity(new Intent(mContext, AlarmActivity.class));
                break;
        }
    }

    private void sendCommand(String type, final int optionContent, final int icon) {
        String cmd;
        String content;
        switch (type) {
            case MyConstants.REMOTE_CLOSE:
                cmd = "of";
                content = "1";
                break;
            case MyConstants.ELEC_SAVE_SWITCH:
                cmd = "po";
                content = "0";
                break;
            case MyConstants.TERM_FEE:
                cmd = "hf";
                content = "";
                break;
            default:
                return;
        }
        CommService.get().sendCommand(mContext, Session.getInstance().getSetupDevice().getImei(), cmd, content, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(view,false,true);
            }

            @Override
            public void onFinish() {
//				WizardAlertDialog.getInstance().closeProgressDialog();
                //CustomProgressDialog.getInstance(getActivity()).closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
                    ChatMsg chatMsg = createSysChatMsg(ChatActivity.mEntityImei);
                    chatMsg.setContent(getString(optionContent) + " " + getString(R.string.send_success));
                    chatMsg.setIcon(icon);
                    chatMsg.setTitle(getString(R.string.sys_msg));
                    AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                    //Toast.makeText(mContext, R.string.send_success, Toast.LENGTH_SHORT).show();
                    try {
                        AppContext.db.saveBindingId(chatMsg);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "保存聊天信息到数据库失败！", e);
                    }
                } else {
                    Toast.makeText(mContext, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                CommUtil.hideProcessing();
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });

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
