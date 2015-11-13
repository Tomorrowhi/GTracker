package com.bct.gpstracker.baby.activity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.dialog.VolumeAdjustmentDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.activity.AlarmActivity;
import com.bct.gpstracker.my.activity.DSPManagerActivity;
import com.bct.gpstracker.my.activity.DeviceRestActivity;
import com.bct.gpstracker.my.activity.FenceListActivity;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Cmd;
import com.bct.gpstracker.vo.CmdType;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/8/27 0027.
 * 第一页功能
 */
public class ChatTerMianlAddOneFragment extends Fragment implements View.OnClickListener {
    private final static int OPEN_GALLERY = 799;
    @ViewInject(R.id.add_camera)
    private LinearLayout addCamera;

    @ViewInject(R.id.add_confirm_sos)
    private LinearLayout confirmSOS;

    @ViewInject(R.id.add_delete_sos)
    private LinearLayout deleteSOS;

    @ViewInject(R.id.add_photo)
    private LinearLayout addPhoto;
    @ViewInject(R.id.add_phone)
    private LinearLayout addCallPhone;
    @ViewInject(R.id.sound_recording)
    private LinearLayout soundRecording;
    @ViewInject(R.id.add_child_story)
    private LinearLayout addChildStory;
    @ViewInject(R.id.add_safe_zone)
    private LinearLayout addSafeZone;
    @ViewInject(R.id.add_live_assistant)
    private LinearLayout addLiveAssistant;
    @ViewInject(R.id.add_dormancy_periods)
    private LinearLayout addDormancyPeriods;
    @ViewInject(R.id.bluetooth_against_losing)
    private LinearLayout bluetoothAgainstLosing;
    @ViewInject(R.id.add_audio_control)
    private LinearLayout addAudioControl;
    @ViewInject(R.id.add_phone_bill)
    private LinearLayout phoneBill;

    private View view;
    private Context mContext;
    private ChatAddOneFragmentCallBack mChatAddOneFragmentCallBack;
    private Intent intent;
    private Map<String, Long> countTime = new HashMap<>();
    private String imei = ChatActivity.mEntityImei;
    private VolumeAdjustmentDialog volumeAdjustmentDialog; //调整音量的对话框


    //创建Fragment对象
    public static ChatTerMianlAddOneFragment newInstance() {
        ChatTerMianlAddOneFragment newFragment = new ChatTerMianlAddOneFragment();
        return newFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        countTime.put("confirm_sos", 0L);
        countTime.put("sound_recording", 0L);
        countTime.put("delete_sos", 0L);
        countTime.put("phone_bill", 0L);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ChatAddOneFragmentCallBack)) {
            throw new IllegalStateException("ChatAddOneFragment所在的Activity必须实现ChatAddOneFragmentCallBack接口");
        }
        mChatAddOneFragmentCallBack = (ChatAddOneFragmentCallBack) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_one, container, false);
        ViewUtils.inject(this, view);
        volumeAdjustmentDialog = VolumeAdjustmentDialog.getInstance();
        initEvent();
        return view;
    }

    @Override
    public void onStop() {
        Log.e("TGA", "OneFragment——Stop");
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChatAddOneFragmentCallBack = null;
    }

    /**
     * Avtivity回调接口
     */
    public interface ChatAddOneFragmentCallBack {
        void selectData(Intent data);
    }

    private void initEvent() {
        addCamera.setOnClickListener(this);
        deleteSOS.setOnClickListener(this);
        addPhoto.setOnClickListener(this);
        addCallPhone.setOnClickListener(this);
        soundRecording.setOnClickListener(this);
        addChildStory.setOnClickListener(this);
        addSafeZone.setOnClickListener(this);
        addLiveAssistant.setOnClickListener(this);
        confirmSOS.setOnClickListener(this);
        addDormancyPeriods.setOnClickListener(this);
        bluetoothAgainstLosing.setOnClickListener(this);
        addAudioControl.setOnClickListener(this);
        phoneBill.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Session.getInstance().setSetupDevice(ChatActivity.device);
        switch (v.getId()) {
            case R.id.bluetooth_against_losing:
                //蓝牙防丢
                startActivity(new Intent(mContext, BluetoothActivity.class));
                break;
            case R.id.add_audio_control:
                //音量调整
                volumeAdjustmentDialog.showDialog(mContext, true);
                //int voice = Utils.getPreferences(mContext).getInt(Constants.VOLUME,0);
//                chatMsg.setContent(getString(R.string.setup_auio_control) + " " + getString(R.string.send_success));
//                chatMsg.setIcon(R.drawable.add_audio_control);
//                chatMsg.setTitle(getString(R.string.sys_msg));
                //AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                break;
            case R.id.add_phone_bill:
                //终端查询话费
                if ((System.currentTimeMillis() - countTime.get("phone_bill")) > Constants.REQUEST_INTERVAL) {
                    sendCommand(MyConstants.TERM_FEE, R.string.chat_select_phone_bill, R.drawable.add_phone_bill);
                    countTime.put("phone_bill", System.currentTimeMillis());
                } else {
                    Toast.makeText(mContext, R.string.foot_menu_prompt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_confirm_sos:
                //确认SOS
                if ((System.currentTimeMillis() - countTime.get("confirm_sos")) > Constants.REQUEST_INTERVAL) {
                    confirmSOS();
                } else {
                    Toast.makeText(mContext, R.string.foot_menu_prompt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_phone:
                //打电话
                String phoneNumber = ChatActivity.device.getPhone();
                if (!Utils.isMobileNO(phoneNumber)) {
                    Toast.makeText(mContext, "手表电话号码有误", Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
                startActivity(phoneIntent);
                break;
            case R.id.sound_recording:
                //录音
                if ((System.currentTimeMillis() - countTime.get("sound_recording")) > Constants.REQUEST_INTERVAL) {
                    SoundRecordingCommand();
                } else {
                    Toast.makeText(mContext, R.string.foot_menu_prompt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_child_story:
                //儿歌/故事
                intent = new Intent(mContext, DSPManagerActivity.class);
                intent.putExtra("IMEI", ChatActivity.mEntityImei);
                startActivity(intent);
                break;
            case R.id.add_safe_zone:
                //安全区域
                intent = new Intent(mContext, FenceListActivity.class);
                startActivity(intent);
                break;
            case R.id.add_live_assistant:
                //生活助手
                intent = new Intent(mContext, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.add_photo:
                //选择图片
                intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, OPEN_GALLERY);
                break;
            case R.id.add_delete_sos:
                //清除SOS
                if ((System.currentTimeMillis() - countTime.get("delete_sos")) > Constants.REQUEST_INTERVAL) {
                    deleteSOS();
                } else {
                    Toast.makeText(mContext, R.string.foot_menu_prompt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_camera:
                //启动相机
                Toast.makeText(mContext, "拍照", Toast.LENGTH_SHORT).show();
//                intent = new Intent();
//                // 指定开启系统相机的Action
//                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivityForResult(intent, 1);
                break;
            case R.id.add_dormancy_periods:
                //免打扰模式
                Intent intent = new Intent(mContext, DeviceRestActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void confirmSOS() {
        BctClientCallback callback=new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(view,true,false);
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                Context context=AppContext.getContext();
                ChatMsg chatMsg = createSysChatMsg(imei);
                chatMsg.setContent(context.getString(R.string.chat_select_confirm_sos) + " " + context.getString(R.string.send_success));
                chatMsg.setIcon(R.drawable.add_confirm_sos);
                chatMsg.setTitle(getString(R.string.sys_msg));
                AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                countTime.put("confirm_sos", System.currentTimeMillis());
                saveChatMsg(chatMsg);
            }

            @Override
            public void onFailure(String message) {
                CommUtil.hideProcessing();
                if(CommUtil.isNotBlank(message)){
                    CommUtil.sendMsg(CommHandler.TOAST_SHORT, message);
                }
            }
        };
        Cmd cmd = new Cmd(CmdType.YS.getType(), null, imei,callback);
        CommUtil.sendMsg(CommHandler.SEND_COMMAND, cmd);
    }

    /**
     * 保存聊天信息到数据库
     */
    public void saveChatMsg(ChatMsg chatMsg) {
        try {
            AppContext.db.saveBindingId(chatMsg);
        } catch (Exception e) {
            Log.e(Constants.TAG, "保存聊天信息到数据库失败！", e);
        }
    }

    /**
     * 清除SOS
     */
    private void deleteSOS() {
        String cmd = "ns";
        CommService.get().sendCommand(mContext, imei, cmd, null, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(view,true,false);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                try {
                    CommUtil.hideProcessing();
                    Context context=AppContext.getContext();
                    ChatMsg chatMsg = createSysChatMsg(imei);
                    chatMsg.setContent(context.getString(R.string.chat_select_delete_sos) + " " + context.getString(R.string.send_success));
//                chatMsg.setIcon(R.drawable.add_delete_sos);
//                chatMsg.setTitle(getString(R.string.sys_msg));
                    AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                    countTime.put("delete_sos", System.currentTimeMillis());
                    saveChatMsg(chatMsg);
                } catch (Exception e) {
                    Log.e(Constants.TAG,null,e);
                }
            }

            @Override
            public void onFailure(String message) {
                try {
                    CommUtil.hideProcessing();
                    Context context=AppContext.getContext();
                    CommUtil.sendMsg(CommHandler.TOAST_SHORT, String.format(context.getString(R.string.failed), context.getString(R.string.menu_clear_str)));
                    ChatMsg chatMsg = createSysChatMsg(imei);
                    chatMsg.setContent(context.getString(R.string.chat_select_delete_sos) + " " + context.getString(R.string.send_fail));
//                chatMsg.setIcon(R.drawable.add_delete_sos);
//                chatMsg.setTitle(getString(R.string.sys_msg));
                    AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                    countTime.put("delete_sos", 0L);
                    saveChatMsg(chatMsg);
                } catch (Exception e) {
                    Log.e(Constants.TAG, null, e);
                }
            }
        });
    }

    /**
     * 录音指令
     */
    private void SoundRecordingCommand() {
        SoundRecording(ChatActivity.mEntityImei, 15, new BctClientCallback() {

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) throws Exception{
                if (obj.getRetcode() == 1) {
                    //Toast.makeText(mContext, R.string.send_sound_record_success, Toast.LENGTH_SHORT).show();
                    ChatMsg chatMsg = createSysChatMsg(ChatActivity.mEntityImei);
                    chatMsg.setContent(getString(R.string.sound_recording) + " " + getString(R.string.send_success));
//                    chatMsg.setIcon(R.drawable.record_animate_07);
//                    chatMsg.setTitle(getString(R.string.sys_msg));
                    AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_DISPLAYMSG);
                    countTime.put("sound_recording", System.currentTimeMillis());
                    try {
                        AppContext.db.saveBindingId(chatMsg);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "保存聊天信息到数据库失败！", e);
                    }
                } else {
                    Toast.makeText(mContext, R.string.send_sound_record_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                countTime.put("sound_recording", 0L);
            }
        });
    }

    /**
     * 发送录音命令
     */
    private void SoundRecording(String imeis, int lengthTime, final BctClientCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("imeis", imeis);
            data.put("rc", lengthTime);
            //使用异步请求链接对象
            BctClient.getInstance().POST(getActivity(), CommonRestPath.RecordSound(), data, new JsonHttpResponseHelper(callback).getHandler());
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mChatAddOneFragmentCallBack.selectData(data);
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
//                    chatMsg.setIcon(icon);
//                    chatMsg.setTitle(getString(R.string.sys_msg));
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
