package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * @author 作者 E-mail:黄飞  353240166@qq.com
 * @version 创建时间：2015年2月7日 下午4:18:08
 * 类说明:历史轨迹实体
 */
public class HistoryTrack implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 336239814119950870L;
	/**
	 *                     "longitude": 33.2312343, 
                    "latitude": 122.2234123, 
                    "baidulng": 33.2312343, 
                    "baidulat": 122.2234123, 
                    "googlelng": 33.2312343, 
                    "googlelat": 122.2234123, 
                    "datetime": "2014-01-01 10:35:05", 
                    "createtime": "2014-01-01 13:45:05"

	 */
	private double longitude;
	private double latitude;
	private double baidulng;
	private double baidulat;
	private double googlelng;
	private double googlelat;
	private String dateTime;
	private String createTime;
	
	public HistoryTrack(){};
	public HistoryTrack(JSONObject json){
		try {
			if(json.has("longitude")) longitude = json.getDouble("longitude");
			if(json.has("latitude")) latitude = json.getDouble("latitude");
			if(json.has("baidulng")) baidulng = json.getDouble("baidulng");
			if(json.has("baidulat")) baidulat = json.getDouble("baidulat");
			if(json.has("googlelng")) googlelng = json.getDouble("googlelng");
			if(json.has("googlelat")) googlelat = json.getDouble("googlelat");
			if(json.has("datetime")) dateTime = json.getString("datetime");
			if(json.has("createtime")) createTime = json.getString("createtime");
			
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
	public double getBaidulng() {
		return baidulng;
	}
	public void setBaidulng(double baidulng) {
		this.baidulng = baidulng;
	}
	public double getBaidulat() {
		return baidulat;
	}
	public void setBaidulat(double baidulat) {
		this.baidulat = baidulat;
	}
	public double getGooglelng() {
		return googlelng;
	}
	public void setGooglelng(double googlelng) {
		this.googlelng = googlelng;
	}
	public double getGooglelat() {
		return googlelat;
	}
	public void setGooglelat(double googlelat) {
		this.googlelat = googlelat;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
}
