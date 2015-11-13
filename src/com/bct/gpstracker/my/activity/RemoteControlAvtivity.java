package com.bct.gpstracker.my.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/8/27 0027.
 * 远程控制
 */
public class RemoteControlAvtivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.backBtn)
    private ImageButton backBtn;
    @ViewInject(R.id.alarmLayout)
    private RelativeLayout alarmLayout;
    @ViewInject(R.id.limitLayout)
    private RelativeLayout limitLayout;
    @ViewInject(R.id.wifiLayout)
    private RelativeLayout wifiLayout;
    @ViewInject(R.id.powerLayout)
    private RelativeLayout powerLayout;
    @ViewInject(R.id.fenceLayout)
    private RelativeLayout fenceLayout;
    @ViewInject(R.id.audioControl)
    private RelativeLayout audioControl;


    private Intent intentMsg;
    private Context mContext = RemoteControlAvtivity.this;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        ViewUtils.inject(this);
        initView();
        initEvent();
    }

    private void initView() {
        view = View.inflate(mContext, R.layout.activity_remote_control, null);
    }


    private void initEvent() {
        backBtn.setOnClickListener(this);
        alarmLayout.setOnClickListener(this);
        limitLayout.setOnClickListener(this);
        wifiLayout.setOnClickListener(this);
        powerLayout.setOnClickListener(this);
        fenceLayout.setOnClickListener(this);
        audioControl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioControl:
                //音量调整
                Toast.makeText(mContext,"音量调整待实现",Toast.LENGTH_SHORT).show();
                break;
            case R.id.fenceLayout:
                //电子栅栏
                selectDevice("fence");
                break;
            case R.id.alarmLayout:
                //生活助手
                selectDevice("alarm");
                break;
            case R.id.limitLayout:
                //休眠时段
                selectDevice("limit");
                break;
            case R.id.wifiLayout:
                //家庭WIFI
                intentMsg = new Intent(mContext, WifiActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.powerLayout:
                //远程关机
                selectDevice("power");
                break;
            case R.id.backBtn:
                //返回按键
                this.finish();
                break;
        }
    }

    public void selectDevice(final String type) {
        List<Device> devices = Session.getInstance().getMonitors();
        String[] ds = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            ds[i] = devices.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择终端");
        builder.setItems(ds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Session.getInstance().setSetupDevice(Session.getInstance().getMonitors().get(i));
                switch (type) {
                    case "fence":
                        Intent fenceIntent = new Intent(mContext, FenceListActivity.class);
                        startActivity(fenceIntent);
                        break;
                    case "limit":
                        Intent restIntent = new Intent(mContext, DeviceRestActivity.class);
                        startActivity(restIntent);
                        break;
                    case "alarm":
                        Intent alarmIntent = new Intent(mContext, AlarmActivity.class);
                        startActivity(alarmIntent);
                        break;
                    case "power":
                        sendCommand(MyConstants.REMOTE_CLOSE);
                        break;
                    case MyConstants.TERM_FEE:
                        sendCommand(MyConstants.TERM_FEE);
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void sendCommand(String type) {
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
                    Toast.makeText(mContext, R.string.send_success, Toast.LENGTH_SHORT).show();
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
}
