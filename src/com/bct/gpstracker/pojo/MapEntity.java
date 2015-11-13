package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

import com.bct.gpstracker.vo.TermType;

public class MapEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2844510703649067275L;
    private double longitude;
    private double latitude;
    private String name;
    private String phone;
    private String imei;
    private int signal;
    private double electricity;
    private int positioning;
    private int status;
    private String datetime;
    private String createTime;
    private String voiceUrl;
    private String address;
    private String distance;
    private TermType termType;
    private String portrait;

    public MapEntity() {
    }

    ;

    public MapEntity(JSONObject json) {
        if (json == null) {
            termType = TermType.APP;
            return;
        } else {
            termType = TermType.WATCH;
        }
        try {
            if (json.has("longitude")) longitude = json.getDouble("googlelng");
            if (json.has("latitude")) latitude = json.getDouble("googlelat");
            if (json.has("name")) name = json.getString("name");
            if (json.has("phone")) phone = json.getString("phone");
            if (json.has("imei")) imei = json.getString("imei");
            if (json.has("signal")) signal = json.getInt("signal");
            if (json.has("electricity")) electricity = json.getDouble("electricity");
            if (json.has("positioning")) positioning = json.getInt("positioning");
            if (json.has("status")) status = json.getInt("status");
            if (json.has("datetime")) datetime = json.getString("datetime");
            if (json.has("createtime")) createTime = json.getString("createtime");
            if (json.has("portrait")) portrait = json.getString("portrait");
//		    if(json.has("createtime")) createTime= Utils.getDayTime(json.getString("createtime"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public double getElectricity() {
        return electricity;
    }

    public void setElectricity(double electricity) {
        this.electricity = electricity;
    }

    public int getPositioning() {
        return positioning;
    }

    public void setPositioning(int positioning) {
        this.positioning = positioning;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
