package com.bct.gpstracker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.BluetoothActivity;
import com.bct.gpstracker.common.Constants;

/**
 * Created by longchao on 2015/9/2 0002.
 */
public class BlueToothService extends Service {

    private static final String TAG = BlueToothService.class.getSimpleName();

    private String btMessage = "";
    private boolean isConnectSuccess = false; //只有在连接成功过后才能发警报


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private ScheduledExecutorService scheduledExecutorService; //建立连接用的高度线程
    private connectTaskFirst connectTaskFirst;
    private String mAddress = "";
    private BlueToothService.reconnectTask reconnectTask;

    // 声明Notification(通知)的管理者
    private NotificationManager mNotifyMgr;
    private Notification notification;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.TAG, "蓝牙服务开启了...");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectTaskFirst = new connectTaskFirst();
        reconnectTask = new reconnectTask();
        registerReceiver(bleBroadcastReceiver, bleIntentFilter());

        initNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "蓝牙服务关闭了...");
        unregisterReceiver(bleBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                btMessage = device.getAddress() + "---" + device.getName() + "设备已发现:";
                sendBleLog(btMessage);
                Log.d(Constants.TAG, "当前的地址：" + mAddress);
                Log.d(Constants.TAG, "搜索到的地址：" + device.getAddress());

                if (mAddress.equals(device.getAddress())) {
                    Log.d(TAG, "设备找到了!!!");
//                    connectBLE(mAddress);
////                    short rssi = intent.getExtras().getShort(
////                            BluetoothDevice.EXTRA_RSSI);
////                    btMessage = device.getAddress()+"的信号强度："+rssi;
////                    sendMsgBroadcast(Constants.BLE_ACTION_DISCOVER);
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { //设备连接成功
                Log.d(Constants.TAG, "蓝牙连接成功：isConnectSuccess" + isConnectSuccess);
                Constants.isBleDisconnected = false;

                //2秒后如果没有断开，才是真的连接成功
                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.isBleDisconnected == false) {
                            isConnectSuccess = true;
                            mBluetoothAdapter.cancelDiscovery(); //停止搜索
                        }

                    }
                }, 2, TimeUnit.SECONDS);


                btMessage = device.getName() + "  " + device.getAddress() + "设备已连接！！";
                Log.d(TAG, btMessage);
//                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//                executor.scheduleWithFixedDelay(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, "连接情况:" + bluetoothSocket.isConnected()+(i++));
//                    }
//                },1,1,TimeUnit.SECONDS);
//                mBluetoothAdapter.cancelDiscovery(); //连接成功后，停止搜索
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                btMessage = device.getName() + "正在断开蓝牙连接。。。";

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                btMessage = device.getName() + "蓝牙连接已断开！！";
                Log.d(TAG, btMessage);
                Constants.isBleDisconnected = true; //蓝牙连接已断开

                //断开之后要重连
                if (Constants.isActivityConnected == false) { //只有不是主动断开的情况，才有断开重连
                    if (isConnectSuccess == true) //有连接成功能会断开重连
                        reconnectBle(mAddress);
                }

//                sendMsgBroadcast(Constants.BLE_ACTION_DISCONNECTED); //这个广播就是发出警告
            } else if (Constants.CONNECT_BLE.equals(action)) { //连接设备的广播
                Log.i(TAG, "activity发了连接设备的广播");
                mAddress = intent.getStringExtra(Constants.BLE_ADDRESS);
                //发了连接设备的广播，这里要做的是先搜索设备
//                mBluetoothAdapter.startDiscovery();
                connectBLE(mAddress);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //一个搜索周期结束
                Log.i(TAG, "一个搜索周期结束");
            }
        }

    };


    private static IntentFilter bleIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND); //发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //设备已连接
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED); //正在断开蓝牙
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //蓝牙已断开
        intentFilter.addAction(Constants.CONNECT_BLE);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return intentFilter;
    }

    /**
     * 连接蓝牙设备的方法
     *
     * @param address 蓝牙的mac值
     */
    private void connectBLE(String address) {
        Constants.isActivityConnected = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery(); //点连接时开始搜索
        //每次连接之前都要先置为false，直到成功时才为true
        isConnectSuccess = false;

        Log.i(TAG, " 设备为客户端………… ");


//        sendUIInfo("开始搜索设备");
        ArrayList<String> deviceList = new ArrayList<>();

        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            Log.d(TAG, "已绑定的设备:" + device.getName() + "::" + device.getBondState());
            deviceList.add(device.getAddress());
        }
//        if (!deviceList.contains(address)) {
//            Toast.makeText(BlueToothService.this, "首次连接设备请点击菜单栏的蓝牙配对", Toast.LENGTH_LONG).show();
//        }


        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG, "设备连接上没？" + mBluetoothDevice.getName());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.submit(connectTaskFirst);

    }

    /**
     * 蓝牙的断开重连，第一次
     */
    private void reconnectBle(String address) {
        Log.i(TAG, " 断开重连 ");
        mBluetoothAdapter.getRemoteDevice(address);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(reconnectTask);
    }


    /**
     * 第一次连接时
     */
    private class connectTaskFirst implements Runnable {

        @Override
        public void run() {
            try {
                sendBleLog("开始连接设备,第一次" + mAddress);
                Log.d(TAG, "开始连接设备,第一次");

                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.w(TAG, "ble socket第一次连接失败,进行第二次连接",e);
                Log.d(Constants.TAG, "连接失败要关流");
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new Thread(new connectTaskSecond()).start();
                e.printStackTrace();
            }
        }
    }

    /**
     * 第二次连接
     */
    private class connectTaskSecond implements Runnable {

        @Override
        public void run() {
            try {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendBleLog("开始连接设备,第二次" + mAddress);
                mBluetoothAdapter.getRemoteDevice(mAddress);

                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.w(TAG, "ble socket第二次连接失败", e);
                Log.d(Constants.TAG, "连接失败要关流");
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //然后进行第三次连接
                new Thread(new connectTaskThird()).start();
                e.printStackTrace();
            }
        }
    }

    /**
     * 第三次连接
     */
    private class connectTaskThird implements Runnable {

        @Override
        public void run() {
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendBleLog("开始连接设备,第三次" + mAddress);
                mBluetoothAdapter.getRemoteDevice(mAddress);
                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                shutdownClient();
                Log.w(TAG, "ble socket第三次连接失败，不连接了，发连接不上的广播",e);
                sendBroadcast(new Intent(Constants.BLE_CONNECTED_FAIL));
                e.printStackTrace();
            }
        }
    }


    /**
     * 断开重连时用的task第一次
     */
    private class reconnectTask implements Runnable {

        @Override
        public void run() {
            try {
                sendBleLog("断开重连第一次");
                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.d(Constants.TAG, "断开重连失败连接失败要关流");
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new Thread(new reconnectTaskSecond()).start();
                e.printStackTrace();
            }
        }
    }

    /**
     * 第二次断开重连
     */
    private class reconnectTaskSecond implements Runnable {
        @Override
        public void run() {
            try {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendBleLog("断开重连,第二次" + mAddress);
                mBluetoothAdapter.getRemoteDevice(mAddress);
                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.w(TAG, "ble socket第二次断开重连失败", e);
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //然后进行第三次连接
                new Thread(new recconnectTaskThird()).start();
                e.printStackTrace();
            }
        }
    }

    /**
     * 第三次断开重连
     */
    private class recconnectTaskThird implements Runnable {
        @Override
        public void run() {
            try {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendBleLog("断开重连,第三次" + mAddress);
                mBluetoothAdapter.getRemoteDevice(mAddress);
                bluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.w(TAG, "ble socket第三次断开重连失败，报警", e);
                shutdownClient();
                Constants.isBleConnected = false;
                if (!Constants.isActivityConnected) { //如果不是主动断开，才发警报
                    Log.w(TAG, "重连接失败，不是主动断开的，发蓝牙断开了的指令");
                    Log.d(TAG, "isConnectSuccess:" + isConnectSuccess);
                    if (isConnectSuccess == true) { //只有在成功连接过才能发警报
                        sendBroadcast(new Intent(Constants.BLE_ACTION_DISCONNECTED));
                        //这里把通知也发了
                        // 发布和管理所要创建的Notification
                        mNotifyMgr.notify("ble", R.drawable.ic_launcher, notification);
                        isConnectSuccess = false;
                    }
                }
                e.printStackTrace();
            }
        }
    }


    //断开蓝牙连接
    private void shutdownClient() {
        if (null != scheduledExecutorService) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bluetoothSocket = null;
        }
    }

    //初始化notify
    private void initNotification() {
        // TODO Auto-generated method stub
        // 创建NotificationManager对象
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 创建一个即将要执行的PendingIntent对象
        Intent resultIntent = new Intent(BlueToothService.this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                BlueToothService.this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification(R.drawable.ic_launcher, "宝定离开安全区域", System.currentTimeMillis());
        notification.setLatestEventInfo(BlueToothService.this, "蓝牙连接断开", "宝定离开安全区域", resultPendingIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // notification.defaults = Notification.DEFAULT_SOUND; // 使用系统默认声音
    }

    private void sendBleLog(String str) {
        Intent intent = new Intent(BluetoothActivity.BLE_LOG_INFO);
        intent.putExtra(BluetoothActivity.BLE_LOG_INFO, str);
        sendBroadcast(intent);
    }
}
