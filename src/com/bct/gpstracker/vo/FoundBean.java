package com.bct.gpstracker.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Admin on 2015/8/6 0006.
 * 发现的Bean对象
 */
public class FoundBean implements Parcelable {


    public String distance;
    public String lat;
    public String lng;
    public String portrait;
    public String publishContent;
    public String publishId;
    public String publishTime;
    public String userName;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.distance);
        dest.writeString(this.lat);
        dest.writeString(this.lng);
        dest.writeString(this.portrait);
        dest.writeString(this.publishContent);
        dest.writeString(this.publishId);
        dest.writeString(this.publishTime);
        dest.writeString(this.userName);
    }

    public FoundBean() {
    }

    protected FoundBean(Parcel in) {
        this.distance = in.readString();
        this.lat = in.readString();
        this.lng = in.readString();
        this.portrait = in.readString();
        this.publishContent = in.readString();
        this.publishId = in.readString();
        this.publishTime = in.readString();
        this.userName = in.readString();
    }

    public static final Creator<FoundBean> CREATOR = new Creator<FoundBean>() {
        public FoundBean createFromParcel(Parcel source) {
            return new FoundBean(source);
        }

        public FoundBean[] newArray(int size) {
            return new FoundBean[size];
        }
    };
}
