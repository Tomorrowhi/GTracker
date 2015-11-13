package com.bct.gpstracker.listener;

import android.content.Context;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.bct.gpstracker.R;
import com.bct.gpstracker.util.TTSController;

/**
 * Created by HH
 * Date: 2015/9/23 0023
 * Time: 下午 3:58
 */
public class GTAMapNaviListener implements AMapNaviListener {
    private Context context;
    private TTSController tts;

    public GTAMapNaviListener(Context context, TTSController tts) {
        this.tts = tts;
        this.context = context;
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {
        tts.playText(s);
    }

    @Override
    public void onEndEmulatorNavi() {
        tts.playText(context.getString(R.string.navi_end));
    }

    @Override
    public void onArriveDestination() {
        tts.playText(context.getString(R.string.navi_arriving_destination));
    }

    @Override
    public void onCalculateRouteSuccess() {
        tts.playText(context.getString(R.string.navi_route_success));
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        tts.playText(context.getString(R.string.navi_route_failed));
    }

    @Override
    public void onReCalculateRouteForYaw() {
        tts.playText(context.getString(R.string.navi_route_offset));
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        tts.playText(context.getString(R.string.navi_route_for_jam));
    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }
}
