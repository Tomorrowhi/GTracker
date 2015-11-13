package com.bct.gpstracker.ui;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.dialog.NoWifiDialog;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.vo.Session;
import com.lurencun.service.autoupdate.Version;
import com.lurencun.service.autoupdate.internal.FoundVersionDialog;
import com.lurencun.service.autoupdate.internal.NetworkUtil;
import com.lurencun.service.autoupdate.internal.VersionDialogListener;
import com.lurencun.service.autoupdate.internal.VersionPersistent;

/**
 * 用户登录页面
 *
 * @author huangfei
 */
public class LoginActivity extends Activity {

    private SharedPreferences mSharedPreferences;
//    private AppUpdate mAppUpdate;
    private PackageInfo info;
    private Context mContext = LoginActivity.this;
    private EditText phoneText, pwdText;
    private Button registButton, loginButton;
    private TextView forgotPwdView;    //忘记密码？
    private CheckBox checkBox;    //记住密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = Utils.getPreferences(LoginActivity.this);
        setContentView(R.layout.activity_login);// 显示真正的应用界面
//        mAppUpdate = AppUpdateService.getAppUpdate(this);

        try {
            info = LoginActivity.this.getPackageManager().getPackageInfo(LoginActivity.this.getApplicationContext().getPackageName(), 0);
            TextView tv = (TextView) findViewById(R.id.verTv);
            tv.setText(getString(R.string.version_tit) + info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initView();

//        if (mAppUpdate != null) {
//            mAppUpdate.callOnResume();
//        }
        getVersion();
        deleteExceptionLog();

        List<String> imeis= IMEI.getIMEI(this);
        if(CommUtil.isNotEmpty(imeis)){
            Session.getInstance().setClientId(imeis.get(0));
        }
    }

    /**
     * 删除异常记录
     */
    private void deleteExceptionLog() {
        boolean aBoolean = mSharedPreferences.getBoolean(MyConstants.EXCEPTION_LOG_UPLOAD, false);
        if (aBoolean) {
            File file = new File(mContext.getFilesDir() + "/crashLog/");
            if (!file.exists()) {
                Log.d("TAG", "异常捕获存储目录不存在");
                return;
            }

            File[] files = file.listFiles();
            if (files != null) {
                for (File mFile : files) {
                    if (!mFile.isDirectory()) {
                        //删除数据
                        mFile.delete();
                        Log.d("TAG", "异常捕获存_删除数据");
                    }
                }
            }
        }

    }

    private void initView() {
        phoneText = (EditText) findViewById(R.id.phoneET);
        pwdText = (EditText) findViewById(R.id.pwdET);
        registButton = (Button) findViewById(R.id.registBtn);
        loginButton = (Button) findViewById(R.id.loginBtn);
        forgotPwdView = (TextView) findViewById(R.id.forgotPwdTV);
        checkBox = (CheckBox) findViewById(R.id.pwdCB);

        if (mSharedPreferences.getBoolean(MyConstants.REMEMBER_PWD, false)) {
            phoneText.setText(mSharedPreferences.getString(MyConstants.USER_PHONE, ""));
            pwdText.setText(mSharedPreferences.getString(MyConstants.USER_PWD, ""));
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        registButton.setOnClickListener(clickListener);
        loginButton.setOnClickListener(clickListener);
        forgotPwdView.setOnClickListener(clickListener);

        Constants.hasNewVersion = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Session.getInstance().getMainActivity() != null) {
            LoginActivity.this.finish();
        }
        AppContext.forceLogout=false;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mAppUpdate != null) {
//            mAppUpdate.callOnPause();
//        }
    }


    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loginBtn:
                    if (phoneText.getText().toString().trim().equals("")) {
                        Toast.makeText(LoginActivity.this, R.string.phone_null_err, Toast.LENGTH_SHORT).show();
                    } else if (pwdText.getText().toString().trim().equals("")) {
                        Toast.makeText(LoginActivity.this, R.string.pwd_null_err, Toast.LENGTH_SHORT).show();
                    } else {
                        login(phoneText.getText().toString().trim(), pwdText.getText().toString().trim());
                    }
//				Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
//				LoginActivity.this.startActivity(loginIntent);
                    break;
                case R.id.registBtn:
                    Intent registIntent = new Intent(LoginActivity.this, RegistActivity.class);
                    LoginActivity.this.startActivity(registIntent);
                    Session.getInstance().setLoginActivity(LoginActivity.this);
                    break;
                case R.id.forgotPwdTV:
                    Intent pwdIntent = new Intent(LoginActivity.this, FindPwdActivity.class);
                    LoginActivity.this.startActivity(pwdIntent);
                    break;
            }
        }
    };

    /**
     * 用户登录
     *
     * @param phone
     * @param pwd
     */
    private void login(final String phone, final String pwd) {
        User.login(this, phone, pwd, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.login_post_data, LoginActivity.this);
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                WizardAlertDialog.getInstance().closeProgressDialog();
                if (obj.getRetcode() == 1) {
                    if (checkBox.isChecked()) {
                        long user_id = JSONHelper.getLong(obj.getBody(), "userId");
                        Editor editor = mSharedPreferences.edit();
                        editor.putString(MyConstants.USER_PHONE, phone);
                        editor.putString(MyConstants.USER_PWD, pwd);
                        editor.putLong(MyConstants.USER_ID, user_id);
                        editor.putBoolean(MyConstants.REMEMBER_PWD, true);
                        editor.apply();
                    } else {
                        Editor editor = mSharedPreferences.edit();
                        editor.putString(MyConstants.USER_PHONE, "");
                        editor.putString(MyConstants.USER_PWD, "");
                        editor.putLong(MyConstants.USER_ID, 0);
                        editor.putBoolean(MyConstants.REMEMBER_PWD, false);
                        editor.apply();
                    }
                            /*------------*/
                    //Session.getInstance().setAccessKey(JSONHelper.getString(obj.getBody(), "accesskey"));
                    Session.getInstance().setAccessKey(JSONHelper.getString(obj.getBody(), "accesskey"));
                    User user = new User();
                    user.setId(JSONHelper.getLong(obj.getBody(), "id"));
                    user.setPhone(phone);
                    user.setAppUserNum(JSONHelper.getString(obj.getBody(), "appUserNum"));
                    user.setImei(obj.getBodyString("imei"));
                    Session.getInstance().setUser(user);
                    Session.getInstance().setImei(user.getImei());
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(loginIntent);
                    AppContext.isEntered = true;
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(LoginActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                WizardAlertDialog.getInstance().closeProgressDialog();
                Toast.makeText(LoginActivity.this, R.string.login_err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                Utils.getManager(LoginActivity.this).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (!AppContext.isEntered) {
//            Intent itt = new Intent(AppContext.getContext(), CommunicationService.class);
//            LoginActivity.this.stopService(itt);

//        Intent pushIntent = new Intent(AppContext.getContext(), PushService.class);
//        LoginActivity.this.stopService(pushIntent);

//        }
    }


    /**
     * 获取终端版本信息
     */
    private void getVersion() {
        User.getAppVersion(this, new BctClientCallback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    JSONObject jsonObject = obj.getBody();
                    int versionCode = JSONHelper.getInt(jsonObject, "versionCode");
                    String versionName = JSONHelper.getString(jsonObject, "versionName");
                    String feature = JSONHelper.getString(jsonObject, "releaseNote");
                    String releaseUrl = JSONHelper.getString(jsonObject, "releaseUrl");
                    final Version version = new Version(versionCode, versionName, feature, releaseUrl);
                    try {
                        info = LoginActivity.this.getPackageManager().getPackageInfo(LoginActivity.this.getApplicationContext().getPackageName(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (null != info && version.code <= info.versionCode) {
                    } else {
                        FoundVersionDialog dialog = new FoundVersionDialog(LoginActivity.this, version, new VersionDialogListener() {
                            @Override
                            public void doUpdate(boolean laterOnWifi) {
                                if (!laterOnWifi) {
                                    checkNetworkAndUpdate(version);
                                } else {
                                    new VersionPersistent(LoginActivity.this).save(version);
                                }
                            }

                            @Override
                            public void doIgnore() {
                            }
                        });
                        dialog.show();
                    }
                } else {
                }
            }

            @Override
            public void onFailure(String message) {
            }
        });
    }

    private void checkNetworkAndUpdate(final Version version) {
        if (NetworkUtil.getNetworkType(LoginActivity.this) == NetworkUtil.WIFI) {
            Log.d(Constants.TAG, "现在是wifi网络");
            AppUpdataHandle.getInstance(LoginActivity.this).downloadAndInstall(version);
        } else if (NetworkUtil.getNetworkType(LoginActivity.this) == NetworkUtil.MOBILE) {
            Log.d(Constants.TAG, "现在走的是流量");
            NoWifiDialog dialog = new NoWifiDialog(LoginActivity.this, version, new VersionDialogListener() {
                @Override
                public void doUpdate(boolean laterOnWifi) {
                    AppUpdataHandle.getInstance(LoginActivity.this).downloadAndInstall(version);
                }
                @Override
                public void doIgnore() {
                }
            });
            dialog.show();

        } else {
            Toast.makeText(LoginActivity.this, "请检查网络是否连接", Toast.LENGTH_SHORT).show();
        }
    }

}
