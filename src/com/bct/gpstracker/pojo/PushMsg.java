package com.bct.gpstracker.pojo;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by HH
 * Date: 2015/6/29 0029
 * Time: 下午 4:53
 */
@Table(name="push_msg")
public class PushMsg {
    @Column(column = "id")
    private Long id;
    @Column(column = "type")
    private Integer type;
    @Column(column = "title")
    private String title;
    @Column(column = "msg")
    private String msg;
    @Column(column = "imei")
    private String imei;
    @Column(column = "upload_time")
    private Long uploadTime;
    @Column(column = "create_time")
    private Long createTime;
    @Column(column = "msg_state")
    private int msgState = 0; // 判断是否已读状态,0未读,1已读

    public PushMsg() {
    }

    public PushMsg(Integer type, String title, String msg,String imei, Long uploadTime) {
        this.type = type;
        this.title = title;
        this.msg = msg;
        this.imei=imei;
        this.uploadTime = uploadTime;
        this.createTime=System.currentTimeMillis();
    }

    public PushMsg(Integer type, String title, String msg, Long uploadTime,int msgState) {
        this.type = type;
        this.title = title;
        this.msg = msg;
        this.uploadTime = uploadTime;
        this.createTime=System.currentTimeMillis();
        this.msgState = msgState;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getMsgState() {  return msgState;  }

    public void setMsgState(int msgState) { this.msgState = msgState; }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushMsg pushMsg = (PushMsg) o;
        return (id != null ? id.equals(pushMsg.id) : pushMsg.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        return result;
    }
}
