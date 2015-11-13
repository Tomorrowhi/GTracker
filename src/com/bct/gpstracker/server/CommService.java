package com.bct.gpstracker.server;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;

/**
 * Created by HH
 * Date: 2015/7/31 0031
 * Time: 下午 3:58
 */
public class CommService {

    private static CommService commService;
    public synchronized static CommService get(){
        if(commService==null){
            commService=new CommService();
        }
        return commService;
    }
    private CommService(){}

    /**
     *
     * @param context
     * @param imei
     * @param command
     * @param content
     * @param callback
     * <pre>
     * 为保持代码简洁，也可使用如下方式：<br />
     * Cmd cmd = new Cmd(CmdType.YS.getType(), cmdcont, imei,callback);<br />
     * CommUtil.sendMsg(CommHandler.SEND_COMMAND, cmd);
     * </pre>
     */
    public  void sendCommand(Context context,String imei,String command,String content, final BctClientCallback callback){
        try {
            JSONObject json = new JSONObject();
//            json.put("imei",imei);
            json.put("type", command);
            if (CommUtil.isNotBlank(content)) {
                json.put("content", content);
            }

            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setId(-1L);
            chatMsg.setType(ContType.CMD.getType());
            chatMsg.setImei(imei);
            chatMsg.setContent(json.toString());
            chatMsg.setIsSend(false);
            chatMsg.setTermType(Session.getInstance().getMapEntityByImei(imei).getTermType().getType());
            chatMsg.setUserId(Session.getInstance().getLoginedUserId());
            chatMsg.setTime(System.currentTimeMillis());
            chatMsg.setSucc(1);

            AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_SEND);

            if (callback != null) {
                ResponseData responseData = new ResponseData();
                responseData.setRetcode(1);
                callback.onSuccess(responseData);
            }
//            BctClient.getInstance().POST(context, CommonRestPath.sendCommand(), json, new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception e){
            Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
        }
    }
}
