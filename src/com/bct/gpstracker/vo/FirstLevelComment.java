package com.bct.gpstracker.vo;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Admin on 2015/8/11 0011.
 */
public class FirstLevelComment implements Parcelable {
    public String commTime;
    public String commentContent;
    public String commentId;
    public String replyMsg;
    public String portrait;
    public List<SecondLevelComment> secondLevelComment;
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

    public List<SecondLevelComment> getSecondLevelComment() {
        return secondLevelComment;
    }

    public void setSecondLevelComment(List<SecondLevelComment> secondLevelComment) {
        this.secondLevelComment = secondLevelComment;
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
        dest.writeString(this.commentContent);
        dest.writeString(this.commentId);
        dest.writeString(this.replyMsg);
        dest.writeString(this.portrait);
        dest.writeList(this.secondLevelComment);
        dest.writeString(this.userName);
    }

    public FirstLevelComment() {
    }

    protected FirstLevelComment(Parcel in) {
        this.commTime = in.readString();
        this.commentContent = in.readString();
        this.commentId = in.readString();
        this.replyMsg = in.readString();
        this.portrait = in.readString();
        this.secondLevelComment = new ArrayList<SecondLevelComment>();
        in.readList(this.secondLevelComment, SecondLevelComment.class.getClassLoader());
        this.userName = in.readString();
    }

    public static final Creator<FirstLevelComment> CREATOR = new Creator<FirstLevelComment>() {
        public FirstLevelComment createFromParcel(Parcel source) {
            return new FirstLevelComment(source);
        }

        public FirstLevelComment[] newArray(int size) {
            return new FirstLevelComment[size];
        }
    };
}
