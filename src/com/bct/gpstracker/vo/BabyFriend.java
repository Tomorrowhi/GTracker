package com.bct.gpstracker.vo;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by TAoTAo-PC on 2015/7/29.
 */
public class BabyFriend implements Parcelable{

    public List<FirstLevelComment> firstLevelComment;
    public String portrait;
    public String publishContent;
    public String publishId;
    public String publishTime;
    public String userName;
    public String publishPath;

    public String getPublishPath() {
        return publishPath;
    }

    public void setPublishPath(String publishPath) {
        this.publishPath = publishPath;
    }

    public List<FirstLevelComment> getFirstLevelComment() {
        return firstLevelComment;
    }

    public void setFirstLevelComment(List<FirstLevelComment> firstLevelComment) {
        this.firstLevelComment = firstLevelComment;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getPublishContent() {
        return publishContent;
    }

    public void setPublishContent(String publishContent) {
        this.publishContent = publishContent;
    }

    public String getPublishId() {
        return publishId;
    }

    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BabyFriend() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(firstLevelComment);
        dest.writeString(this.portrait);
        dest.writeString(this.publishContent);
        dest.writeString(this.publishId);
        dest.writeString(this.publishTime);
        dest.writeString(this.userName);
        dest.writeString(this.publishPath);
    }

    protected BabyFriend(Parcel in) {
        this.firstLevelComment = in.createTypedArrayList(FirstLevelComment.CREATOR);
        this.portrait = in.readString();
        this.publishContent = in.readString();
        this.publishId = in.readString();
        this.publishTime = in.readString();
        this.userName = in.readString();
        this.publishPath = in.readString();
    }

    public static final Creator<BabyFriend> CREATOR = new Creator<BabyFriend>() {
        public BabyFriend createFromParcel(Parcel source) {
            return new BabyFriend(source);
        }

        public BabyFriend[] newArray(int size) {
            return new BabyFriend[size];
        }
    };
}



