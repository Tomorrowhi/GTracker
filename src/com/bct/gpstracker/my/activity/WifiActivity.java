package com.bct.gpstracker.my.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.vo.WifiInfos;

/**
 * 家庭wifi
 *
 * @author zhongjiayuan
 */
public class WifiActivity extends BaseActivity {

    private ImageButton backButton, pwdBtn, pwdBtn2, pwdBtn3;
    private EditText pwdET, pwdET2, pwdET3, wifiET, wifiET2, wifiET3;
    private Button deleteButton, completeButton;
    private SharedPreferences sp;
    private String wifiText = "";
    private String wifiText2 = "";
    private String wifiText3 = "";
    private String pwdText = "";
    private String pwdText2 = "";
    private String pwdText3 = "";
    private LinearLayout wifiRootLL;

    private SharedPreferences.Editor edit;
    private boolean ispwdBtn = true;  //显示密码图标的状态
    private boolean ispwdBtn2 = true;  //显示密码图标的状态
    private boolean ispwdBtn3 = true;  //显示密码图标的状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        wifiET = (EditText) findViewById(R.id.wifiET);
        wifiET2 = (EditText) findViewById(R.id.wifiET2);
        wifiET3 = (EditText) findViewById(R.id.wifiET3);
        pwdET = (EditText) findViewById(R.id.pwdET);
        pwdET2 = (EditText) findViewById(R.id.pwdET2);
        pwdET3 = (EditText) findViewById(R.id.pwdET3);
        pwdBtn = (ImageButton) findViewById(R.id.pwdBtn);
        pwdBtn2 = (ImageButton) findViewById(R.id.pwdBtn2);
        pwdBtn3 = (ImageButton) findViewById(R.id.pwdBtn3);
        deleteButton = (Button) findViewById(R.id.deleteBtn);
        backButton = (ImageButton) findViewById(R.id.backBtn);
        completeButton = (Button) findViewById(R.id.completeBtn);
        wifiRootLL = (LinearLayout) findViewById(R.id.wifiRootLL);
        sp = getSharedPreferences("wifiInfo", Context.MODE_PRIVATE);
        edit = sp.edit();
        initData();
        backButton.setOnClickListener(clickListener);
        completeButton.setOnClickListener(clickListener);
        deleteButton.setOnClickListener(clickListener);
        pwdBtn.setOnClickListener(clickListener);
        pwdBtn2.setOnClickListener(clickListener);
        pwdBtn3.setOnClickListener(clickListener);
        wifiRootLL.setOnClickListener(clickListener);
    }

    private void initData() {
        Device.getWifi(WifiActivity.this, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(getWindow().getDecorView(),false,true);
            }

            @Override
            public void onFinish() {
//                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();

                Log.d("服务器上的wifi数据", obj.getBodyArray().toString());

                if (obj.getRetcode() == 1) {
                    JSONObject json = JSONHelper.getJSONObject(obj.getBodyArray(), 0);
                    if(json!=null) {
                        wifiET.setText(sp.getString("wifi", JSONHelper.getString(json, "wifi")));
                        pwdET.setText(sp.getString("wifiPwd", JSONHelper.getString(json, "wifiPwd")));
                    }

                    JSONObject json2 = JSONHelper.getJSONObject(obj.getBodyArray(), 1);
                    if(json2 != null) {
                        wifiET2.setText(JSONHelper.getString(json2, "wifi"));
                        pwdET2.setText(JSONHelper.getString(json2, "wifiPwd"));
                    }

                    JSONObject json3 = JSONHelper.getJSONObject(obj.getBodyArray(), 2);
                    if(json3 != null) {
                        wifiET3.setText(JSONHelper.getString(json3, "wifi"));
                        pwdET3.setText(JSONHelper.getString(json3, "wifiPwd"));
                    }
                } else {
                    Toast.makeText(WifiActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                CommUtil.hideProcessing();
                Toast.makeText(WifiActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });


    }

    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    WifiActivity.this.finish();
                    break;
                case R.id.completeBtn:
                    wifiText = wifiET.getText().toString().trim();
                    wifiText2 = wifiET2.getText().toString().trim();
                    wifiText3 = wifiET3.getText().toString().trim();
                    pwdText = pwdET.getText().toString().trim();
                    pwdText2 = pwdET2.getText().toString().trim();
                    pwdText3 = pwdET3.getText().toString().trim();

                    if (wifiText.isEmpty() && wifiText2.isEmpty() && wifiText3.isEmpty()) {
                        Toast.makeText(WifiActivity.this, R.string.my_wifi_notnull, Toast.LENGTH_SHORT).show();
                    } else if (checkIllegalChar(wifiText, pwdText) || checkIllegalChar(wifiText2, pwdText2) ||
                            checkIllegalChar(wifiText3, pwdText3)) {
                        Toast.makeText(WifiActivity.this, R.string.my_wifi_forbidden, Toast.LENGTH_SHORT).show();
                    } else {
                        submitWifi();
                    }
                    break;
                case R.id.deleteBtn:
                    wifiET.setText("");
                    pwdET.setText("");
                    break;
                case R.id.pwdBtn:
                    if (ispwdBtn) {
                        //显示密码
                        pwdET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        pwdBtn.setImageResource(R.drawable.pwd_hide);
                        ispwdBtn = !ispwdBtn;
                    } else {
                        //隐藏密码
                        pwdET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pwdBtn.setImageResource(R.drawable.pwd_show);
                        ispwdBtn = !ispwdBtn;
                    }
                    break;
                case R.id.pwdBtn2:
                    if (ispwdBtn2) {
                        //显示密码
                        pwdET2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        pwdBtn2.setImageResource(R.drawable.pwd_hide);
                        ispwdBtn2 = !ispwdBtn2;
                    } else {
                        //隐藏密码
                        pwdET2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pwdBtn2.setImageResource(R.drawable.pwd_show);
                        ispwdBtn2 = !ispwdBtn2;
                    }
                    break;
                case R.id.pwdBtn3:
                    if (ispwdBtn3) {
                        //显示密码
                        pwdET3.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        pwdBtn3.setImageResource(R.drawable.pwd_hide);
                        ispwdBtn3 = !ispwdBtn3;
                    } else {
                        //隐藏密码
                        pwdET3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pwdBtn3.setImageResource(R.drawable.pwd_show);
                        ispwdBtn3 = !ispwdBtn3;
                    }
                    break;
                case R.id.wifiRootLL:
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    break;

            }
        }

        private boolean checkIllegalChar(String wifiStr, String pwdStr) {
            return wifiStr.contains("|") || wifiStr.contains(",") ||
                    pwdStr.contains("|") || pwdStr.contains(",");
        }
    };


    /**
     * 提交wifi信息
     */
    private void submitWifi() {
        ArrayList<WifiInfos> wifiInfoList = packageWifiInfo();

        Log.i("TAG", wifiInfoList.toString());

        if (null != wifiInfoList && wifiInfoList.size() > 0) {
            Device.saveWifis(WifiActivity.this, wifiInfoList, ChatActivity.mEntityImei, new BctClientCallback() {
                @Override
                public void onStart() {
                    WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, WifiActivity.this);
                }

                @Override
                public void onFinish() {
                    WizardAlertDialog.getInstance().closeProgressDialog();
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(WifiActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        WifiActivity.this.finish();
                    } else {
                        Toast.makeText(WifiActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                        Log.i("上传家庭wifi", obj.getMsg());
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(WifiActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }


//        Device.saveWifi(WifiActivity.this, wifiET.getText().toString(), pwdET.getText().toString(), ChatActivity.mEntityImei, new BctClientCallback() {
//            @Override
//            public void onStart() {
//                WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, WifiActivity.this);
//            }
//
//            @Override
//            public void onFinish() {
//                WizardAlertDialog.getInstance().closeProgressDialog();
//            }
//
//            @Override
//            public void onSuccess(ResponseData obj) {
//                if (obj.getRetcode() == 1) {
//                    Toast.makeText(WifiActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//                    WifiActivity.this.finish();
//                } else {
//                    Toast.makeText(WifiActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(String message) {
//                Toast.makeText(WifiActivity.this, message, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private ArrayList<WifiInfos> packageWifiInfo() {
        //提交之前先封装数据
        ArrayList<WifiInfos> wifiList = new ArrayList<>();

        if (CommUtil.isNotBlank(wifiText)) {
            WifiInfos wifiInfos = new WifiInfos();
            wifiInfos.setId("1");
            wifiInfos.setWifi(wifiText);
            wifiInfos.setWifiPwd(pwdText);
            wifiList.add(wifiInfos);
        }
        if (CommUtil.isNotBlank(wifiText2)) {
            WifiInfos wifiInfos = new WifiInfos();
            wifiInfos.setId("2");
            wifiInfos.setWifi(wifiText2);
            wifiInfos.setWifiPwd(pwdText2);
            wifiList.add(wifiInfos);
        }
        if (CommUtil.isNotBlank(wifiText3)) {
            WifiInfos wifiInfos = new WifiInfos();
            wifiInfos.setId("3");
            wifiInfos.setWifi(wifiText3);
            wifiInfos.setWifiPwd(pwdText3);
            wifiList.add(wifiInfos);
        }
        return wifiList;
    }

}
