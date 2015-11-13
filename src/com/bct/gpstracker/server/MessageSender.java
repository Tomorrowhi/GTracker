package com.bct.gpstracker.server;

import java.text.SimpleDateFormat;
import java.util.*;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.fix.CountDownTimer;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;
import com.bct.gpstracker.vo.TermType;

/**
 * Created by HH
 * Date: 2015/7/22 0022
 * Time: 上午 9:34
 */
public class MessageSender {

    private static Context mContext;
    private MessageSenderThread senderThread;
    private Thread heartBeatThread;
    private static MessageSender sender;
    private SocketClient client;
    private Map<String, Long> nameMap = new HashMap<>();
    private Map<String, byte[]> cache = new HashMap<>();
    private Map<Long, TimeCount> countMap = new HashMap<>();
    private MessageSender() {

    }

    public static MessageSender getInstance() {
        if (sender == null) {
            synchronized (MessageSender.class) {
                if(sender == null) {
                    sender = new MessageSender();
                }
            }
        }
        return sender;
    }

    public static MessageSender getInstance(Context context) {
        if (sender == null) {
            synchronized (MessageSender.class) {
                if(sender == null) {
                    sender = new MessageSender();
                    mContext = context;
                }
            }
        }
        return sender;
    }

    public synchronized void start(SocketClient client) {
        this.client = client;
        if (senderThread != null) {
            senderThread.setClient(client);
        } else {
            senderThread = new MessageSenderThread(client);
            senderThread.start();
        }
    }

    /**
     * 启动发送心跳包线程
     * @return
     */
    public boolean sendHeartBeat() {
        if (heartBeatThread == null) {
            synchronized (MessageSender.class) {
                if (heartBeatThread == null) {
                    heartBeatThread = new Thread("Heart Beat Thread") {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    if (!Utils.isNetworkConnected(AppContext.getContext()) || client == null || !client.isConnected()) {
                                        CommunicationService cs = CommunicationService.get();
                                        if (cs != null) {
                                            cs.connect();
                                        }
                                        synchronized (this) {
                                            this.wait(Constants.HEART_BEAT_INTERVAL);
                                        }
                                        continue;
                                    }
                                    sendHeartBeatSingle();
                                    synchronized (this) {
                                        this.wait(Constants.HEART_BEAT_INTERVAL);
                                    }
                                } catch (InterruptedException ie) {
                                    Log.i(MessageSender.class.getName(), "心跳线程已结束！");
                                    return;
                                } catch (Exception e) {
                                    Log.e(Constants.TAG, "发送心跳包错误", e);
                                }
                            }
                        }
                    };
                    heartBeatThread.start();
                }
            }
        }
        return true;
    }

    /**
     * 发送单个心跳
     */
    public void sendHeartBeatSingle() {
        byte[] bytes = new byte[5];
        ByteUtil.setBytes(bytes, 0, 2, (short) 0xFFFF);
        ByteUtil.setBytes(bytes, 2, 1, (byte) 0x01);
        ByteUtil.setBytes(bytes, 3, 2, (short) 0xFFFF);
        senderThread.addBytes(bytes);
        senderThread.notifyBytes();
    }

    public boolean sendLogin(String imei) {
        try {
            byte[] bytes = new byte[13];
            ByteUtil.setBytes(bytes, 0, 2, (short) 0xFFFF);
            ByteUtil.setBytes(bytes, 2, 1, (byte) 0x00);
            ByteUtil.setBytesByHex(bytes, 3, 8, imei);
            ByteUtil.setBytes(bytes, 11, 2, (short) 0xFFFF);
            senderThread.addBytesAtFirst(bytes);
            senderThread.notifyBytes();
            sendHeartBeat();
            return true;
        } catch (Exception e) {
            Log.e(Constants.TAG, "发送登录包错误", e);
            return false;
        }
    }

    /**
     * 不要直接调用本类，应该使用 AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_SEND); 来发送
     *
     * @param chatMsg
     * @return
     */
    public boolean sendMessage(ChatMsg chatMsg) {
        if (chatMsg == null) {
            return true;
        }
        if (senderThread == null) {
            senderThread = new MessageSenderThread(client);
            senderThread.start();
        }
        Looper.prepare();
        byte[] bytes;
        byte[] bodyBytes;
        if (isTxtType(chatMsg)) {
            bodyBytes = chatMsg.getContent().getBytes();
        } else {
            bodyBytes = FileUtils.readFileAsBytes(AppContext.getContext(), chatMsg.getLocalUrl());
        }
        String currUserImei=Session.getInstance().getImei();
        if(CommUtil.isBlank(currUserImei)||currUserImei.length()!=15){
            CommUtil.showMsgShort(mContext.getString(R.string.imei_error));
            return false;
        }
        String name = chatMsg.getImei() + currUserImei + CommUtil.generateUniqueId(chatMsg.getId(), 14);
        nameMap.put(name, chatMsg.getId());
        String ext = getMsgExt(chatMsg);
        int extByteLen = ext.getBytes().length;
        int pgLen = Constants.PACKAGE_BODY_LENGTH;
        int byteLeft = bodyBytes.length % pgLen;
        int packs = byteLeft == 0 ? bodyBytes.length / pgLen : bodyBytes.length / pgLen + 1;
        int termFlag = TermType.APP.getType().intValue() == chatMsg.getTermType().intValue() ? 1 : 0;
        for (int i = 0; i < packs; i++) {
            int start = i * pgLen;
            if (i < packs - 1 || byteLeft == 0) {
                bytes = new byte[41 + pgLen + extByteLen];
                System.arraycopy(bodyBytes, start, bytes, 39 + extByteLen, pgLen);//内容体
                ByteUtil.setBytes(bytes, 7, 2, (short) pgLen);//内容体长度
                ByteUtil.setBytes(bytes, 9, 1, (byte) (((termFlag << 1) + 1) & 0xFF));//类型识别标识:APP 连续包
                ByteUtil.setBytes(bytes, 39 + extByteLen + pgLen, 2, (short) 0xFFFF);//结束
            } else {
                bytes = new byte[41 + byteLeft + extByteLen];
                System.arraycopy(bodyBytes, start, bytes, 39 + extByteLen, byteLeft);//内容体
                ByteUtil.setBytes(bytes, 7, 2, (short) byteLeft & 0xFFFF);//内容体长度
                ByteUtil.setBytes(bytes, 9, 1, (byte) ((termFlag << 1) & 0xFF));//类型识别标识:APP 连续包
                ByteUtil.setBytes(bytes, 39 + extByteLen + byteLeft, 2, (short) 0xFFFF);//结束
            }
            ByteUtil.setBytes(bytes, 0, 2, (short) 0xFFFF);//头
            ByteUtil.setBytes(bytes, 2, 1, (byte) 0x02);//控制域
            ByteUtil.setBytes(bytes, 3, 4, bodyBytes.length);//总长度

            int pkgNo = i + 1;
            ByteUtil.setBytes(bytes, 10, 1, (byte) (pkgNo & 0xFF));//包序号
            ByteUtil.setBytes(bytes, 11, 1, (byte) (chatMsg.getType() & 0xFF));//数据的格式
            ByteUtil.setBytes(bytes, 12, 4, start);//起始位置
            ByteUtil.setBytesByHex(bytes, 16, 22, name);//文件名
            ByteUtil.setBytes(bytes, 38, 1, (byte) (extByteLen & 0xFF));//后缀长度
            if (extByteLen > 0) {
                ByteUtil.setBytes(bytes, 39, extByteLen, ext);//后缀
            }
            cache.put(name + "_" + pkgNo, bytes);
            senderThread.addBytes(bytes);
            senderThread.notifyBytes();

            //聊天界面显示进度
            if (chatMsg.getId() != null && chatMsg.getId() > 0) {
                SenderMsgProgress progress = new SenderMsgProgress();
                progress.setId(chatMsg.getId());
                progress.setProgress((i + 1) / (double) packs * 100);
                AppContext.getEventBus().post(progress, Constants.EVENT_TAG_CHAT_PROGRESS);
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //
        }
        if (chatMsg.getId() != null && chatMsg.getId() > 0) {
            //发送完成后再发送一次，以显示100%进度，并且重置进度
            SenderMsgProgress progress = new SenderMsgProgress();
            progress.setId(chatMsg.getId());
            progress.setProgress(-1D);
            AppContext.getEventBus().post(progress, Constants.EVENT_TAG_CHAT_PROGRESS);

            TimeCount timeCount = new TimeCount(Constants.MSG_TIMEOUT, 1000, chatMsg.getId());
            timeCount.start();
            countMap.put(chatMsg.getId(), timeCount);
        }
        return true;
    }

    private boolean isTxtType(ChatMsg chatMsg) {
        return chatMsg.getType() == ContType.TXT.getType()
                ||chatMsg.getType()==ContType.CMD.getType();
    }

    private String getMsgExt(ChatMsg chatMsg) {
        if (chatMsg.getType() == ContType.TXT.getType()) {
            return "txt";
        }else if(chatMsg.getType() == ContType.CMD.getType()){
            return "pos";
        } else {
            return CommUtil.isNotBlank(chatMsg.getLocalUrl()) && chatMsg.getLocalUrl().contains(".") ?
                    chatMsg.getLocalUrl().substring(chatMsg.getLocalUrl().lastIndexOf('.') + 1, chatMsg.getLocalUrl().length()) : "";
        }
    }

    private String formatedDate() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return fmt.format(new Date());
    }

    /**
     * 接收到服务器回复包，分析哪些包需要重传
     *
     * @param name
     * @param bts
     */
    public void reSendPackage(String name, byte[] bts) {
        boolean isSuccess = true;
        for (int i = 0; i < bts.length; i++) {
            for (int j = 0; j < 8; j++) {
                int val = bts[i] >>> (8 - j - 1) & 0x1;
                if (val > 0) {
                    isSuccess = false;
                    int pkgNo = i * 8 + j;
                    byte[] bytes = cache.get(name + "_" + pkgNo);
                    if (bytes != null && bytes.length > 0) {
                        senderThread.addBytes(bytes);
                    }
                }
            }
        }
        Long id = nameMap.get(name);
        TimeCount timeCount = countMap.get(id);
        if (!isSuccess) {
            //如果没有完全成功，则重发后重新开始计时
            senderThread.notifyBytes();
            if (timeCount != null) {
                timeCount.cancel();
                timeCount = new TimeCount(Constants.MSG_TIMEOUT, 1000, id);
                timeCount.start();
                countMap.put(id, timeCount);
            }
        } else {
            if (id != null && id > 0) {
                //如果成功了，标记该消息为成功，并移除倒计时
                try {
                    AppContext.db.execNonQuery("update chat_msg set succ=1 where id=" + id);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "更新消息状态失败", e);
                }
                if (timeCount != null) {
                    timeCount.cancel();
                    countMap.remove(id);
                }
            }
        }
        clearPackage(name);
    }

    public void clearPackage(String name) {
        synchronized (cache) {
            Iterator<Map.Entry<String, byte[]>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey().startsWith(name)) {
                    it.remove();
                }
            }
        }
    }

    public void close() {
        if (senderThread != null) {
            senderThread.interrupt();
            senderThread=null;
        }
        if (heartBeatThread != null) {
            heartBeatThread.interrupt();
            heartBeatThread=null;
        }
        if (client != null) {
            client.close();
            client=null;
        }
    }

    /**
     * 收到完整消息后，回传接收包的成功状态
     * @param name
     * @param succPkg
     * @param sum
     * @param ext
     * @param type
     */
    public void responseServer(String name, Set<Integer> succPkg, int sum, String ext, int type) {
        byte[] respBytes = new byte[4];
        for (int i = 0; i < respBytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                int idx = i * 8 + j;
                if (idx < sum && !succPkg.contains(idx + 1)) {
                    respBytes[i] += 1 << (8 - j - 1);
                }
            }
        }
        int extLen = ext.getBytes().length;
        byte[] respBody = new byte[29 + respBytes.length + extLen];
        ByteUtil.setBytes(respBody, 0, 2, (short) 0xFFFF);
        ByteUtil.setBytes(respBody, 2, 1, (byte) 0x12);
        ByteUtil.setBytesByHex(respBody, 3, 22, name);
        ByteUtil.setBytes(respBody, 25, respBytes.length, respBytes);
        ByteUtil.setBytes(respBody, 25 + respBytes.length, 1, extLen);
        ByteUtil.setBytes(respBody, 26 + respBytes.length, extLen, ext);
        boolean isReSendType = ContType.FENCE.getType() == type;
        ByteUtil.setBytes(respBody, 26 + respBytes.length + extLen, 1, isReSendType ? 1 : 0);
        ByteUtil.setBytes(respBody, 27 + respBytes.length + extLen, 2, (short) 0xFFFF);
        senderThread.addBytes(respBody);
        senderThread.notifyBytes();
    }

    public void notifySenderThread() {
        senderThread.notifyBytes();
    }

    /**
     * 倒计时
     */
    private class TimeCount extends CountDownTimer {
        private Long id;

        public TimeCount(long millisInFuture, long countDownInterval, long id) {
            super(millisInFuture, countDownInterval);
            this.id = id;
        }

        @Override
        public void onFinish() {
            try {
                //倒计时完成，说明消息超时，标记失败
                if (countMap.containsKey(id)) {
                    countMap.remove(id);
                } else {
                    return;
                }
                AppContext.db.execNonQuery("update chat_msg set succ=0 where id=" + id);
                ChatActivity chatActivity = ChatActivity.getChatActivity();
                if (chatActivity != null) {
                    Message msg = chatActivity.getHandler().obtainMessage(chatActivity.UPDATE_CHAT_FAILED);
                    msg.obj = id;
                    chatActivity.getHandler().sendMessage(msg);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "更新消息状态失败", e);
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    public class SenderMsgProgress{
        private Long id;
        private Double progress;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Double getProgress() {
            return progress;
        }

        public void setProgress(Double progress) {
            this.progress = progress;
        }
    }
}
