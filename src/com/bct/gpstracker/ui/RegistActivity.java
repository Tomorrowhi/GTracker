package com.bct.gpstracker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Session;

/**
 * 用户注册页面
 *
 * @author huangfei
 */
public class RegistActivity extends Activity implements OnClickListener {

    private ImageButton backButton;
    private Button registButton, validButton;
    private EditText phoneText, pwdText, pwdConfirmText, validText, deviceET;
    private TimeCount timeCount;
    private ProgressBar progressBar;
    private TextView nofityView, protocolView;    //错误提示
    private boolean isValid = false;    //验证号码是否可用
    private LinearLayout protocolLayout,registLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        timeCount = new TimeCount(Constants.VALIDCODE_TOTAL_TIME, Constants.VALIDCODE_APART_TIME);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        registButton = (Button) findViewById(R.id.registBtn);
        validButton = (Button) findViewById(R.id.validBtn);
        phoneText = (EditText) findViewById(R.id.phoneET);
        pwdText = (EditText) findViewById(R.id.pwdET);
        pwdConfirmText = (EditText) findViewById(R.id.pwdConfirmET);
        validText = (EditText) findViewById(R.id.validET);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        nofityView = (TextView) findViewById(R.id.notifyView);
        protocolView = (TextView) findViewById(R.id.protocolTV);
        protocolView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        protocolLayout = (LinearLayout) findViewById(R.id.protocolLayout);
        deviceET = (EditText) findViewById(R.id.deviceET);
        registLL = (LinearLayout) findViewById(R.id.registLL);

        backButton.setOnClickListener(this);
        registButton.setOnClickListener(this);
        validButton.setOnClickListener(this);
        protocolView.setOnClickListener(this);
        registLL.setOnClickListener(this);
//        findViewById(R.id.scanBtn).setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.scanBtn:
//                Intent openCameraIntent = new Intent(RegistActivity.this, CaptureActivity.class);
//                openCameraIntent.putExtra("type", "register");
//                startActivityForResult(openCameraIntent, 0);
//                break;
            case R.id.backBtn:
                RegistActivity.this.finish();
                break;
            case R.id.protocolTV:
                Intent pIntent = new Intent(RegistActivity.this, ProtocolActivity.class);
                RegistActivity.this.startActivity(pIntent);
                break;
            case R.id.registBtn:
                String pwd = pwdText.getText().toString().trim();
                if (CommUtil.isBlank(pwd)) {
                    Toast.makeText(RegistActivity.this, R.string.pwd_null_err, Toast.LENGTH_SHORT).show();
                } else if (CommUtil.isBlank(validText.getText())) {
                    Toast.makeText(RegistActivity.this, R.string.valid_null_err, Toast.LENGTH_SHORT).show();
                } else if (pwd.length() < 6 || pwd.length() > 20) {
                    Toast.makeText(RegistActivity.this, R.string.pwd_length_err, Toast.LENGTH_SHORT).show();
                } else if (!pwdConfirmText.getText().toString().trim().equals(pwdText.getText().toString().trim())) {
                    Toast.makeText(RegistActivity.this, R.string.pwd_confirm_err, Toast.LENGTH_SHORT).show();
                } else {
                    regist(phoneText.getText().toString().trim(), pwdText.getText().toString().trim(),
                            validText.getText().toString().trim());
                }
                break;
            case R.id.validBtn:
                //允许使用平台的用户名密码登录
//				if(phoneText.getText().toString().trim().length() != 11){
//					Toast.makeText(RegistActivity.this, "手机号格式不正确", Toast.LENGTH_SHORT).show();
//				}else{}

                /*用户点击 获取验证码 进行号码校验*/
                //获得用户输入的手机号码
                String phoneNumber = phoneText.getText().toString().trim();
                //进行校验
                if (!Utils.isMobileNO(phoneNumber)) {
                    Toast.makeText(RegistActivity.this, R.string.phone_err_, Toast.LENGTH_SHORT).show();
                }else{
                    checkPhoneAndRetrieveVaildCode(phoneNumber);
                }
                break;
            case R.id.registLL:
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                break;
        }
    }

    /**
     * 校验手机号码是否已经被注册
     * @param phoneNumber 用户输入的手机号
     * @return  true 已经被注册  false 未被注册
     */
    private void checkPhoneAndRetrieveVaildCode(final String phoneNumber) {

        User.checkPhone(RegistActivity.this, phoneNumber, new BctClientCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    //用户存在
                    Toast.makeText(RegistActivity.this,R.string.phone_err_exist,Toast.LENGTH_SHORT).show();
                } else if (obj.getRetcode() == 3) {
                    //用户不存在
                    getValid(phoneNumber);
                    timeCount.start();
                } else {
                    Toast.makeText(RegistActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                Utils.getManager(RegistActivity.this).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }


    /**
     * 获取验证码
     *
     * @param phoneStr
     */
    private void getValid(String phoneStr) {
        User.getValdcode(this, phoneStr, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.send_valid_code, RegistActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                Toast.makeText(RegistActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                if (obj.getRetcode() == 0) {
                    timeCount.cancel();
                    timeCount.onFinish();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(RegistActivity.this, message, Toast.LENGTH_SHORT).show();
                timeCount.cancel();
                timeCount.onFinish();
            }
        });
    }

    /**
     * 注册
     *
     * @param phoneStr
     */
    private void regist(final String phoneStr, String pwdStr, String codeStr) {
        User.register(this, phoneStr, pwdStr, codeStr, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.regist_post_data, RegistActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    Session.getInstance().setAccessKey(JSONHelper.getString(obj.getBody(), "accesskey"));

                    User user = new User();
                    user.setPhone(phoneStr);
                    user.setAppUserNum(JSONHelper.getString(obj.getBody(), "appUserNum"));
                    Session.getInstance().setUser(user);

                    Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegistActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
            }
        });
    }


    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            validButton.setText(R.string.get_valid_hint);
            validButton.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            validButton.setClickable(false);
            validButton.setText(millisUntilFinished / 1000 + getResources().getString(R.string.time_minus_hint));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            deviceET.setText(scanResult);
        }
    }

    @Override
    protected void onDestroy() {
        /*退出注册页面时，更改注册标记为初始值*/
        super.onDestroy();
    }
}
