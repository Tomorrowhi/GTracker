package com.bct.gpstracker.util;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by liaoxw on 15/3/27.
 */
public class JSONHelper {

    public static int getInt(JSONObject json,String key){
        int val = 0;
        try{
            if(json.has(key)){
                val = json.getInt(key);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return val;
    }

    public static String getString(JSONObject json,String key){
        String val = "";
        try{
            if(json.has(key)){
                val = json.getString(key);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return val;
    }

    public static Date getDate(JSONObject json,String key){
        Date val = new Date();
        try{
            if(json.has(key)){
                //TODO:生成日期
               // val = json.getString(key);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return val;
    }

    public static JSONObject getJSONObject(JSONArray json,int index){
        try{
            if(json.length() > index){
                return json.getJSONObject(index);
            }
        }catch (Exception ex){

        }
        return null;
    }

    public static JSONArray getJSONArray(JSONObject json,String key){
        try{
            if(json.has(key)) {
                return json.getJSONArray(key);
            }
        }catch (Exception ex){

        }
        return new JSONArray();
    }

    public static JSONObject getJSONObject(JSONObject json,String key){
        try{
            if(json.has(key)) {
                return json.getJSONObject(key);
            }
        }catch (Exception ex){

        }
        return null;
    }

    public static Long getLong(JSONObject json, String key) {
        Long val = 0L;
        try{
            if(json.has(key)){
                val = json.getLong(key);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return val;
    }
    public static boolean getState(JSONObject json, String key) {
        boolean val = false;
        try{
            if(json.has(key)){
                val = json.getBoolean(key);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return val;
    }

    public static JSONObject parseJSON(String jsonStr){
        try {
            return new JSONObject(jsonStr);
        }catch (Exception e){
            return null;
        }
    }

}
