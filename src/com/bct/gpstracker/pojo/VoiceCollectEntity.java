package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * 语音收藏的实体
 * @author huangfei
 *
 */
public class VoiceCollectEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2625718313248457527L;
	
	private int id;
	private String photoUrl;
	private String name;
	private String collectTime;
	
	public VoiceCollectEntity(){};
	public VoiceCollectEntity(JSONObject json){
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPhotoUrl() {
		return photoUrl;
	}
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(String collectTime) {
		this.collectTime = collectTime;
	};
	
}
