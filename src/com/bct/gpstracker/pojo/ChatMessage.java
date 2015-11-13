package com.bct.gpstracker.pojo;

import java.util.Date;

import android.content.Context;

import org.json.JSONObject;

import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;

public class ChatMessage {
	/**
	 *         "content": "sdfsf",
       "create_time": "2014-02-13 10:27:13",
       "id": "24",
       "name": "asd"
	 */
	private int id;
	private String content;
	private String name;
	private Date createTime;
	private boolean isComMeg = false;
	private String photo;
	private String type;	 //类型
	private String voiceUrl; //语音连接
	private String localUrl; //本地语言地址
	private String time;	//时间
	public ChatMessage() {
		super();
	}
//	public ChatMessage(String content, Date createTime, boolean isComMeg,String photo) {
//		super();
//		this.content = content;
//		this.createTime = createTime;
//		this.isComMeg = isComMeg;
//		this.photo = photo;
//	}
	public ChatMessage(JSONObject json){
		try {
			if(json.has("id")) id = json.getInt("id");
			if(json.has("content")) content = json.getString("content");
			if(json.has("name")) name = json.getString("name");
			if(json.has("create_time")) createTime = new Date(json.getLong("create_time")*1000);
			if(json.has("time")) time = json.getString("time");
			if(json.has("photo")) photo = json.getString("photo");
			if(json.has("voiceUrl")) voiceUrl = json.getString("voiceUrl");
			if(json.has("localUrl")) localUrl = json.getString("localUrl");
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
    public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public boolean getMsgType() {
    	return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
    	isComMeg = isComMsg;
    }
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public boolean isComMeg() {
		return isComMeg;
	}

	public void setComMeg(boolean isComMeg) {
		this.isComMeg = isComMeg;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVoiceUrl() {
		return voiceUrl;
	}

	public void setVoiceUrl(String voiceUrl) {
		this.voiceUrl = voiceUrl;
	}

	public String getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


	public static void sendVoice(Context context,String imei,String filename,String filecontent,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("fileName", filename);
			json.put("imei",imei);
			json.put("voiceFile", filecontent);
			BctClient.getInstance().POST(context, CommonRestPath.sendVoice(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void collectVoice(Context context,int id,String url,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("termId", id);
			json.put("voiceUrl",url);
			BctClient.getInstance().POST(context, "/collectVoice/collect", json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void getCollectVoice(Context context,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context, "/collectVoice/query", json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
