package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

public class MessageSwitchEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2614050339964038066L;
	
	/**
	 * "sos": 1, 
        "fall": 0,
        "lowBatt": 0,
        "fence": 0,
        "wifi": 1

	 */
	private int sos;//紧急报警
	private int fall;//跌倒报警
	private int lowBatt;//低电报警
	private int fence;//电子围栏
	private int wifi;//回家离家
    private int down;//拆卸报警
    private int vibrate;//震动报警
    private int shift;//位移报警
	
	public MessageSwitchEntity(){};
	public MessageSwitchEntity(JSONObject json){
		try {
			if(json.has("sos")) sos = json.optInt("sos");
			if(json.has("fall")) fall = json.optInt("fall");
			if(json.has("lowBatt")) lowBatt = json.optInt("lowBatt");
			if(json.has("fence")) fence = json.optInt("fence");
			if(json.has("wifi")) wifi = json.optInt("wifi");
			if(json.has("down")) down = json.optInt("down");
			if(json.has("vibrate")) vibrate = json.optInt("vibrate");
			if(json.has("shift")) shift = json.optInt("shift");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getSos() {
		return sos;
	}
	public void setSos(int sos) {
		this.sos = sos;
	}
	public int getFall() {
		return fall;
	}
	public void setFall(int fall) {
		this.fall = fall;
	}
	public int getLowBatt() {
		return lowBatt;
	}
	public void setLowBatt(int lowBatt) {
		this.lowBatt = lowBatt;
	}
	public int getFence() {
		return fence;
	}
	public void setFence(int fence) {
		this.fence = fence;
	}
	public int getWifi() {
		return wifi;
	}
	public void setWifi(int wifi) {
		this.wifi = wifi;
	}

    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public int getVibrate() {
        return vibrate;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }
}
