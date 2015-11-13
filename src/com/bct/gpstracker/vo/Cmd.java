package com.bct.gpstracker.vo;

import com.bct.gpstracker.inter.BctClientCallback;

/**
 * Created by HH
 * Date: 2015/7/31 0031
 * Time: 下午 4:28
 */
public class Cmd {
    /**
     * 指令类型
     */
    private String type;
    /**
     * 指令内容，相当于后台的ti参数，很多指令不用填这个
     */
    private String cont;
    /**
     * 终端的IMEI
     */
    private String imei;

    /**
     * 自定义回调
     */
    private BctClientCallback callback;

    /**
     * 隐藏响应消息显示
     */
    private boolean hideMessage=false;

    public Cmd() {
    }

    public Cmd(String type, String cont, String imei) {
        this.type = type;
        this.cont = cont;
        this.imei = imei;
    }

    public Cmd(String type, String cont, String imei, BctClientCallback callback) {
        this.type = type;
        this.cont = cont;
        this.imei = imei;
        this.callback = callback;
    }

    public Cmd(String type, String cont, String imei, boolean hideMessage) {
        this.type = type;
        this.cont = cont;
        this.imei = imei;
        this.hideMessage = hideMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCont() {
        return cont;
    }

    public void setCont(String cont) {
        this.cont = cont;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public BctClientCallback getCallback() {
        return callback;
    }

    public void setCallback(BctClientCallback callback) {
        this.callback = callback;
    }

    public boolean isHideMessage() {
        return hideMessage;
    }

    public void setHideMessage(boolean hideMessage) {
        this.hideMessage = hideMessage;
    }
}
