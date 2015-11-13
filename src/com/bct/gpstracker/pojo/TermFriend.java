package com.bct.gpstracker.pojo;

import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;

/**
 * Created by Admin on 2015/9/9 0009.
 * 用户好友bean，不再与监护人共同使用Keeper类了，原来访问监护人的接口中会将好友信息一并返回，以后会做数据清理，
 * 不再返回好友数据，好友通过访问新接口获取到好友列表，新版好友是没有头像的，头像使用系统默认头像，
 */
public class TermFriend extends ManaRelation implements Serializable {


    private int id = 0;
    private String relationship = "";    //关系
    private String name = "";//名字
    private String cellPhone = "";//电话
    private String oldCellPhone = "";//原电话
    public TermFriend() {
    }
    public TermFriend(JSONObject obj){
        try{
            this.setId(obj.optInt("id"));
            this.setRelationship(obj.optString("relation"));
            this.setName(obj.optString("name"));
            this.setCellPhone(obj.optString("phone"));
        }catch(Exception e){
            Log.e(TermFriend.class.getName(),"new termFriend error",e);
        }
    }
    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getOldCellPhone() {
        return oldCellPhone;
    }

    public void setOldCellPhone(String oldCellPhone) {
        this.oldCellPhone = oldCellPhone;
    }

    public void friendsAddAndEdit(Context context, final BctClientCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", this.getId());
            json.put("name", this.getName());
            json.put("phone", this.getCellPhone());
            json.put("relation", this.getRelationship());
            BctClient.getInstance().POST(context, CommonRestPath.FriendsEdit(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

    public void friendsQuery(Context context, final BctClientCallback callback) {
        //需要测试
        try {
            BctClient.getInstance().POST(context, CommonRestPath.FriendsQuery(),null, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                callback.onFailure("Exception");
            } catch (Exception e) {
                Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
            }
        }
    }

    public void friendsDelete(Context context,JSONObject json, final BctClientCallback callback) {
        //需要测试
        try {
            /*JSONObject json = new JSONObject();
            // ids ==>  Long[],可以删除多个，也可以删除单个，需要传入long类型数组参数
            json.put("ids", ids);*/
            BctClient.getInstance().POST(context, CommonRestPath.FriendsDelete(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
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
        return "GoodFriends{" +
                "id=" + id +
                ", relationship='" + relationship + '\'' +
                ", name='" + name + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                '}';
    }
}
