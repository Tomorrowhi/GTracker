package com.bct.gpstracker.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Msg;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.exception.DbException;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 上午 9:30
 */
public class MessageReceiver extends Thread {
    private SocketClient client;

    private Msg msg = null;
    private byte[] body = null;
    private byte[] byteFragment = null;
    private int sumLen = 0;
    private Set<Integer> succPkg=new HashSet<>();

    public static final int INVALID_PACK_LEN=-1;
    public static final int APPEND_PACK_LEN=-2;

    public MessageReceiver(SocketClient client) {
        this.client = client;
        this.setName("Receiver Thread");
    }

    public void setClient(SocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (client == null||!client.isConnected()) {
                    CommunicationService cs = CommunicationService.get();
                    if (cs == null) {
                        return;
                    }
                    cs.connect();
                    synchronized (CommunicationService.lockObj) {
                        CommunicationService.lockObj.wait();
                    }
                    if (client == null||!client.isConnected()) {
                        continue;
                    }
                }

                byte[] bytes = client.receive();
                if (bytes != null && bytes.length > 0) {
                    Log.d(Constants.TAG, "获取到的据长度：" + bytes.length+" 数据：" + ByteUtil.bytesToHexString(bytes));
                    if(bytes.length<1000){
                        Log.d(Constants.TAG,"可显示内容："+(new String(bytes)));
                    }
                    process(bytes);
                }
                Thread.sleep(Constants.RECEIVE_INTERVAL);
            } catch (InterruptedException ie){
                Log.i(MessageReceiver.class.getName(),"接收线程已结束！");
                return;
            }catch (Exception e) {
                Log.e(Constants.TAG, "出错", e);
            }
        }
    }

    private void process(byte[] bts) {
        int begin = 0;
        byte[] bytes;
        if (byteFragment != null && byteFragment.length > 0) {
            int newLen=byteFragment.length + bts.length;
            if(newLen>Constants.MAX_RECEIVE_LENGTH){
                Log.e(Constants.TAG,"接收到的文件过大，已丢弃！长度："+newLen);
                byteFragment=null;
                sumLen=0;
                body=null;
                msg=null;
                succPkg.clear();
                return;
            }
            bytes = new byte[newLen];
            System.arraycopy(byteFragment, 0, bytes, 0, byteFragment.length);
            System.arraycopy(bts, 0, bytes, byteFragment.length, bts.length);
            byteFragment = null;
        } else {
            bytes = bts;
        }
        int packLen;
        do {
            packLen = findPackageLen(bytes, begin);
            if (packLen == APPEND_PACK_LEN && begin < bytes.length) {
                begin = findValidPackagePosition(bytes, begin);
                byteFragment = new byte[bytes.length - begin];
                System.arraycopy(bytes, begin, byteFragment, 0, byteFragment.length);
                return;
            }

            if (packLen == 0) {
                return;
            }

            if (packLen == INVALID_PACK_LEN) {
                Log.d(Constants.TAG, "未获取到有效的数据");
                return;
            }

            int flag = bytes[begin + 2];
            if (flag == 0x12) {
                processReponse(bytes, begin);
                begin += packLen;
                packLen = findPackageLen(bytes, begin);
                continue;
            }
            if (flag != 0x02) {
                begin += packLen;
                packLen = findPackageLen(bytes, begin);
                continue;
            }

            int serial = (int) bytes[begin + 10];
            if (body == null || serial == 1) {
                int contLen = ByteUtil.subBytesToInt(bytes, begin + 3, 4);
                body = new byte[contLen];
                sumLen = 0;
                msg = null;
                succPkg.clear();
            }
            if (msg == null) {
                msg = new Msg();
            }
            int bodyLen = ByteUtil.subBytesToInt(bytes, begin + 7, 2);
            int byteStartIndex = ByteUtil.subBytesToInt(bytes, begin + 12, 4);
            int extLen = bytes[begin + 38];
            if(bodyLen > 0) {
                System.arraycopy(bytes, begin + 39 + extLen, body, byteStartIndex, bodyLen);
            }
            succPkg.add(serial);
            sumLen += bodyLen;
            String name = ByteUtil.subBytesToHex(bytes, begin + 16, 22);
            String ext = ByteUtil.subBytesToString(bytes, begin + 39, extLen);
            int type = bytes[begin + 11];
            if (msg.getName() == null) {
                if (name.length() > 30) {
                    msg.setSelf(name.substring(0, 15));
                    msg.setFrom(name.substring(15, 30));
                }
                msg.setName(name + '.' + ext);

                msg.setType(ContType.getType(type));
            }
            boolean isEnd = (bytes[begin + 9] & 0x01) == 0;
            if (isEnd) {
                MessageSender.getInstance().responseServer(name, succPkg, serial, ext, type);
            }
            boolean isURL = ((bytes[begin + 9]>>>3) & 0x01) == 1;
            if (sumLen >= body.length) {
                msg.setData(body);
                //Send to UI
                saveMsg(msg,isURL);
                msg = null;
                body = null;
                sumLen = 0;
                succPkg.clear();
                byteFragment = null;
            }
            begin += packLen;
        } while (packLen > 0);
    }

    private void saveMsg(Msg msg, boolean isURL) {
        Log.d(Constants.TAG,"收到的内容为："+msg.toString());
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setImei(msg.getFrom());
        chatMsg.setType(msg.getType().getType());
        User user = Session.getInstance().getUser();
        long userId;
        if (user != null) {
            userId = user.getId();
        } else {
            SharedPreferences mSharedPreferences = Utils.getPreferences(AppContext.getContext());
            userId = mSharedPreferences.getLong(MyConstants.USER_ID, 0);
        }
        if (userId == 0) {
            return;
        }
        chatMsg.setUserId(userId);
        if (ContType.TXT.equals(msg.getType())) {
            chatMsg.setContent(new String(msg.getData()));
        } else if(!isURL){
            //非URL且非文本类型，普通图片，视频，语音形式
            String folderName = CommUtil.genSpecificName(msg.getSelf(),100);
            try {
                String path = FileUtils.saveFile(AppContext.getContext(), folderName, msg.getName(), msg.getData());
                chatMsg.setLocalUrl(path);

                if(ContType.AUDIO.equals(msg.getType())) {
                    Long duration = MediaUtil.getAmrDuration(CommunicationService.get().getFilesDir() + "/" + path);
                    if (duration > 0) {
                        int dura = (int) (duration.doubleValue() / 1000 + 0.5);
                        if (dura > Constants.VOICE_MAX_TIME) {
                            dura = Constants.VOICE_MAX_TIME;
                        }
                        chatMsg.setDuration(dura);
                    }
                } else if (ContType.MSG_SYS == msg.getType() || ContType.FENCE == msg.getType()) {
                    //系统消息 或 栅栏报警
                    chatMsg.setType(msg.getType().getType());
                    chatMsg.setContent(new String(msg.getData()));
                } else if (ContType.POSI_NOTIFY == msg.getType()) {
                    //定位通知
                    String position=new String(msg.getData());
                    AppContext.getEventBus().post(position,Constants.EVENT_TAG_POSI_NOTIFY);
                    chatMsg=null;
                    return;
                } else if (ContType.TERM_STATUS == msg.getType()){
                    AppContext.getEventBus().post(msg,Constants.EVENT_TAG_TERM_STATUS);
                    chatMsg=null;
                    return;
                } else if(ContType.BIND_NOTIFY == msg.getType()){
                    AppContext.getEventBus().post(msg,Constants.EVENT_TAG_TERM_BIND);
                    chatMsg=null;
                    return;
                } else if(ContType.OFFLINE_NOTIFY == msg.getType()){
                    Log.d(MessageReceiver.class.getName(),"收到离线通知,APP即将离线");
                    AppContext.getEventBus().post(msg,Constants.EVENT_TAG_OFFLINE_NOTIFY);
                    chatMsg=null;
                    return;
                } else if(ContType.AUDIO_STATUS == msg.getType()){
                    AppContext.getEventBus().post(msg,Constants.EVENT_TAG_AUDIO_STATUS);
                    chatMsg=null;
                    return;
                } else if(ContType.SOS_ALERT == msg.getType()){
                    //SOS定位消息
                    chatMsg.setType(msg.getType().getType());
                    chatMsg.setOriginalContent(new String(msg.getData()));
                } else{
                    Log.w(Constants.TAG,"忽略消息："+msg.toString());
                    chatMsg=null;
                    return;
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "保存文件或获取语音时长失败！", e);
            }
        }else{
            //高级图片，视频形式
            String data=new String(msg.getData());
            if(CommUtil.isNotBlank(data)){
                String[] cont=data.split("\n");
                if(ContType.PIC.equals(msg.getType())){
                    if(cont.length>0){
                        chatMsg.setRemoteOrgUrl(cont[0]);
                    }
                    if(cont.length>1){
                        chatMsg.setFileSize(Integer.parseInt(cont[1]));
                    }
                    if(cont.length>2){
                        chatMsg.setRemoteUrl(cont[2]);
                    }
                }else{
                    if(cont.length>0){
                        chatMsg.setRemoteUrl(cont[0]);
                    }
                    if(cont.length>1){
                        chatMsg.setFileSize(Integer.parseInt(cont[1]));
                    }
                    if (cont.length > 2 && CommUtil.isNotBlank(cont[2])) {
                        chatMsg.setDuration(Integer.parseInt(cont[2]));
                    }
                }
            }else{
                chatMsg=null;
                return;
            }
        }
        chatMsg.setTime(new Date().getTime());
        chatMsg.setIsSend(false);
        chatMsg.setIsRead(0);
        try {
            AppContext.db.saveBindingId(chatMsg);
        } catch (DbException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Constants.ACTION_NEW_MSG);
        intent.putExtra("id", chatMsg.getId());
        intent.putExtra("type", 1);
        CommunicationService.get().sendBroadcast(intent);
    }

    private int findPackageLen(byte[] bytes, int begin) {
        if (bytes == null || bytes.length < begin + 5) {
            return 0;
        }
        begin = findValidPackagePosition(bytes, begin);
        if (begin == INVALID_PACK_LEN) {
            //无效包
            return INVALID_PACK_LEN;
        }
        if ((bytes[begin + 3] & 0xFF) == 0xFF && (bytes[begin + 4] & 0xFF) == 0xFF) {
            //接收到心跳包
            return 5;
        }
        if ((bytes[begin + 2] & 0xF) == 0 && bytes.length >= begin + 13 && (bytes[begin + 11] & 0xFF) == 0xFF && (bytes[begin + 12] & 0xFF) == 0xFF) {
            //接收到的登录包
            return 13;
        }
        if ((bytes[begin + 2] >>> 4 & 0xF) == 1 && (bytes[begin + 2] & 0xF) == 2) {
            //回复传输，暂时设置为31，即2+1+22+4+2
            return 31;
        }
        int packLen = 0;
        if ((bytes[begin + 2] & 0xFF) == 0x02) {//传输包
            if (bytes.length < begin + 41) {
                return APPEND_PACK_LEN;
            }
            int bodyLen = ByteUtil.subBytesToInt(bytes, begin + 7, 2);
            int extLen = bytes[begin + 38];
            packLen = 41 + bodyLen + extLen;
            if (bytes.length < packLen + begin) {
                return APPEND_PACK_LEN;
            }
        }
        return packLen;
    }

    private int findValidPackagePosition(byte[] bytes, int begin) {
        if (bytes.length < 5) {
            return INVALID_PACK_LEN;
        }
        if ((bytes[begin] & 0xFF) == 0xFF && (bytes[begin + 1] & 0xFF) == 0xFF) {
            return begin;
        }
        for (int i = 0; i < bytes.length - 4; i++) {
            if ((bytes[begin + i] & 0xFF) == 0xFF && (bytes[begin + i + 1] & 0xFF) == 0xFF) {
                return begin + i;
            }
        }
        return INVALID_PACK_LEN;
    }

    private void processReponse(byte[] bytes, int begin) {
        if (bytes.length - begin > 27) {
            String name = ByteUtil.subBytesToHex(bytes, begin + 3, 22);
            int end = CommUtil.min(bytes.length - 2, begin + 57);
            for (int i = begin + 25; i < end; i++) {
                if ((bytes[i] & 0xFF) == 0xFF && (bytes[i + 1] & 0xFF) == 0xFF) {//一直找到结尾
                    end = i;
                }
            }
            byte[] resendBytes = new byte[end - begin - 25];
            System.arraycopy(bytes, begin + 25, resendBytes, 0, resendBytes.length);
            MessageSender.getInstance().reSendPackage(name, resendBytes);
        } else {
            byteFragment = new byte[bytes.length - begin];
            System.arraycopy(bytes, begin, byteFragment, 0, byteFragment.length);
        }
    }
}
