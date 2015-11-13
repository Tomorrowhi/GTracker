package com.bct.gpstracker.vo;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 上午 11:04
 */
public enum ContType {
    IGONRE(-1), TXT(0x00), PIC(0x01), AUDIO(0x02), VIDEO(0x03), FILE(0x04),
    MSG_SYS(0x05), POSI_NOTIFY(0x06), TERM_STATUS(0x07), CMD(0x08),
    BIND_NOTIFY(0x09), OFFLINE_NOTIFY(0x0A), AUDIO_STATUS(0x0B),
    SOS_ALERT(0x0C), FENCE(0x0D);

    private int type;

    ContType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static boolean isTxtType(ContType type) {
        return type == TXT || type == MSG_SYS || type == SOS_ALERT || type == FENCE;
    }

    public static boolean isSysType(ContType type) {
        return type == MSG_SYS || type == SOS_ALERT || type == FENCE;
    }

    public static ContType getType(int type) {
        for (ContType tp : ContType.values()) {
            if (tp.getType() == type) {
                return tp;
            }
        }
        return IGONRE;
    }
}
