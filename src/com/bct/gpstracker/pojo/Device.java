package com.bct.gpstracker.pojo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.vo.Session;
import com.bct.gpstracker.vo.WifiInfos;

/**
 * 设备实体
 * @author huangfei
 *
 */
public class Device extends ManaRelation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6727947690912338526L;
	/**
	 *             "id": 1,
            "name": "设备1",
            "imei": "565236526589547",
            "portrait": "http://114.119.7.105/gps/uploadsimgs/terminal/2015012039154540.jpg",
            "phone": "138****"

	 */
	private int id;
	private long monitorId;
	private String name = "";
	private String imei ="";//手表id
	private String phone = "";//手机号
	private String portrait = "";
	private String sex = "";
	private String birthday = "";
	private String sign = "";
	private String mac = "";
	private int online = 0; //设备的在线状态，0为不在线，1为在线
    private boolean binded=false;

	private String oldName;
	private String oldPhone;

	public String getMac() {
		return mac;
	}

	public void setMac(String blueMac) {
		this.mac = blueMac;
	}

	public Device(){}
	public Device(JSONObject json){
		try {
			if(json.has("id")) id = json.getInt("id");
			if(json.has("name")) name = json.getString("name");
			if(json.has("imei")) imei = json.getString("imei");
			if(json.has("phone")) phone = json.getString("phone");
			if(json.has("portrait")) portrait = json.getString("portrait");
			if(json.has("sex")) sex = json.getString("sex");
			if(json.has("birthday")) birthday = json.getString("birthday");
			if(json.has("signature")) sign = json.getString("signature");
			if(json.has("mac")) mac = json.getString("mac");
			if(json.has("online")) online = json.getInt("online");
			if(json.has("monitorId")) monitorId = json.getInt("monitorId");
//			blueMac = json.optString("blueMac");
            binded=json.optInt("appBind")==1;

			Log.i("json设备信息：" , json.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(long monitorId) {
		this.monitorId = monitorId;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public String getPortrait() {

		return this.portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldPhone() {
		return oldPhone;
	}

	public void setOldPhone(String oldPhone) {
		this.oldPhone = oldPhone;
	}

    public boolean isBinded() {
        return binded;
    }

    public void setBinded(boolean binded) {
        this.binded = binded;
    }

    public static void add(Context context,String name,String imei,String portrait,String birthday,String sex,String signature, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("id",0);
			json.put("name", name);
			json.put("imei", imei);
			json.put("portrait", portrait);
			json.put("birthday", birthday);
			json.put("sex", sex);
			json.put("signature", signature);
			if(signature == null) json.put("signature"," ");
			BctClient.getInstance().POST(context, CommonRestPath.deviceAdd(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

    public static void editMonitorObject(Context context,Device device, final BctClientCallback callback){
        try{
            JSONObject json = new JSONObject();
            json.put("id",device.getMonitorId());
            json.put("name", device.getName());
            json.put("imei", device.getImei());
            json.put("phone", device.getPhone());
            json.put("portrait", device.getPortrait());
            json.put("birthday", device.getBirthday());
            json.put("sex", device.getSex());
            json.put("signature", device.getSign());
            if(device.getSign() == null) json.put("signature","");
            BctClient.getInstance().POST(context, CommonRestPath.editMonitorObject(), json, new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

	public static void getList(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context, CommonRestPath.getDeviceList(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

    public static void getMonitorList(Context context, final BctClientCallback callback){
        try{
            JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context, CommonRestPath.querytMonitorObject(), json, new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

	public void update(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("id", this.getMonitorId());
			json.put("name", this.getName());
			json.put("imei", this.getImei());
			json.put("portrait", this.getPortrait());
			json.put("birthday", this.getBirthday());
			json.put("sex", this.getSex());
			json.put("signature", this.getSign());
			BctClient.getInstance().POST(context, CommonRestPath.deviceAdd(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public void delete(Context context, final BctClientCallback callback){
		try{
			JSONArray array = new JSONArray();
			array.put(this.getId());
			JSONObject json = new JSONObject();
			json.put("ids", array);
			BctClient.getInstance().POST(context, CommonRestPath.deleteDevice(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

    public void deleteMonitorObject(Context context, final BctClientCallback callback){
        try{
            JSONArray array = new JSONArray();
            array.put(this.getMonitorId());
            JSONObject json = new JSONObject();
            json.put("ids", array);
            BctClient.getInstance().POST(context, CommonRestPath.deleteMonitorObjct(), json, new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

	public void getTrack(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("imei", this.getImei());
			BctClient.getInstance().POST(context, CommonRestPath.getTrack(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}
    public String getTrackSync(){
        try{
            JSONObject json = new JSONObject();
            json.put("imei", this.getImei());

            List<BasicNameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("",json.toString()));

            String baseUrl=BctClient.getInstance().createUrl(CommonRestPath.getTrack());
            HttpPost postMethod = new HttpPost(baseUrl);
            postMethod.addHeader("accesskey", Session.getInstance().getAccessKey());

            StringEntity s = new StringEntity(json.toString());
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            postMethod.setEntity(s);

            HttpClient httpClient=new DefaultHttpClient();
            HttpResponse response = httpClient.execute(postMethod);
            return EntityUtils.toString(response.getEntity());
        }catch (Exception ex){
            Log.e(Constants.TAG, "获取数据出错", ex);
            return null;
        }
    }

	public static void reCorrect(Context context,JSONObject json, final BctClientCallback callback){
		try{
			BctClient.getInstance().POST(context, CommonRestPath.reCorrect(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public  void getHistoryTrackList(Context context,String date, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("date", date);
			json.put("id",this.getId());
			BctClient.getInstance().POST(context, CommonRestPath.historyLocation(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public void getFenceList(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
            json.put("imei",this.getImei());
			BctClient.getInstance().POST(context,CommonRestPath.getFenceList(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void saveWifi(Context context,String name,String password,String imei, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("wifi",name);
			json.put("wifiPwd",password);
            JSONArray jsonArray=new JSONArray();
            jsonArray.put(json);
            JSONObject cont=new JSONObject();
            cont.put("content",jsonArray);
            cont.put("imei",imei);
            BctClient.getInstance().POST(context, CommonRestPath.wifiEdit(), cont, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void saveWifis(Context context,List<WifiInfos> wifiInfosList,String imei, final BctClientCallback callback){
		try{
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < wifiInfosList.size(); i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("wifi", wifiInfosList.get(i).getWifi());
				jsonObject.put("wifiPwd", wifiInfosList.get(i).getWifiPwd());
				jsonObject.put("id", wifiInfosList.get(i).getId());
				jsonArray.put(jsonObject);
			}

            JSONObject cont=new JSONObject();
            cont.put("content",jsonArray);
            cont.put("imei",imei);
            BctClient.getInstance().POST(context, CommonRestPath.wifiEdit(), cont, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}


	public static void getWifi(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context, CommonRestPath.wifiQuery(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void addFence(Context context, JSONObject json,final BctClientCallback callback){
		try{
			BctClient.getInstance().POST(context, CommonRestPath.fenceAdd(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public void getCommandDataByImeiAndType(Context context,String type, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("imei",this.getImei());
			json.put("type",type);
			BctClient.getInstance().POST(context, CommonRestPath.restTimeDuration(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public void fenceDelete(Context context,int id,final BctClientCallback callback){
        try{
            JSONObject json = new JSONObject();
            json.put("id",id);
            BctClient.getInstance().POST(context,CommonRestPath.fenceDelete(), json, new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }
}
