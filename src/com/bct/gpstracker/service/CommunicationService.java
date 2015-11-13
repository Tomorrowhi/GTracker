package com.bct.gpstracker.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import org.simple.eventbus.Subscriber;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.server.MessageReceiver;
import com.bct.gpstracker.server.MessageSender;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.SocketClient;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Session;

/**
 * Created by HH
 * Date: 2015/7/17 0017
 * Time: 上午 9:51
 */
public class CommunicationService extends Service {
    private Lock lock = new ReentrantLock();
    public static Object lockObj=new Object();
    private Handler handler = new ServiceHandler();
    private MessageReceiver receiver = null;
    public MessageSender sender = MessageSender.getInstance(CommunicationService.this);
    private ConnectionRunnable connRunnable = new ConnectionRunnable();
    public SocketClient client;
    private String userId;//目前就是手机的IMEI
    private Integer countDown=0;
    public static final int HANDLER_COUNT_DOWN =1;
    public static final int HANDLER_CONN=2;
    private long lastConnectedTime=0;
    private boolean retryStarted=false;

    private static CommunicationService service;

    public static CommunicationService get() {
        Log.i(CommunicationService.class.getName(), "当前Service实例：" + (service == null ? null : service.toString()));
        return service;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Messenger mMessenger = new Messenger(handler);
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        AppContext.getEventBus().register(this);
//        initService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initService();

        Log.d(Constants.TAG, "Command 初始化！");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        service=null;
        if(receiver!=null){
            receiver.interrupt();
            receiver=null;
        }
        if(sender!=null){
            sender.close();
        }
        AppContext.getEventBus().unregister(this);
        handler.removeMessages(HANDLER_CONN);
        super.onDestroy();
    }

    public void initService() {
        connect();
    }

    public void connect() {
        synchronized (countDown) {
            if(countDown>0){
                return;
            }else {
                countDown=10;
                handler.sendEmptyMessageDelayed(HANDLER_COUNT_DOWN, 1000);
            }
        }
        long uid = Session.getInstance().getLoginedUserId();
        if (uid != 0) {
            userId = Session.getInstance().getImei();
        }
        if (client != null && client.isConnected() || !Utils.isNetworkConnected(this) || CommUtil.isBlank(userId)) {
            return;
        }
        if (!retryStarted) {
            scheduledNextConnect();
        }
    }

    private void scheduledNextConnect() {
//        if(!Utils.isNetworkConnected(this)){
//            schedule.shutdown();
//            return;
//        }
        retryStarted = true;
        long inter = System.currentTimeMillis() - lastConnectedTime;
        if (lastConnectedTime == 0) {
            lastConnectedTime = System.currentTimeMillis();
            handler.sendEmptyMessageDelayed(HANDLER_CONN, 0);
        } else if (inter < 5 * 60 * 1000) {
            handler.sendEmptyMessageDelayed(HANDLER_CONN, 10 * 1000);
        } else if (inter < 30 * 60 * 1000) {
            handler.sendEmptyMessageDelayed(HANDLER_CONN, 60 * 1000);
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_CONN, 5 * 60 * 1000);
        }
    }

    private class ConnectionRunnable implements Runnable {
        @Override
        public void run() {
                lock.lock();
                try {
                    Log.i(Constants.TAG, "开始连接通讯服务器");
                    if (client != null && client.isConnected()) {
                        synchronized (lockObj) {
                            lockObj.notifyAll();
                        }
                        lastConnectedTime=0;
                        retryStarted=false;
                        return;
                    }
                    if (client == null) {
                        String ip;
                        SharedPreferences preferences = Utils.getPreferences(AppContext.getContext());
                        ip = preferences.getString(Constants.SETTING_SERVER_IP, null);
                        if (CommUtil.isBlank(ip) || !ip.matches(Constants.REGX_IP_PORT)) {
                            ip = Utils.getMetaValue(AppContext.getContext(), "chat_server_ip");
                        }
                        String[] ips = ip.split(":| ");
                        client = new SocketClient(ips[0], Integer.valueOf(ips[1]));
                    }
                    client.setTimeOut(10000);
                    if(AppContext.forceLogout){
                        Log.w(CommunicationService.class.getName(),"APP已标记强制退出,取消连接");
                        return;
                    }
                    boolean succ = client.connect();
                    if (!succ) {
                        Log.i(CommunicationService.class.getName(),"尝试连接失败...");
                        scheduledNextConnect();
                        return;
                    }
                    Log.i(CommunicationService.class.getName(),"连接成功！");
                    sender.start(client);
                    sender.sendLogin(userId);
                    if (receiver == null) {
                        receiver = new MessageReceiver(client);
                        receiver.start();
                    } else {
                        receiver.setClient(client);
                    }
                    lastConnectedTime=0;
                    retryStarted=false;
                    synchronized (lockObj) {
                        lockObj.notifyAll();
                    }
                    sender.notifySenderThread();
                }catch (Exception e){
                    Log.i(CommunicationService.class.getName(),"连接服务器出错！",e);
                }finally {
                    lock.unlock();
                }
            }
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_COUNT_DOWN:
                    //单线程操作的延时锁定
                    countDown--;
                    if(countDown>0){
                        handler.sendEmptyMessageDelayed(HANDLER_COUNT_DOWN,1000);
                    }
                    break;
                case HANDLER_CONN:
                    new Thread(connRunnable,"Connection Thread").start();
                    break;
            }
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_CHAT_SEND)
    private void sendMessage(final ChatMsg chatMsg) {
        if (chatMsg != null && sender != null) {
            new Thread() {
                @Override
                public void run() {
                    sender.sendMessage(chatMsg);
                }
            }.start();
        } else {
            connect();
            Intent intent = new Intent(Constants.ACTION_NEW_MSG);
            intent.putExtra("id", chatMsg.getId());
            intent.putExtra("type", 99);
            CommunicationService.get().sendBroadcast(intent);
            Log.e(Constants.TAG, "发送消息失败，sender为null!");
        }
    }
}
