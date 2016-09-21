package com.bct.gpstracker.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;

/**
 * 功能：使用ViewPager实现初次进入应用时的引导页
 *
 * (1)判断是否是首次加载应用--采取读取SharedPreferences的方法
 * (2)是，则进入引导activity；否，则进入LoginActivity
 * (3)2s后执行(2)操作
 *  @author huangfei
 */
public class LauncherActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        SharedPreferences preferences=Utils.getPreferences(this);
        Constants.baseUrl=preferences.getString(Constants.SETTING_SERVER_URL,null);
        if(CommUtil.isBlank(Constants.baseUrl)||Constants.baseUrl.length()<8) {
            Constants.baseUrl = Utils.getMetaValue(this, "base_url");
        }

//        boolean mFirst = isFirstEnter(LauncherActivity.this,LauncherActivity.this.getClass().getName());
//        if(mFirst)
//            mHandler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY,2000);
//        else
//            mHandler.sendEmptyMessageDelayed(SWITCH_MAINACTIVITY,2000);
        Intent mIntentMain = new Intent();
        //正常登录
//        mIntentMain.setClass(LauncherActivity.this, LoginActivity.class);
//        LauncherActivity.this.startActivity(mIntentMain);
//        LauncherActivity.this.finish();

        //跳过登录界面
        mIntentMain.setClass(LauncherActivity.this, MainActivity.class);
        LauncherActivity.this.startActivity(mIntentMain);
        AppContext.isEntered = true;
        LauncherActivity.this.finish();
    }  
     
    //****************************************************************
    // 判断应用是否初次加载，读取SharedPreferences中的guide_activity字段
    //****************************************************************
//    private static final String SHAREDPREFERENCES_NAME = "my_pref";
//    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
//    private boolean isFirstEnter(Context context,String className){
//        if(context==null || className==null||"".equalsIgnoreCase(className)) return false;
//        String mResultStr = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE).getString(KEY_GUIDE_ACTIVITY, "");//取得所有类名 如 com.my.MainActivity
//        if(mResultStr.equalsIgnoreCase("false"))
//            return false;
//        else
//            return true;
//    }
     
     
    //*************************************************
    // Handler:跳转至不同页面
    //*************************************************
//    private final static int SWITCH_MAINACTIVITY = 1000;
//    private final static int SWITCH_GUIDACTIVITY = 1001;
//    public Handler mHandler = new Handler(){
//        public void handleMessage(Message msg) {
//            switch(msg.what){
//            case SWITCH_MAINACTIVITY:
//                Intent mIntentMain = new Intent();
//                mIntentMain.setClass(LauncherActivity.this, LoginActivity.class);
//                LauncherActivity.this.startActivity(mIntentMain);
//                LauncherActivity.this.finish();
//                break;
//            case SWITCH_GUIDACTIVITY:
//                Intent mIntentGUID = new Intent();
//                mIntentGUID.setClass(LauncherActivity.this, GuideActivity.class);
//                LauncherActivity.this.startActivity(mIntentGUID);
//                LauncherActivity.this.finish();
//                break;
//            }
//            super.handleMessage(msg);
//        }
//    };
	
}
