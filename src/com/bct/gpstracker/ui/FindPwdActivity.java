package com.bct.gpstracker.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bct.gpstracker.R;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Session;

/**
 * 找回密码获取验证码页面
 * @author huangfei
 *
 */
public class FindPwdActivity extends Activity {
	private ImageButton backButton;
	private Button validButton,nextButton;
	private TimeCount timeCount;
	private EditText phoneText,validText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_pwd);
		 timeCount = new TimeCount(120000, 1000);
		
		backButton =(ImageButton) findViewById(R.id.backBtn);
		validButton = (Button) findViewById(R.id.validBtn);
		nextButton = (Button) findViewById(R.id.nextBtn);
		phoneText = (EditText) findViewById(R.id.phoneET);
		validText = (EditText) findViewById(R.id.validET);
		
		backButton.setOnClickListener(clickListener);
		validButton.setOnClickListener(clickListener);
		nextButton.setOnClickListener(clickListener);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(Session.getInstance().isNeedClose()==true){
			FindPwdActivity.this.finish();
			Session.getInstance().setNeedClose(false);
		}
	}
	
	/**
	 * 点击事件
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backBtn:
				FindPwdActivity.this.finish();
				break;
			case R.id.validBtn:
                String phone=phoneText.getText().toString().trim();
				if(CommUtil.isBlank(phone)||!Utils.isMobileNO(phone)){
					Toast.makeText(FindPwdActivity.this, R.string.phone_err_, Toast.LENGTH_SHORT).show();
				}else {
					getValid(phoneText.getText().toString().trim());
					timeCount.start();
				}
				break;
			case R.id.nextBtn:
				if(validText.getText().toString().trim().equals("")){
					Toast.makeText(FindPwdActivity.this, R.string.valid_null_err, Toast.LENGTH_SHORT).show();
				}else {
					Intent intent = new Intent(FindPwdActivity.this, UpdatePwdActivity.class);
					intent.putExtra("phone", phoneText.getText().toString().trim());
					intent.putExtra("code", validText.getText().toString().trim());
					FindPwdActivity.this.startActivity(intent);
				}
				break;
			}
		}
	};
	
	
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
		public void onTick(long millisUntilFinished){//计时过程显示
			validButton.setClickable(false);
			validButton.setText(millisUntilFinished /1000+getResources().getString(R.string.time_minus_hint));
		}
	}
	
	/**
	 * 获取验证码
	 * @param phoneStr
	 */
	private void getValid(String phoneStr){
		User.getValdcode(this, phoneStr, new BctClientCallback() {
			@Override
			public void onStart() {
				WizardAlertDialog.getInstance().showProgressDialog(R.string.send_valid_code, FindPwdActivity.this);
			}

			@Override
			public void onFinish() {
				WizardAlertDialog.getInstance().closeProgressDialog();
			}

			@Override
			public void onSuccess(ResponseData obj) {
				Toast.makeText(FindPwdActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                if(obj.getRetcode()==0){
                    timeCount.cancel();
                    timeCount.onFinish();
                }
			}

			@Override
			public void onFailure(String message) {
				Toast.makeText(FindPwdActivity.this, message, Toast.LENGTH_SHORT).show();
                timeCount.cancel();
                timeCount.onFinish();
			}
		});
	}
	
}
