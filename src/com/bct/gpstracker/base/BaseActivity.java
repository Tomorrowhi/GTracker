package com.bct.gpstracker.base;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.simple.eventbus.Subscriber;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.VibrateDialog;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.server.MessageSender;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.service.ExceptionCatchService;
import com.bct.gpstracker.ui.LoginActivity;
import com.bct.gpstracker.util.ByteUtil;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Msg;
import com.bct.gpstracker.vo.Session;

/**
 * Created by HH
 * Date: 2015/7/17 0017
 * Time: 上午 9:48
 */
public class BaseActivity extends Activity {
    protected static InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppContext.getEventBus().register(this);
        registerReceiver(bleBroadcastReceiver, bleIntentFilter());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        AppContext.getEventBus().unregister(this);
        unregisterReceiver(bleBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        IBinder binder = getCurrentFocus() == null ? null : getCurrentFocus().getApplicationWindowToken();
        if (binder != null && imm != null) {
            imm.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.onTouchEvent(event);
    }

    /**
     * 更新设备列表缓存
     *
     * @param msg
     */
    @Subscriber(tag = Constants.EVENT_TAG_TERM_STATUS)
    private void updateTermStatus(Msg msg) {
        if (msg == null || msg.getFrom() == null) {
            return;
        }
        String imei = msg.getFrom();
        int status = ByteUtil.byteArrayToInt(msg.getData());
        List<Device> devices = Session.getInstance().getMonitors();
        for (Device device : devices) {
            if (imei.equals(device.getImei())) {
                device.setOnline(status);
                break;
            }
        }
        Session.getInstance().setMonitors(devices);
    }

    @Subscriber(tag = Constants.EVENT_TAG_TERM_BIND)
    private void updateTermBindStatus(Msg msg) {
        if (msg == null || msg.getFrom() == null) {
            return;
        }
        String imei = msg.getFrom();
        int status = ByteUtil.byteArrayToInt(msg.getData());
        if (status != 1) {
            Log.i(BaseActivity.class.getName(), "收到无效终端绑定信息，直接忽略");
            return;
        }
        Session session = Session.getInstance();
        List<Device> devices = session.getMonitors();
        for (Device device : devices) {
            if (imei.equals(device.getImei())) {
                device.setBinded(true);
                break;
            }
        }
        session.setMonitors(devices);
        session.setChanged();
        session.notifyObservers();
//        CommUtil.showMsgShort(getString(R.string.bind_success));
    }

    @Subscriber(tag = Constants.EVENT_TAG_OFFLINE_NOTIFY)
    private void updateOfflineStatus(Object obj) {
        if (obj instanceof Msg) {
            Msg msg = (Msg) obj;
            if (CommUtil.isNotBlank(msg.getFrom()) && !msg.getFrom().equals(Session.getInstance().getImei())) {
                return;
            }
        }
        AppContext.forceLogout=true;
        CommunicationService cs = CommunicationService.get();
        if (cs != null) {
            cs.onDestroy();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_logout_offline_txt);
        builder.setTitle(R.string.msg_notify);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.known, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logout();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        clear();
    }

    private final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.BLE_ACTION_DISCONNECTED)) { //蓝牙断开
                Utils.Vibrate(BaseActivity.this, new long[]{1000, 2000, 1000, 2000}, true,
                        Utils.getPreferences(BaseActivity.this).getBoolean(Constants.MSG_VIBRATE, true));//震动2秒停1秒
                if (!Constants.isBlutActivityOpen)
                    VibrateDialog.getInstance().showDialog(BaseActivity.this, false);
            } else if (action.equals(Constants.VIBRATOR_CLOSE)) { //震动关闭
                Log.d("BaseActivity", "广播发过来了，手机震动关闭");
                Utils.closeVibrate(BaseActivity.this);
            }
        }
    };

    private static IntentFilter bleIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLE_ACTION_DISCONNECTED);
        intentFilter.addAction(Constants.VIBRATOR_CLOSE);
        return intentFilter;
    }

    /**
     * 退出
     */
    public void logout() {
        User.logout(this, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog("", Session.getInstance().getMainActivity());
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                BaseActivity.this.startActivity(intent);
                clear();
                Session.getInstance().clearData();
                AppContext.isEntered = false;
                AppContext.forceLogout=false;
                AppContext.managerInfoChecked=false;
                BaseActivity.this.finish();
            }

            @Override
            public void onFailure(String message) {
                if (CommUtil.isNotBlank(message)) {
                    Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                onSuccess(null);
            }
        });
    }

    private void clear() {
        Intent commIntent = new Intent(BaseActivity.this, CommunicationService.class);
        BaseActivity.this.stopService(commIntent);

        Intent exceptionIntent = new Intent(BaseActivity.this, ExceptionCatchService.class);
        BaseActivity.this.stopService(exceptionIntent);
    }

    public void sendHeartBeat(){
        MessageSender.getInstance().sendHeartBeatSingle();
    }
}
