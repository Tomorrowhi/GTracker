package com.bct.gpstracker.pojo;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.util.JSONHelper;


public class ResponseData implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1685006415337227012L;

	private int retcode;
	private String msg;
	private JSONObject body;	//为1时为主用户
	private JSONArray bodyArray;

	public ResponseData(){};
    public ResponseData(String jsonStr){
        JSONObject json=JSONHelper.parseJSON(jsonStr);
        init(json);
    }

	public ResponseData(JSONObject json){
        init(json);
	}

    private void init(JSONObject json) {
        JSONObject head = JSONHelper.getJSONObject(json, "head");
        this.retcode = JSONHelper.getInt(head,"retcode");
        this.msg = JSONHelper.getString(head,"msg");
        Object o = JSONHelper.getJSONObject(json,"body");
        if(o == null){
            this.bodyArray = JSONHelper.getJSONArray(json,"body");
        }else{
            this.body = (JSONObject)o;
        }
    }

    public int getBodyInt(String key){
        try {
            return getBody().getInt(key);
        }catch (Exception e){
            return 0;
        }
    }

    public String getBodyString(String key){
        try {
            return getBody().getString(key);
        }catch (Exception e){
            return null;
        }
    }

    public String getBodyString(String key,String defaultVal){
        try {
            return getBody().getString(key);
        }catch (Exception e){
            return defaultVal;
        }
    }

    public String getString(JSONObject jsonObject,String key){
        try {
            return jsonObject.getString(key);
        }catch (Exception e){
            return null;
        }
    }

	public int getRetcode() {
		return retcode;
	}

	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public JSONObject getBody() {
		return body;
	}

	public void setBody(JSONObject body) {
		this.body = body;
	}

	public JSONArray getBodyArray() {
		return bodyArray;
	}

	public void setBodyArray(JSONArray bodyArray) {
		this.bodyArray = bodyArray;
	}
}
