package com.bct.gpstracker.pojo;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 下午 5:19
 */
@Table(name="chat_msg")
public class ChatMsg {
    @Column(column = "id")
    private Long id;
    /**
     *见 com.bct.gpstracker.vo.ContType
     */
    @Column(column = "type")
    private Integer type;

    @Column(column = "user_id")
    private Long userId;
    @Column(column = "user_name")
    private String userName;
    @Column(column = "local_url")
    private String localUrl;
    @Column(column = "content")
    private String content;
    @Column(column = "time")
    private Long time;
    @Column(column = "imei")
    private String imei;
    @Column(column = "from_user")
    private Long fromUser;
    @Column(column = "is_send")
    private Boolean isSend;
    @Column(column = "term_type")
    private Integer termType;
    @Column(column = "remote_url")
    private String remoteUrl;
    @Column(column = "duration")
    private Integer duration;

    /**
     * 0 失败 ，1 成功
     */
    @Column(column = "succ")
    private Integer succ;
    @Column(column = "remote_org_url")
    private String remoteOrgUrl;
    @Column(column = "local_org_url")
    private String localOrgUrl;
    @Column(column = "file_size")
    private Integer fileSize;

    /**
     * Icon Res Id
     */
    @Column(column = "icon")
    private int icon;
    @Column(column = "title")
    private String title;

    /**
     * msg is_read 0、null未读，1已读
     * @return
     */
    @Column(column = "is_read")
    private Integer isRead;

    @Transient
    private String originalContent;


    public Integer getIsRead() {
        return isRead;
    }
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getTermType() {
        return termType;
    }

    public void setTermType(Integer termType) {
        this.termType = termType;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Long getFromUser() {
        return fromUser;
    }

    public void setFromUser(Long fromUser) {
        this.fromUser = fromUser;
    }

    public Boolean getIsSend() {
        return isSend;
    }

    public void setIsSend(Boolean isSend) {
        this.isSend = isSend;
    }

    public Integer getSucc() {
        return succ;
    }

    public void setSucc(Integer succ) {
        this.succ = succ;
    }

    public String getRemoteOrgUrl() {
        return remoteOrgUrl;
    }

    public void setRemoteOrgUrl(String remoteOrgUrl) {
        this.remoteOrgUrl = remoteOrgUrl;
    }

    public String getLocalOrgUrl() {
        return localOrgUrl;
    }

    public void setLocalOrgUrl(String localOrgUrl) {
        this.localOrgUrl = localOrgUrl;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ChatMsg{");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", userId=").append(userId);
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", localUrl='").append(localUrl).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", time=").append(time);
        sb.append(", imei='").append(imei).append('\'');
        sb.append(", fromUser=").append(fromUser);
        sb.append(", isSend=").append(isSend);
        sb.append(", termType=").append(termType);
        sb.append(", remoteUrl='").append(remoteUrl).append('\'');
        sb.append(", duration=").append(duration);
        sb.append(", succ=").append(succ);
        sb.append(", remoteOrgUrl='").append(remoteOrgUrl).append('\'');
        sb.append(", localOrgUrl='").append(localOrgUrl).append('\'');
        sb.append(", fileSize=").append(fileSize);
        sb.append(", icon=").append(icon);
        sb.append(", title='").append(title).append('\'');
        sb.append(", isRead=").append(isRead);
        sb.append(", originalContent='").append(originalContent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
