package com.bct.gpstracker.common;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.bct.gpstracker.inter.NotifyCallback;
import com.bct.gpstracker.util.CommUtil;

/**
 * Created by HH
 * Date: 2015/6/24 0024
 * Time: 下午 7:51
 */
public class LocationProvider implements AMapLocationListener {
    private LocationManagerProxy mLocationManagerProxy;

    private Context context;

    private static Map<String, NotifyCallback> callbackMap;
    private static LinkedList<NotifyCallback> callbackList;

    public static boolean isLocationing = false;

    private static LocationProvider instance;

    public static LocationProvider getInstance(Context context) {
        if (instance == null) {
            instance = new LocationProvider(context);
        }
        return instance;
    }

    private LocationProvider(Context context) {
        this.context = context;
    }

    public void startLocation() {
        mLocationManagerProxy = LocationManagerProxy.getInstance(context);
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);
        mLocationManagerProxy.setGpsEnable(false);
        isLocationing = true;
    }

    public void startLocation(NotifyCallback callback) {
        if(callbackList==null){
            callbackList=new LinkedList<>();
        }
        callbackList.add(callback);
        startLocation();
    }

    public LocationProvider setNotify(String key, NotifyCallback callback) {
        if (callbackMap == null) {
            callbackMap = new ConcurrentHashMap<>();
        }
        callbackMap.put(key, callback);
        return this;
    }

    public void removeNotify(String key) {
        if (callbackMap != null) {
            callbackMap.remove(key);
        }
    }

    public void clearNotify() {
        if (callbackMap != null) {
            callbackMap.clear();
        }
    }

    public void stop() {
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }

    @Override
    public void onLocationChanged(AMapLocation loc) {
        if (loc == null) {
            return;
        }
        isLocationing = false;
        Log.d(Constants.TAG, "当前位置：" + loc.getLatitude() + "," + loc.getLongitude());
        if (callbackMap != null && !callbackMap.isEmpty()) {
            for (Map.Entry<String, NotifyCallback> entry : callbackMap.entrySet()) {
                try {
                    entry.getValue().execute(loc);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "回调出错，有可能是执行环境已改变", e);
                }
            }
        }
        if(CommUtil.isNotEmpty(callbackList)){
            NotifyCallback callback;
            while ((callback=callbackList.poll())!=null){
                try {
                    callback.execute(loc);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "回调出错，有可能是执行环境已改变", e);
                }
            }
        }
        stop();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
