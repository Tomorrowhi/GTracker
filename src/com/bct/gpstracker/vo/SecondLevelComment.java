package com.bct.gpstracker.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Admin on 2015/8/11 0011.
 */
public class SecondLevelComment implements Parcelable{
    public String commTime;
    public String commentId;
    public String commentContent;
    public String portrait;
    public String replyMsg;
    public String userName;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.commTime);
        dest.writeString(this.commentId);
        dest.writeString(this.commentContent);
        dest.writeString(this.portrait);
        dest.writeString(this.replyMsg);
        dest.writeString(this.userName);
    }

    public SecondLevelComment() {
    }

    protected SecondLevelComment(Parcel in) {
        this.commTime = in.readString();
        this.commentId = in.readString();
        this.commentContent = in.readString();
        this.portrait = in.readString();
        this.replyMsg = in.readString();
        this.userName = in.readString();
    }

    public static final Creator<SecondLevelComment> CREATOR = new Creator<SecondLevelComment>() {
        public SecondLevelComment createFromParcel(Parcel source) {
            return new SecondLevelComment(source);
        }

        public SecondLevelComment[] newArray(int size) {
            return new SecondLevelComment[size];
        }
    };
}
