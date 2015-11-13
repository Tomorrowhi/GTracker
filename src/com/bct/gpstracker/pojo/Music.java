package com.bct.gpstracker.pojo;

/**
 * Created by HH
 * Date: 2015/8/27 0027
 * Time: 上午 10:22
 */
public class Music {
    private Integer id;
    private Integer serial;
    private String url;
    private String name;
    private String singer;
    private Integer duration;
    private Integer fileSize;//文件大小，byte
    private Integer status=1;//0 下载中，1 成功，2 失败
    private Long userId;
    private String portraitUrl;
    private boolean playing=false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Music{");
        sb.append("id=").append(id);
        sb.append(", serial=").append(serial);
        sb.append(", url='").append(url).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", singer='").append(singer).append('\'');
        sb.append(", duration=").append(duration);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", status=").append(status);
        sb.append(", userId=").append(userId);
        sb.append(", portraitUrl='").append(portraitUrl).append('\'');
        sb.append(", playing=").append(playing);
        sb.append('}');
        return sb.toString();
    }
}
