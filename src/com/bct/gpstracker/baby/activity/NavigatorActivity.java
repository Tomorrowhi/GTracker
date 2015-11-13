package com.bct.gpstracker.baby.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.util.TTSController;

public class NavigatorActivity extends BaseActivity implements AMapNaviViewListener {
    private AMapNaviView mAmapAMapNaviView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naviview);
        init(savedInstanceState);
    }

    /**
     * 初始化
     * @param savedInstanceState
     */
    private void init(Bundle savedInstanceState) {
        mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.navi_map);
        mAmapAMapNaviView.onCreate(savedInstanceState);
        // 设置导航界面监听
        mAmapAMapNaviView.setAMapNaviViewListener(this);
        AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);
        TTSController.getInstance(this).startSpeaking();
    }

    /**
     * 导航界面左下角返回按钮回调
     *
     */
    @Override
    public void  onNaviCancel() {
        AMapNavi.getInstance(this).stopNavi();
        Intent intent = new Intent(NavigatorActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    /**
     * 导航界面右下角功能设置按钮回调
     *
     */
    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviMapMode(int arg0) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }


    /**
     * 返回键盘监听
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AMapNavi.getInstance(this).stopNavi();
            Intent intent = new Intent(NavigatorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAmapAMapNaviView.onResume();
    };

    @Override
    public void onPause() {
        super.onPause();
        mAmapAMapNaviView.onPause();
        AMapNavi.getInstance(this).stopNavi();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
        mAmapAMapNaviView.onDestroy();
        TTSController.getInstance(this).stopSpeaking();
        TTSController.getInstance(this).destroy();
    }
}
