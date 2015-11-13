package com.bct.gpstracker.vo;

/**
 * Created by Admin on 2015/8/11 0011.
 */
public class CommentBean {
    public int id;  //评论级别
    public String commTime;
    public String commentId;
    public String commentContent;
    public String portrait;
    public String replyMsg;
    public String userName;
    public String fatherCommentID;

    public String getFatherCommentID() {
        return fatherCommentID;
    }

    public void setFatherCommentID(String fatherCommentID) {
        this.fatherCommentID = fatherCommentID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommTime() {
        return commTime;
    }

    public void setCommTime(String commTime) {
        this.commTime = commTime;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getReplyMsg() {
        return replyMsg;
    }

    public void setReplyMsg(String replyMsg) {
        this.replyMsg = replyMsg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
