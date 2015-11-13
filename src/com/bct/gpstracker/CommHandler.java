package com.bct.gpstracker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.bct.gpstracker.dialog.CommLoadingDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.Cmd;
import com.bct.gpstracker.vo.DialogConfig;

/**
 * Created by hh on 2015/7/26 0026.
 *
 */
public class CommHandler extends Handler {
    private static Context context;
    private static CommHandler commHandler;

    public static final int TOAST_SHORT = 0;
    public static final int TOAST_LONG = 1;
    public static final int CLOSE_DIALOG = 2;
    public static final int SHOW_DIALOG = 3;
    public static final int SEND_COMMAND = 4;

    private CommHandler() {
        context = AppContext.getContext();
    }

    public synchronized static CommHandler getHandler() {
        if (commHandler == null) {
            commHandler = new CommHandler();
        }
        return commHandler;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TOAST_SHORT:
                Toast.makeText(context, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
                break;
            case TOAST_LONG:
                Toast.makeText(context, String.valueOf(msg.obj), Toast.LENGTH_LONG).show();
                break;
            case SHOW_DIALOG:
                DialogConfig dc = (DialogConfig) msg.obj;
                CommLoadingDialog.getDialog().show(dc.getView().getContext(), dc.getView(), dc.isCanCancel(), dc.isModal());
                break;
            case CLOSE_DIALOG:
                CommLoadingDialog.getDialog().close();
                break;
            case SEND_COMMAND:
                sendCommand(msg.obj);
                break;
            default:
                break;
        }
    }

    private void sendCommand(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Cmd) {
            final Cmd cmd = (Cmd) obj;
            BctClientCallback callback=cmd.getCallback();
            if(callback==null) {
                callback = new BctClientCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onSuccess(ResponseData obj) {
                        if (!cmd.isHideMessage() && CommUtil.isNotBlank(obj.getMsg())) {
                            CommUtil.sendMsg(CommHandler.TOAST_SHORT, obj.getMsg());
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        if (CommUtil.isNotBlank(message)) {
                            CommUtil.sendMsg(CommHandler.TOAST_SHORT, message);
                        }
                    }
                };
            }
            CommService.get().sendCommand(AppContext.getContext(), cmd.getImei(), cmd.getType(), cmd.getCont(), callback);
        }
    }
}
