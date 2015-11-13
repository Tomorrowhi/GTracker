package com.bct.gpstracker.vo;

/**
 * Created by HH
 * Date: 2015/7/15 0015
 * Time: 下午 2:13
 */
public enum MsgType {
    SYS_MSG(1),WARN_MSG(2),TERM_FEE(3),OTHER(4);

    private Integer value;
    MsgType(Integer val) {
        value=val;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
