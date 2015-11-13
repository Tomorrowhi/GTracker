package com.bct.gpstracker.ui;

import android.app.Activity;
import android.os.Bundle;
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
import com.bct.gpstracker.vo.Session;

/**
 * 找回密码设置新密码页面
 * @author huangfei
 *
 */
public class UpdatePwdActivity extends Activity {
	
	private ImageButton backButton;
	private Button completeButton;
	private EditText pwdText,confirmPwdText;
	
	private String phone = "";
	private String code = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_pwd);
		phone = getIntent().getStringExtra("phone");
		code = getIntent().getStringExtra("code");
		
		backButton =(ImageButton) findViewById(R.id.backBtn);
		completeButton = (Button) findViewById(R.id.completeBtn);
		pwdText = (EditText) findViewById(R.id.pwdET);
		confirmPwdText  =(EditText) findViewById(R.id.pwdConfirmET);
		
		backButton.setOnClickListener(clickListener);
		completeButton.setOnClickListener(clickListener);
		
	}
	
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backBtn:
				UpdatePwdActivity.this.finish();
				break;
			case R.id.completeBtn:
				String pwd=pwdText.getText().toString().trim();
				if(CommUtil.isBlank(pwd)){
					Toast.makeText(UpdatePwdActivity.this, R.string.pwd_null_err, Toast.LENGTH_SHORT).show();
				}else if(pwd.length() < 6 || pwd.length() > 20){
					Toast.makeText(UpdatePwdActivity.this, R.string.pwd_length_err, Toast.LENGTH_SHORT).show();
				}else if (!pwdText.getText().toString().trim().equals(confirmPwdText.getText().toString().trim())) {
					Toast.makeText(UpdatePwdActivity.this, R.string.pwd_confirm_err, Toast.LENGTH_SHORT).show();
				}else {
					if(phone!=null&&code!=null){
						updatePwd(phone,code,pwd);
					}
				}
				break;
			}
		}
	};
	
	
	/**
	 * 获取验证码
	 * @param phoneStr
	 */
	private void updatePwd(String phoneStr,String codeStr,String pwdStr){
		User.modifyPassword(this,phoneStr,pwdStr,codeStr,new BctClientCallback(){
			@Override
			public void onStart() {
				WizardAlertDialog.getInstance().showProgressDialog("更密码中", UpdatePwdActivity.this);
			}

			@Override
			public void onFinish() {
				WizardAlertDialog.getInstance().closeProgressDialog();
			}

			@Override
			public void onSuccess(ResponseData obj) {
				if(obj.getRetcode() == 1){
					Toast.makeText(UpdatePwdActivity.this, R.string.pwd_setup_success, Toast.LENGTH_SHORT).show();
					Session.getInstance().setNeedClose(true);
					UpdatePwdActivity.this.finish();
				}else{
					Toast.makeText(UpdatePwdActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
				}

			}

			@Override
			public void onFailure(String message) {
				Toast.makeText(UpdatePwdActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
//		try {
//			final JSONObject object = new JSONObject();
//			object.put("phone", phoneStr);
//			object.put("pwd", pwdStr);
//			object.put("validcode", codeStr);
//			HttpEntity entity = new StringEntity(object.toString());
//			AsyncHttpClient client = new AsyncHttpClient();
//			client.post(UpdatePwdActivity.this,
//					Utils.getMetaValue(UpdatePwdActivity.this, "base_url")+CommonRestPath.modifyPwd(),
//					entity, "", new AsyncHttpResponseHandler(){
//				@Override
//				public void onSuccess(int statusCode, String content) {
//				//	System.out.println(content);
//					String retCode = "";
//					try {
//						JSONObject object = new JSONObject(content);
//						if(object.has("head")){
//							retCode = object.getJSONObject("head").getString("retcode");
//						}
//						if(!retCode.equals("")){
//							if(retCode.equals("1")){
//								Session.getInstance().setNeedClose(true);
//								Toast.makeText(UpdatePwdActivity.this, R.string.pwd_setup_success, Toast.LENGTH_SHORT).show();
//								UpdatePwdActivity.this.finish();
//							}else{
//								Toast.makeText(UpdatePwdActivity.this, object.getJSONObject("head").getString("msg"), Toast.LENGTH_SHORT).show();
//							}
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				@Override
//				public void onFailure(Throwable error, String content) {
//				//	System.err.println(content);
//				}
//				@Override
//				public void onFinish() {
////					WizardAlertDialog.getInstance().closeProgressDialog();
//				}
//				@Override
//				public void onStart() {
////					System.out.println("-------请求参数:"+object.toString());
////					System.out.println("接口URL："+Utils.getMetaValue(UpdatePwdActivity.this, "base_url")+CommonRestPath.modifyPwd());
////					WizardAlertDialog.getInstance().showProgressDialog(R.string.add_data, RegistActivity.this);
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
}
