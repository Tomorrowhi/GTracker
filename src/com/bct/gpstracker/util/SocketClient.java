package com.bct.gpstracker.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import android.util.Log;

import com.bct.gpstracker.common.Constants;

/**
 * Created by HH
 * Date: 2015/7/20 0020
 * Time: 下午 3:14
 */
public class SocketClient {
    private Socket clientSocket;

    private InetSocketAddress tcpAddress;

    private int timeOut = 1000;  //超时设置, 默认一分钟

    private OutputStream out;

    private InputStream in;

    private Date sendTime;

    // 设置一次接收数据的大小, 如果不设置,默认为1M
//    private final int receiveMaxSize = 2048;

    //客户端唯一标识号码, 类似于HTTP的Session
    private long clientID = -1;

    public SocketClient(String ip, int port)
    {
        tcpAddress = new InetSocketAddress(ip,port);
    }

    /**
     * 设置超时时间, 客户端必备良药, 否则, 服务端不反馈, 就需要等到天荒地老了, 因为这个是阻塞的模式
     * @param tm
     */
    public void setTimeOut(int tm)
    {
        timeOut = tm;
    }


    /**
     * 对服务端口的连接
     * @return true成功, false表示失败
     */
    public boolean connect()
    {
        try
        {
            if(clientSocket != null && isConnected())
            {
                return true;
            }
            clientSocket = new Socket();
            clientSocket.connect(tcpAddress);
            clientSocket.setKeepAlive(true);

            if( clientSocket.isConnected() )
            {
                clientSocket.setSoTimeout(timeOut);
                out = clientSocket.getOutputStream();
                in = clientSocket.getInputStream();
                Log.i(Constants.TAG,"连接通讯服务器成功！ IP:"+tcpAddress.getAddress().getHostAddress()+" "+tcpAddress.getPort());
                return true;
            }
        }
        catch(Exception ex)
        {
            //Log.e(Constants.TAG,"连接通讯服务器出错！",ex);
            clientSocket = null;
            out = null;
            in = null;
        }
        return false;
    }

    public boolean isConnected(){
        return clientSocket!=null&&clientSocket.isConnected()&&!clientSocket.isClosed()&&!clientSocket.isInputShutdown()&&!clientSocket.isOutputShutdown();
    }

    /**
     * 发送特定编码的String到服务端
     * @param sendString
     * @param charset 指定的编码
     * @throws Exception
     */
    public boolean send(String sendString, String charset)
    {

        try {
            byte[] datas = sendString.getBytes(charset);
            return send(datas);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }


    /**
     * 发送
     * @param datas
     * @throws Exception
     */
    public boolean send(byte[] datas)
    {
        if(!isConnected())
        {
            return false;
        }
        try {
            out.write(datas);
            out.flush();
            sendTime = new Date(System.currentTimeMillis()); //获取当前时间);
            return true;
        } catch (Exception e) {
            Log.e(Constants.TAG,"发送数据出错",e);
        }
        return false;
    }

    /**
     * 接收Server端的信息, 并且以特定编码的String 保存
     * 最佳的方式, 是将另外一个receive方法包装, 避免维护过多的接口.
     * @param charset
     * @return
     * @throws Exception
     */
    public String receive(String charset)
    {
        try {
            byte[] receiveData = receive();
            if (receiveData == null)
            {
                return null;
            }
            String sData = new String(receiveData, charset);
            return sData.trim();
        } catch (Exception e) {
            Log.e(Constants.TAG,"接收数据出错：",e);
            return null;
        }
    }



    /**
     * 接收指定位置的信息, 并用byte[]的方式存储.
     * 实质上什么时候结束一条消息, 是跟业务相关.
     * @return
     * @throws Exception
     */
    public byte[] receive() throws Exception
    {
        if( null == clientSocket || clientSocket.isClosed())
        {
            connect();
        }
//        byte[] buff = new byte[1024];
//        ByteArrayOutputStream bout=new ByteArrayOutputStream();
//        int rc;
//        while((rc=in.read(buff,0,1024))>0){
//            bout.write(buff,0,rc);
//        }
//        byte[] stores=bout.toByteArray();

        int bytesLen = in.available();
        int totalCount = 0;
        byte[] buff = new byte[bytesLen];
        while( bytesLen > 0)
        {
            try
            {
                bytesLen = in.read(buff, totalCount, bytesLen);
                totalCount += bytesLen;
                bytesLen = in.available();
                if(bytesLen>0){
                    byte[] tp=new byte[buff.length+bytesLen];
                    System.arraycopy(buff,0,tp,0,buff.length);
                    buff=tp;
                }
            }catch(Exception e)
            {
                //超时抛出异常, 这样可以中断read阻塞
                break;
            }
        }
//        byte[] stores = new byte[totalCount];
//        System.arraycopy(buff, 0, stores, 0, totalCount);
//        Log.d(Constants.TAG, "获取到数据长度：" + buff.length);
        return buff;
    }

    /**
     * 得到发送时间
     * @return
     */
    public Date getSendTime() {
        return sendTime;
    }

    public void close()
    {
        try {
            if(clientSocket!=null) {
                clientSocket.close();
            }
            if(in!=null) {
                in.close();
            }
            if(out!=null) {
                out.close();
            }

        } catch (Exception e) {
            //
        }
    }

    public long getClientID() {
        return clientID;
    }

    public void setClientID(long clientID) {
        this.clientID = clientID;
    }


    /**
     * 比较特殊的接收方式, 比如按照状态机的接收方式,  需要将该接口暴露在外面, 留给外部较大的自由度
     * 原因是大部分数据接收是分批次, 并且有结束标示
     * @return
     */
    public InputStream getInputStream()
    {
        return in;
    }


    public OutputStream getOutputStream()
    {
        return out;
    }

}
