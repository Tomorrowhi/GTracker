package com.bct.gpstracker.pojo;

import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;

/**
 * 监护人实体
 * @author huangfei
 *
 */
public class Keeper extends ManaRelation implements Serializable{
	
	private static final long serialVersionUID = -6806294559373705721L;
	/**
	 *  "id": 1,
            "name": "13526236236",
            "portrait": " http://114.119.7.105/gps/uploadsimgs/user/5652365985556956.jpg ",
			"passward": "WDsdsawfSFSFerwweeeedfa",
			"appIdentity": "爷爷",
            "appUserNum": “2”
	 */
	private int id = 0;
	private String name = "";//电话号码
	private String portrait = "";//头像
	private String password = "";
	private String appIdentity = "";	//关系
	private String appUserNum = "";
    private String cellPhone = "";
    private String oldCellPhone = "";
    private String nickName = "";
	private String validcode="";
	private String imei="";
    private int relation=0;

	public String getValidcode() {
		return validcode;
	}

	public void setValidcode(String validcode) {
		this.validcode = validcode;
	}

	public Keeper(){};
	public Keeper(JSONObject json){
		try {
			if(json.has("id")) id = json.optInt("id",0);
			if(json.has("name")) name = json.optString("name");
			if(json.has("portrait")) portrait = json.optString("portrait");
			if(json.has("passward")) password = json.optString("passward");
			if(json.has("appIdentity")) appIdentity = json.optString("appIdentity");
			if(json.has("appUserNum")) appUserNum = json.optString("appUserNum");
			if(json.has("cellPhone")) cellPhone = json.optString("cellPhone");
			nickName = json.optString("nickName");
			imei = json.optString("imei");
            relation=json.optInt("relation");
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPortrait() {
		return portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAppIdentity() {
		return appIdentity;
	}
	public void setAppIdentity(String appIdentity) {
		this.appIdentity = appIdentity;
	}
	public String getAppUserNum() {
		return appUserNum;
	}
	public void setAppUserNum(String appUserNum) {
		this.appUserNum = appUserNum;
	}

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getOldCellPhone() {
        return oldCellPhone;
    }

    public void setOldCellPhone(String oldCellPhone) {
        this.oldCellPhone = oldCellPhone;
    }

    public static void getList(Context context, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			BctClient.getInstance().POST(context, CommonRestPath.getUserList(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void add(Context context,Keeper keeper,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("id", "0");
			json.put("name", keeper.getName());
			json.put("password", keeper.getPassword());
			json.put("portrait",keeper.getPortrait());
			json.put("appUserNum",keeper.getAppUserNum());
			json.put("appIdentity", keeper.getAppIdentity());
			json.put("validcode",keeper.getValidcode());
			json.put("cellPhone", keeper.getCellPhone());
			json.put("nickName", keeper.getNickName());
			json.put("relation", keeper.getRelation());
			BctClient.getInstance().POST(context, CommonRestPath.userADD(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public void update(Context context,final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("id",this.getId());
			json.put("name", this.getName());
			json.put("password", this.getPassword());
			json.put("portrait",this.getPortrait());
			json.put("appUserNum",this.getAppUserNum());
			json.put("appIdentity", this.getAppIdentity());
			json.put("validcode",getValidcode());
            json.put("cellPhone", getCellPhone());
            json.put("nickName", getNickName());
            json.put("relation", getRelation());
            BctClient.getInstance().POST(context, CommonRestPath.userADD(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

	public void setAscensionMain(Context context,boolean isArise, final BctClientCallback callback){
		try{
			JSONObject json = new JSONObject();
			json.put("ascensionId",id);
            json.put("isArise",isArise?1:0);
			BctClient.getInstance().POST(context, "/identityUser/ascensionMain", json, new JsonHttpResponseHelper(callback).getHandler());
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
			BctClient.getInstance().POST(context, CommonRestPath.DeleteUser(), json, new JsonHttpResponseHelper(callback).getHandler());
		}catch (Exception ex){
			ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
	}

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Keeper{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", portrait='").append(portrait).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", appIdentity='").append(appIdentity).append('\'');
        sb.append(", appUserNum='").append(appUserNum).append('\'');
        sb.append(", cellPhone='").append(cellPhone).append('\'');
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", validcode='").append(validcode).append('\'');
        sb.append(", imei='").append(imei).append('\'');
        sb.append(", relation=").append(relation);
        sb.append('}');
        return sb.toString();
    }
}
