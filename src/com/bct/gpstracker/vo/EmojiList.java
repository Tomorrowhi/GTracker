package com.bct.gpstracker.vo;

/**
 * Created by Admin on 2015/9/18 0018.
 * 表情列表
 */
public class EmojiList {

    private int emoteId;
    private String emoteCode;
    private String emoteName;
    private String emoteUrl;
    private int downloaded;
    private boolean selectItem;

    public boolean isSelectItem() {
        return selectItem;
    }

    public void setSelectItem(boolean selectItem) {
        this.selectItem = selectItem;
    }

    public int getEmoteId() {
        return emoteId;
    }

    public void setEmoteId(int emoteId) {
        this.emoteId = emoteId;
    }

    public String getEmoteCode() {
        return emoteCode;
    }

    public void setEmoteCode(String emoteCode) {
        this.emoteCode = emoteCode;
    }

    public String getEmoteName() {
        return emoteName;
    }

    public void setEmoteName(String emoteName) {
        this.emoteName = emoteName;
    }

    public String getEmoteUrl() {
        return emoteUrl;
    }

    public void setEmoteUrl(String emoteUrl) {
        this.emoteUrl = emoteUrl;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }
}
