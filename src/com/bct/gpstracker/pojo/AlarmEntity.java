package com.bct.gpstracker.pojo;

import java.io.Serializable;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.vo.Session;

/**
 * 生活助手实体
 *
 * @author huangfei
 */
public class AlarmEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7728007301828306604L;

    /**
     * "id":1,
     * “name":"小新的朗读",
     * "termId": 3,
     * "startTime": "0900",
     * "endTime": "1400",
     * "weeks": "1,2,0,4,5,0,0",
     * "imei": "012207000691838",
     * "fileName": "ls,012207000691838,012207000691838,0001",
     * "voiceFile":""
     * "content":闹钟的内容
     * audioSeq :铃声位置 1-10
     */
    private int id = 0;
    private int termId = 0;
    private String time = "";
    private String weeks = "";
    private String name = "";
    private String imei = "";
    private String voiceUrl = "";
    private String content = "";  //闹钟的内容（新加的字段）
    private int audioSeq = 0; //铃声位置 1-10

    public AlarmEntity() {
    }

    ;

    public AlarmEntity(JSONObject json) {
        try {
            if (json.has("id")) id = json.getInt("id");
            if (json.has("termId")) termId = json.getInt("termId");
            if (json.has("startTime")) time = json.getString("startTime");
            if (json.has("weeks")) weeks = json.getString("weeks");
            if (json.has("name")) name = json.getString("name");
            if (json.has("imei")) imei = json.getString("imei");
            if (json.has("voiceUrl")) voiceUrl = json.getString("voiceUrl");
            if (json.has("content")) content = json.getString("content");
            if (json.has("audioSeq")) audioSeq = json.getInt("audioSeq");
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getAudioSeq() {
        return audioSeq;
    }

    public void setAudioSeq(int audioSeq) {
        this.audioSeq = audioSeq;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public static void getList(Context context, final BctClientCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("imei", Session.getInstance().getSetupDevice().getImei());
            BctClient.getInstance().POST(context, CommonRestPath.alarmQuery(),json,new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void add(Context context, JSONObject json, final BctClientCallback callback) {
        try {
            BctClient.getInstance().POST(context, CommonRestPath.alarmAdd(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void delete(Context context, final BctClientCallback callback) {
        try {
            JSONArray json = new JSONArray();
            json.put(this.getId());
            JSONObject data = new JSONObject();
            data.put("ids", json);
            BctClient.getInstance().POST(context, CommonRestPath.alarmDelete(), data, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
