package com.bct.gpstracker.server;

import java.util.LinkedList;

import android.util.Log;

import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.util.ByteUtil;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.SocketClient;

/**
 * Created by HH
 * Date: 2015/7/22 0022
 * Time: 上午 11:52
 */
public class MessageSenderThread extends Thread {
    private LinkedList<byte[]> byteList = new LinkedList<>();
    private SocketClient client;
    private int retryTimes=0;

    public void setClient(SocketClient client) {
        this.client=client;
    }

    public void addBytes(byte[] bytes) {
        this.byteList.add(bytes);
    }

    public void addBytesAtFirst(byte[] bytes) {
        this.byteList.addFirst(bytes);
    }

    public MessageSenderThread(SocketClient client) {
        this.client = client;
        this.setName("Sender Thread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(client==null||!client.isConnected()){
                    CommunicationService cs = CommunicationService.get();
                    if(cs==null){
                        return;
                    }
                    cs.connect();
                    synchronized (byteList){
                        byteList.wait();
                    }
                    if(client==null||!client.isConnected()){
                        continue;
                    }
                }
                synchronized (byteList) {
                    if (CommUtil.isEmpty(byteList) || client == null) {
                        byteList.wait();
                    }
                    if (CommUtil.isNotEmpty(byteList)) {
                        sendBytes();
                    }
                }
            }catch (InterruptedException ie){
                Log.i(MessageSenderThread.class.getName(),"发送线程已结束！");
                return;
            }catch (Exception e) {
                Log.e(Constants.TAG,"发送数据出错！",e);
                try {
                    synchronized (byteList){
                        byteList.wait();
                    }
                } catch (Exception e1) {
                    //
                }
            }
        }
    }

    private synchronized void sendBytes() {
        try {
            retryTimes=0;
            while (byteList.size() > 0) {
                byte[] bt = byteList.getFirst();
                if (bt != null && bt.length > 0) {
                    Log.d(Constants.TAG, "发送的数据长度：" + bt.length+" 数据:" + ByteUtil.bytesToHexString(bt));
                    boolean succ=client.send(bt);
                    if(succ){
                        byteList.removeFirst();
                    }else{
                        Log.i(Constants.TAG, "失去服务器连接，正在尝试重新连接！");
                        client.close();
                        CommunicationService cs=CommunicationService.get();
                        if(cs==null){
                            Log.e(MessageSenderThread.class.getName(),"连接实例为空，退出发送！");
                            return;
                        }
                        cs.connect();
                        synchronized (byteList) {
                            if (byteList.size() == 0 || retryTimes > 4) {
                                retryTimes = 0;
                                byteList.wait();
                            }
                        }
                        retryTimes++;
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (InterruptedException e) {
            //
        }
    }

    public void notifyBytes() {
        try {
            synchronized (byteList) {
                byteList.notifyAll();
            }
            synchronized (CommunicationService.lockObj){
                CommunicationService.lockObj.notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
