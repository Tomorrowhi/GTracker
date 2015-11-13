package com.bct.gpstracker.baby.activity;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.VibrateDialog;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.service.BlueToothService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.vo.Cmd;
import com.bct.gpstracker.vo.CmdType;
import com.bct.gpstracker.vo.Session;


/**
 * Created by longchao on 2015/9/1 0001.
 * 蓝牙防偷的activity
 */
public class BluetoothActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = BluetoothActivity.class.getSimpleName();


    private BluetoothAdapter mBluetoothAdapter;
    private String address = ""; //终端设备的地址值


    private int bleConnectState = 0; //蓝牙连接状态
    int point = 0;

    private static final int CONSTATE_NORMAL = 0; //初始状态，未连接
    private static final int CONSTATE_SUCCESS = 1; //连接成功
    private static final int CONSTATE_FAIL = 2;//连接失败
    private static final int CONSTATE_CYCLEEND = 3; //一个搜索周期结束
    private static final int CONSTATE_DISCONNECT = 4; //蓝牙断开
    public static final String BLE_LOG_INFO = "ble_log_info";
    private boolean isLogTextOpen = false;


    private ImageButton backBtn;
    private TextView bleStateTV, bleLog, titleNameTV;
    private TextView bleDesTV;
    private CheckBox bleStateCB;
    private ImageView bleHeadIV;

    private void assignViews() {
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        bleStateTV = (TextView) findViewById(R.id.bleStateTV);
        bleDesTV = (TextView) findViewById(R.id.bleDesTV);
        bleStateCB = (CheckBox) findViewById(R.id.bleStateCB);
        bleHeadIV = (ImageView) findViewById(R.id.bleHeadIV);
        bleLog = (TextView) findViewById(R.id.bleLog);
        titleNameTV = (TextView) findViewById(R.id.titleNameTV);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        assignViews();
        init();


        bleLog.setText("");

        //进入后，获取对应设备的mac
        Device device = Session.getInstance().getDevice(ChatActivity.mEntityImei);
        if (null != device)
            getMacAddress(device.getMac());


//        address = "45:88:C6:F4:62:60";//先用这个设备来干活
//        address = "67:E5:8D:7D:62:60";//先用这个设备来干活


        bleDesTV.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        Constants.isBlutActivityOpen = true;

        getDevice();

    }

    private void getMacAddress(String deviceAddress) {
        address = deviceAddress.toUpperCase(); //蓝牙地址必须为大写，否则会报错

        Log.d(TAG, "服务器上取出的地址为：" + address);
        setBleLogText("服务器取出的地址为：" + address);
//        address = "9:52:88:5A:62:60";
        if (null != address && address.length() != 17) { //要补齐mac地址
            Log.d(TAG, "要补齐address");
            String[] strings = address.split(":");
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].length() != 2) {
                    strings[i] = "0" + strings[i];
                }
            }
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < strings.length; i++) {
                buffer.append(strings[i]).append(":");
            }
            if (buffer.length() >= 17)
                address = buffer.substring(0, 17);
        }
        Log.d(TAG, "服务器上取出的地址为：" + address + "位数：" + address.length());
        setBleLogText("补齐后的地址：" + address + "位数：" + address.length());
    }

    private void init() {
        bleDesTV.setOnClickListener(this);
        openHandBle();  //进来就先发一个命令
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
        startService(new Intent(BluetoothActivity.this, BlueToothService.class));
        registerReceiver(bleBroadcastReceiver, bleIntentFilter());
//        bleStateCB.setChecked(mBluetoothAdapter.isEnabled());
        bleStateCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bleStateCB.isChecked()) { //这个时候是没有勾选上，则点击后勾选
                    if (null == address || address.length() != 17) {
                        Toast.makeText(BluetoothActivity.this, "手表蓝牙ID未注册到系统中，请与系统管理员联系", Toast.LENGTH_SHORT).show();
                        bleStateCB.setChecked(false);
                        return;
                    }

                    Log.d(Constants.TAG, "点这里，勾选");
                    if (!mBluetoothAdapter.isEnabled())
                        mBluetoothAdapter.enable();
                    bleStateTV.setText(getString(R.string.blue_phoneble_open));
                    if (!Constants.isBleConnected) { //只要不是连接成功的，点击都有效
                        bleLog.setText("补齐后的地址：" + address + "位数：" + address.length());
                        //这里发命令开启终端的蓝牙
                        openHandBle();
//                        bleDesTV.setText("搜索中...");
//                        searchingText();


                        WizardAlertDialog.getInstance().showProgressDialog(R.string.blue_searching4, BluetoothActivity.this, 1);

                        final Intent intent = new Intent(Constants.CONNECT_BLE);
                        intent.putExtra(Constants.BLE_ADDRESS, address);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                    Log.d(Constants.TAG, "延时2秒连接");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                sendBroadcast(intent);
                            }
                        }).start();

                    }
                } else {
                    //这里是手动关闭蓝牙，则不应该让蓝牙报警
                    Constants.isActivityConnected = true;
                    bleDesTV.setText(getResources().getString(R.string.ble_open_ble));
                    Constants.isBleConnected = false;
                    bleDesTV.setVisibility(Constants.isBleConnected ? View.VISIBLE : View.GONE);
                    Log.d(Constants.TAG, "点这里，不勾选");
                    mBluetoothAdapter.disable();
                    bleStateTV.setText(getString(R.string.blue_phoneble_close));
                    //这里发命令关闭终端的蓝牙
                    Cmd cmd = new Cmd(CmdType.BL.getType(), "0", ChatActivity.mEntityImei, true); //0为关闭蓝牙
                    CommUtil.sendMsg(CommHandler.SEND_COMMAND, cmd);
                }
            }
        });
        bleStateCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //勾选上了就开启蓝牙
                    bleStateTV.setText(getString(R.string.blue_phoneble_open));

                } else { //否则就关闭蓝牙
                    bleStateTV.setText(getString(R.string.blue_phoneble_close));
                }
            }
        });
//        bleStateTV.setText(mBluetoothAdapter.isEnabled() ? "手机蓝牙：已开启" : "手机蓝牙：已关闭");
        titleNameTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isLogTextOpen) { //为true，则是要关闭
                    bleLog.setVisibility(View.GONE);
                    isLogTextOpen = false;
                    Toast.makeText(BluetoothActivity.this, "测试日志关闭", Toast.LENGTH_SHORT).show();
                } else {
                    bleLog.setVisibility(View.VISIBLE);
                    isLogTextOpen = true;
                    Toast.makeText(BluetoothActivity.this, "测试日志开启", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    /**
     * 开启终端的蓝牙
     */
    private void openHandBle() {
        //每次进入界面，先发一个命令开启终端蓝牙

        Cmd cmd = new Cmd(CmdType.BL.getType(), "1", ChatActivity.mEntityImei, true); //1为开启蓝牙
        CommUtil.sendMsg(CommHandler.SEND_COMMAND, cmd);
        setBleLogText("远程开启蓝牙命令已发");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                BluetoothActivity.this.finish();
                break;
            case R.id.bleDesTV:

                break;
//            case R.id.bleDesTV:
//                Log.d(TAG, "点击连接设备");
//                if (!mBluetoothAdapter.isEnabled()) {
//                    Toast.makeText(BluetoothActivity.this, getString(R.string.blue_openble), Toast.LENGTH_SHORT).show();
//                } else {
//                    if (!Constants.isBleConnected) { //只要不是连接成功的，点击都有效
//                        openHandBle();
////                        bleDesTV.setText("搜索中...");
//                        searchingText();
//                        Intent intent = new Intent(Constants.CONNECT_BLE);
//                        intent.putExtra(Constants.BLE_ADDRESS, address);
//                        sendBroadcast(intent);
//                    } else {
//
//                    }
//                }
//                break;

        }
    }


    private static IntentFilter bleIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLE_ACTION_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BLE_LOG_INFO);
        intentFilter.addAction(Constants.BLE_CONNECTED_FAIL);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //已连接

        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return intentFilter;
    }


    private final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.BLE_ACTION_DISCONNECTED)) { //蓝牙服务发蓝牙断开的连接（这里是重连后还连不上才发的广播）
                Constants.isBleConnected = false;
                bleConnectState = CONSTATE_DISCONNECT;
                Log.d(TAG, "蓝牙断开了....");
                VibrateDialog.getInstance().showDialog(BluetoothActivity.this, false);
                bleDesTV.setText(getResources().getString(R.string.ble_open_ble));
                bleDesTV.setVisibility(Constants.isBleConnected ? View.VISIBLE : View.GONE);
                bleStateCB.setChecked(false);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) { //已连接
                Log.d(Constants.TAG, "连接成功,activity");

                //2秒后如果没有断开，才是真的连接成功
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                    @Override
                    public void run() {

                        if (Constants.isBleDisconnected == false) {
                            Constants.isBleConnected = true;
                            bleConnectState = CONSTATE_SUCCESS;
                            Log.d(TAG, "连接成功!!!");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    WizardAlertDialog.getInstance().closeProgressDialog();
                                    bleStateCB.setChecked(true);
                                    bleDesTV.setText(getString(R.string.blue_connect_success));
                                    bleDesTV.setVisibility(Constants.isBleConnected ? View.VISIBLE : View.GONE);
                                }
                            });
                        } else {
                            Log.d(Constants.TAG, "否则还是算失败");
                            Constants.isActivityConnected = false;
                            connectFail();
                        }
                    }
                }, 2, TimeUnit.SECONDS);


            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) { //发现设备

            } else if (action.equals(Constants.BLE_CONNECTED_FAIL)) { //连接失败
                Log.d(Constants.TAG, "连接失败");
//                if (Constants.isBleConnected == false)


                connectFail();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) { //一个搜索周期结束
//                bleConnectState = CONSTATE_CYCLEEND;
//
//                if (!Constants.isBleConnected)
//                    bleDesTV.setText(getString(R.string.blue_no_serach_device));
            } else if (action.equals(BLE_LOG_INFO)) { //蓝牙Log显示
                setBleLogText((String) intent.getCharSequenceExtra(BLE_LOG_INFO));
            }
        }
    };

    /**
     * 连接失败
     */
    private void connectFail() {
        bleConnectState = CONSTATE_FAIL;

        Constants.isBleConnected = false;
        if (Constants.isActivityConnected == false) {
            bleDesTV.setText(getString(R.string.blue_connect_fail));
            bleDesTV.setVisibility(View.VISIBLE);
        }
        bleStateCB.setChecked(false);
        WizardAlertDialog.getInstance().closeProgressDialog();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bleBroadcastReceiver);
        Constants.isBlutActivityOpen = false;

        Log.d(Constants.TAG, "destroy方法是走了的 isBleConnected:" + Constants.isBleConnected);

        if (Constants.isBleConnected == false) { //退出时，如果没有连接，则关闭
            Log.d(Constants.TAG, "这里进得来吗:");
            mBluetoothAdapter.disable();
            //这里发命令关闭终端的蓝牙
            Cmd cmd = new Cmd(CmdType.BL.getType(), "0", ChatActivity.mEntityImei, true); //0为关闭蓝牙
            CommUtil.sendMsg(CommHandler.SEND_COMMAND, cmd);
            stopService(new Intent(BluetoothActivity.this, BlueToothService.class));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (null != mBluetoothAdapter) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(Constants.TAG, "蓝牙未打开啊啊啊啊啊！！！");
                Constants.isBleConnected = false;
            }
            bleStateCB.setChecked(Constants.isBleConnected);
            bleStateTV.setText(Constants.isBleConnected ? getString(R.string.blue_phoneble_open) : getString(R.string.blue_phoneble_close));
        }
        if (Constants.isBleConnected) {
            bleDesTV.setText(getString(R.string.blue_connect_success));
            bleDesTV.setVisibility(Constants.isBleConnected ? View.VISIBLE : View.GONE);
        } else {
            bleDesTV.setText(getResources().getString(R.string.ble_open_ble));
            bleDesTV.setVisibility(Constants.isBleConnected ? View.VISIBLE : View.GONE);
        }

        VibrateDialog.getInstance().bleDisconnected(BluetoothActivity.this);
    }

    private void setBleLogText(String string) {
        String text = (String) bleLog.getText();
        text = text + "\n" + string;
        bleLog.setText(text);
    }


    /**
     * 获取所有的设备
     */
    private void getDevice() {
        Device.getList(BluetoothActivity.this, new BctClientCallback() {
            @Override
            public void onStart() {
//                WizardAlertDialog.getInstance().showProgressDialog(R.string.get_device_data, AppContext.getContext());
            }

            @Override
            public void onFinish() {
//                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    Log.d(Constants.TAG, "当前的imei:"+ChatActivity.mEntityImei);
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        Device device = new Device(JSONHelper.getJSONObject(obj.getBodyArray(), i));

                        if (device.getImei().equals(ChatActivity.mEntityImei)) {
                            Log.d(Constants.TAG, "找到了当前的imei，重新设置address");
                            getMacAddress(device.getMac());
                        }
                    }

                } else {
                }
            }

            @Override
            public void onFailure(String message) {
                if (CommUtil.isNotBlank(message)) {
//                    makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}