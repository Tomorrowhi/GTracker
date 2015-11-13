package com.bct.gpstracker.vo;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 上午 11:02
 */
public class Msg {
    private String from;
    private String self;
    private String name;
    private ContType type;
    private byte[] data;
    private String filePath;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContType getType() {
        return type;
    }

    public void setType(ContType type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Msg{");
        sb.append("from='").append(from).append('\'');
        sb.append(", self='").append(self).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append(", data=");
        if (data == null || data.length == 0) {
            sb.append("null");
        } else if (type == ContType.AUDIO || type == ContType.VIDEO || type == ContType.FILE || type == ContType.PIC) {
            sb.append("[Media Type]");
        } else {
            sb.append('[');
            sb.append(new String(data));
            sb.append(']');
        }
        sb.append(", filePath='").append(filePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
