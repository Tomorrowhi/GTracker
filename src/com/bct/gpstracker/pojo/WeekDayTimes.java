package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * 围栏有效时间实体
 * @author huangfei
 *
 */
public class WeekDayTimes implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6981839018947220100L;
	
	/**
	 * "mondayStartTime":”07:10”,
		"mondayEndTime":”17:00”,
		"tuesdayStartTime":”07:10”,
		"tuesdayEndTime":”17:00”,
		"wednesdayStartTime":”07:10”,
		"wednesdayEndTime":”17:00”,
		"thursdayStartTime":””,
		"thursdayEndTime":””,
		"fridayStartTime":”07:10”,
		"fridayEndTime":”17:00”,
		"saturdayStartTime":””,
		"saturdayEndTime":””,
		"sundayStartTime":””,
		"sundayEndTime":””
	 */
	private String mondayST;
	private String mondayET;
	private String tuesdayST;
	private String tuesdayET;
	private String wednesdayST;
	private String wednesdayET;
	private String thursdayST;
	private String thursdayET;
	private String fridayST;
	private String fridayET;
	private String saturdayST;
	private String saturdayET;
	private String sundayST;
	private String sundayET;
	
	public WeekDayTimes(){};
	public WeekDayTimes(JSONObject json){
		try {
			if(json.has("mondayStartTime")) mondayST = json.getString("mondayStartTime");
			if(json.has("mondayEndTime")) mondayET = json.getString("mondayEndTime");
			if(json.has("tuesdayStartTime")) tuesdayST = json.getString("tuesdayStartTime");
			if(json.has("tuesdayEndTime")) tuesdayET = json.getString("tuesdayEndTime");
			if(json.has("wednesdayStartTime")) wednesdayST = json.getString("wednesdayStartTime");
			if(json.has("wednesdayEndTime")) wednesdayET = json.getString("wednesdayEndTime");
			if(json.has("thursdayStartTime")) thursdayST = json.getString("thursdayStartTime");
			if(json.has("thursdayEndTime")) thursdayET = json.getString("thursdayEndTime");
			if(json.has("fridayStartTime")) fridayST = json.getString("fridayStartTime");
			if(json.has("fridayEndTime")) fridayET = json.getString("fridayEndTime");
			if(json.has("saturdayStartTime")) saturdayST = json.getString("saturdayStartTime");
			if(json.has("saturdayEndTime")) saturdayET = json.getString("saturdayEndTime");
			if(json.has("sundayStartTime")) sundayST = json.getString("sundayStartTime");
			if(json.has("sundayEndTime")) sundayET = json.getString("sundayEndTime");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getMondayST() {
		return mondayST;
	}
	public void setMondayST(String mondayST) {
		this.mondayST = mondayST;
	}
	public String getMondayET() {
		return mondayET;
	}
	public void setMondayET(String mondayET) {
		this.mondayET = mondayET;
	}
	public String getTuesdayST() {
		return tuesdayST;
	}
	public void setTuesdayST(String tuesdayST) {
		this.tuesdayST = tuesdayST;
	}
	public String getTuesdayET() {
		return tuesdayET;
	}
	public void setTuesdayET(String tuesdayET) {
		this.tuesdayET = tuesdayET;
	}
	public String getWednesdayST() {
		return wednesdayST;
	}
	public void setWednesdayST(String wednesdayST) {
		this.wednesdayST = wednesdayST;
	}
	public String getWednesdayET() {
		return wednesdayET;
	}
	public void setWednesdayET(String wednesdayET) {
		this.wednesdayET = wednesdayET;
	}
	public String getThursdayST() {
		return thursdayST;
	}
	public void setThursdayST(String thursdayST) {
		this.thursdayST = thursdayST;
	}
	public String getThursdayET() {
		return thursdayET;
	}
	public void setThursdayET(String thursdayET) {
		this.thursdayET = thursdayET;
	}
	public String getFridayST() {
		return fridayST;
	}
	public void setFridayST(String fridayST) {
		this.fridayST = fridayST;
	}
	public String getFridayET() {
		return fridayET;
	}
	public void setFridayET(String fridayET) {
		this.fridayET = fridayET;
	}
	public String getSaturdayST() {
		return saturdayST;
	}
	public void setSaturdayST(String saturdayST) {
		this.saturdayST = saturdayST;
	}
	public String getSaturdayET() {
		return saturdayET;
	}
	public void setSaturdayET(String saturdayET) {
		this.saturdayET = saturdayET;
	}
	public String getSundayST() {
		return sundayST;
	}
	public void setSundayST(String sundayST) {
		this.sundayST = sundayST;
	}
	public String getSundayET() {
		return sundayET;
	}
	public void setSundayET(String sundayET) {
		this.sundayET = sundayET;
	}
	
	
}
