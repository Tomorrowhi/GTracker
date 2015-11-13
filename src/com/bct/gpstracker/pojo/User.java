package com.bct.gpstracker.pojo;

import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import com.amap.api.maps.model.LatLng;
import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.vo.Session;


public class User implements Serializable{
	private static final long serialVersionUID = 1685006415337227032L;

    private Long id;
	private String phone;
    private String portrait;
	private String pwd;
    private String imei;
	private String appUserNum;	//为1时为主用户
	private String term_hone; //终端手机号
	
	public User(){};
	public User(JSONObject json){
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getTerm_hone() {
		return term_hone;
	}

	public void setTerm_hone(String term_hone) {
		this.term_hone = term_hone;
	}

	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getAppUserNum() {
		return appUserNum;
	}
	public void setAppUserNum(String appUserNum) {
		this.appUserNum = appUserNum;
	}

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public static void checkPhone(Context context,String phone, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("phone",phone);
			BctClient.getInstance().POST(context,CommonRestPath.checkPhone(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void getValdcode(Context context,String phone, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("phone",phone);
			BctClient.getInstance().POST(context,CommonRestPath.validCode(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void register(Context context,String phone,String password,String code, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("phone",phone);
			json.put("pwd",password);
			json.put("validcode",code);
			json.put("clientId", Session.getInstance().getClientId());
//			json.put("eqImei",imei);
//			json.put("term_hone", term_hone);
			BctClient.getInstance().POST(context,CommonRestPath.register(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void login(Context context,String phone,String password, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("phone",phone);
			json.put("pwd", password);
            json.put("clientid", Session.getInstance().getClientId());
			BctClient.getInstance().POST(context,CommonRestPath.login(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void modifyPassword(Context context,String phone,String password,String code, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("phone",phone);
			json.put("pwd", password);
			json.put("validcode", code);
			BctClient.getInstance().POST(context,CommonRestPath.modifyPwd(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void logout(Context context,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context,CommonRestPath.logout(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void sendFeedback(Context context,JSONObject json,final BctClientCallback callback){
		try{
			BctClient.getInstance().POST(context,CommonRestPath.sendFeedback(),json,new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public static void getAppVersion(Context context,final BctClientCallback callback){
		try{
			BctClient.getInstance().POST(context,CommonRestPath.getVersion(),(new JSONObject()),new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

    public static void getChatGIFVersion(Context context,final BctClientCallback callback){
        try{
            BctClient.getInstance().POST(context,CommonRestPath.getVersion(),(new JSONObject()),new JsonHttpResponseHelper(callback).getHandler());
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                callback.onFailure(null);
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

    public static void postGPS() {
        try {
            LatLng latLng = Session.getInstance().getLatLng();
            if (latLng == null) {
                return;
            }
            String gps = String.format("g,%s,%s", latLng.latitude + "", latLng.longitude + "");
            String wifi = String.format("%s", Session.getInstance().getWifiInfo());
            String cell = String.format("l,", Session.getInstance().getGsmCellInfo());
            JSONObject json = new JSONObject();
            json.put("gpsInfro", gps);
            json.put("wifiInfro", wifi);
            json.put("lbsInfro", cell);
            BctClientCallback callback=new BctClientCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                }

                @Override
                public void onFailure(String message) {
                }
            };
            BctClient.getInstance().POST(AppContext.getContext(),CommonRestPath.postPosition(),json,new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            Log.e(Constants.TAG, "提交数据出错！", ex);
        }

    }

}
