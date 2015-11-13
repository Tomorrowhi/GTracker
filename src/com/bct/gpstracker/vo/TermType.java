package com.bct.gpstracker.vo;

/**
 * Created by HH
 * Date: 2015/7/23 0023
 * Time: 上午 9:26
 */
public enum TermType {
    WATCH(0),VEHICLE(1),BRACELET(2),APP(3);

    private Integer type;

    TermType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
