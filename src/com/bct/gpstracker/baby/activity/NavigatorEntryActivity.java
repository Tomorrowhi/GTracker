package com.bct.gpstracker.baby.activity;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.listener.GTAMapNaviListener;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.TTSController;

public class NavigatorEntryActivity extends BaseActivity{
    private NaviLatLng sNode;
    private GTAMapNaviListener naviListener;
    private TTSController tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sNode=null;
        tts=TTSController.getInstance(this);
        tts.init();
        tts.startSpeaking();

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsEnabled){
            CommUtil.hideProcessing();
            //Utils.openGPSManually(NavigatorEntryActivity.this);
            String info=getString(R.string.navi_open_gps);
            tts.playText(info);
            CommUtil.showMsgShort(info);
//            finish();
//            return;
        }

        final AMapNavi navi = AMapNavi.getInstance(this);
        navi.startGPS();

        double startLatitude = getIntent().getDoubleExtra("startLatitude", -1);
        double startLongitude = getIntent().getDoubleExtra("startLongitude", -1);
        double endLatitude = getIntent().getDoubleExtra("endLatitude", -1);
        double endLongitude = getIntent().getDoubleExtra("endLongitude", -1);
        if (endLatitude == -1 || endLongitude == -1) {
            CommUtil.showMsgShort(getString(R.string.navi_not_found_destination));
            finish();
            return;
        }
        NaviLatLng latLngFrom = new NaviLatLng(startLatitude, startLongitude);
        NaviLatLng latLng = new NaviLatLng(endLatitude, endLongitude);
        final List<NaviLatLng> fromList = Collections.singletonList(latLngFrom);
        final List<NaviLatLng> toList = Collections.singletonList(latLng);

        naviListener=new GTAMapNaviListener(this,tts) {
            @Override
            public void onCalculateRouteSuccess() {
                CommUtil.hideProcessing();
                //tts.playText(getString(R.string.navi_route_success));
                Intent intent = new Intent(NavigatorEntryActivity.this, NavigatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCalculateRouteFailure(int i) {
                String info=getString(R.string.navi_route_failed);
                CommUtil.showMsgShort(info);
                tts.playText(getString(R.string.navi_route_failed));
                finish();
            }

            @Override
            public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
//                if (sNode == null) {
//                    sNode = aMapNaviLocation.getCoord();
//                    navi.calculateDriveRoute(Collections.singletonList(sNode), toList, null, AMapNavi.DrivingDefault);
//                }
            }
        };
        navi.setAMapNaviListener(naviListener);
        boolean flag=navi.calculateDriveRoute(fromList, toList, null, AMapNavi.DrivingDefault);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AMapNavi.getInstance(this).removeAMapNaviListener(naviListener);
    }
}
