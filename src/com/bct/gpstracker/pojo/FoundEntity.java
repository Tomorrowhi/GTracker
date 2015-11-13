package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * 发现的实体
 * @author huangfei
 */
public class FoundEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8415695525451382562L;

	private String photo;
	private String name;
	private String distance;
	private String type;
	private String sign;
	
	public FoundEntity(){};
	public FoundEntity(JSONObject json){
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
}
