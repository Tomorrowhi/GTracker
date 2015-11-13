package com.bct.gpstracker.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amap.api.maps.model.LatLng;
import com.bct.gpstracker.util.JSONHelper;

/**
 * 围栏实体
 * @author huangfei
 *
 */
public class FenceEntity implements Serializable{
	private static final long serialVersionUID = 7843351729288886429L;
	
	private int id;
	private int areaType;	//栅栏类型(1 圆2 矩形3 多边形)
	private WeekDayTimes dayTimes = new WeekDayTimes();
//	private WeekDayTimes dayTimes = null;
	private List<LatLngMix> latLngMixes = new ArrayList<>();
	private String areaName = "";
	private String startTime = "00:00";
	private String endTime = "00:00";
	private String weekDays = "";
	
	public FenceEntity(){};

	public FenceEntity(JSONObject json){
		try {
			if(json.has("id")) id = json.getInt("id");
			if(json.has("areaType")) areaType = json.getInt("areaType");
			if(json.has("weekDayTimes")){
				dayTimes = new WeekDayTimes(json.getJSONObject("weekDayTimes"));
			}
			areaName = JSONHelper.getString(json,"areaName");
			startTime = JSONHelper.getString(json,"startTime");
			endTime = JSONHelper.getString(json,"endTime");
			weekDays = JSONHelper.getString(json,"weekDays");

			if(json.has("coordinates")){
				JSONArray array = json.getJSONArray("coordinates");
				if(array.length()!=0){
					for(int i=0;i<array.length();i++){
						LatLng latLng = new LatLng(Double.parseDouble(array.getJSONObject(i).getString("latitude")), Double.parseDouble(array.getJSONObject(i).getString("longitude")));
                        if(array.getJSONObject(i).has("radius")){
                            double radius=array.getJSONObject(i).getDouble("radius");
                            latLngMixes.add(new LatLngMix(latLng, radius));
                        }else {
                            latLngMixes.add(new LatLngMix(latLng, 0));
                        }
					}
				}
			}
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
	public int getAreaType() {
		return areaType;
	}
	public void setAreaType(int areaType) {
		this.areaType = areaType;
	}
	public WeekDayTimes getDayTimes() {
		return dayTimes;
	}
	public void setDayTimes(WeekDayTimes dayTimes) {
		this.dayTimes = dayTimes;
	}
	public List<LatLngMix> getLatLngMixes() {
		return latLngMixes;
	}
	public void setLatLngMixes(List<LatLngMix> latLngMixes) {
		this.latLngMixes = latLngMixes;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getWeekDays() {
		return weekDays;
	}

	public void setWeekDays(String weekDays) {
		this.weekDays = weekDays;
	}

    public class LatLngMix {
        private LatLng latLng;
        private double radius;

        public LatLngMix(LatLng latLng, double radius) {
            this.latLng = latLng;
            this.radius = radius;
        }

        public LatLng getLatLng() {
            return latLng;
        }

        public void setLatLng(LatLng latLng) {
            this.latLng = latLng;
        }

        public double getRadius() {
            return radius;
        }

        public Double getRadiusValue() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }
    }
}
