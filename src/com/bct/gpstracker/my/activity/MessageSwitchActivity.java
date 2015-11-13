package com.bct.gpstracker.my.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.MessageSwitchEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnCompoundButtonCheckedChange;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * 消息开关页面
 * @author huangfei
 *
 */
public class MessageSwitchActivity extends BaseActivity {
    private ImageButton backButton;
	private ToggleButton sosButton,fallButton,lowBattButton,fenceButton,wifiButton,downBtn,vibrateBtn,shiftBtn;

    private MessageSwitchEntity mEntity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_switch);

		backButton = (ImageButton) findViewById(R.id.backBtn);
//		sosButton = (ToggleButton) findViewById(R.id.sosBtn);
//		fallButton = (ToggleButton) findViewById(R.id.fallBtn);
//		lowBattButton = (ToggleButton) findViewById(R.id.lowBattBtn);
//		fenceButton = (ToggleButton) findViewById(R.id.fenceBtn);
//		wifiButton = (ToggleButton) findViewById(R.id.wifiBtn);
//		downBtn = (ToggleButton) findViewById(R.id.downBtn);
//		vibrateBtn = (ToggleButton) findViewById(R.id.vibrateBtn);
//		shiftBtn = (ToggleButton) findViewById(R.id.shiftBtn);
		backButton.setOnClickListener(clickListener);
		
		
//		sosButton.setOnClickListener(clickListener);
//		fallButton.setOnClickListener(clickListener);
//		lowBattButton.setOnClickListener(clickListener);
//		fenceButton.setOnClickListener(clickListener);
//		wifiButton.setOnClickListener(clickListener);
//
//
//		downBtn.setOnClickListener(clickListener);
//		vibrateBtn.setOnClickListener(clickListener);
//		shiftBtn.setOnClickListener(clickListener);

//		getMessageSwitch();

        ToggleButton voiceBtn=(ToggleButton)findViewById(R.id.voiceBtn);;
        ToggleButton vibrateButton=(ToggleButton)findViewById(R.id.vibrateButton);
        SharedPreferences pres=Utils.getPreferences(this);
        voiceBtn.setChecked(pres.getBoolean(Constants.MSG_VOICE, true));
        vibrateButton.setChecked(pres.getBoolean(Constants.MSG_VIBRATE,true));

        ViewUtils.inject(this);
	}
	
	/**
	 * 初始化页面
	 */
	private void initView(){
		if(mEntity!=null){
            sosButton.setChecked(mEntity.getSos()!=1);
            lowBattButton.setChecked(mEntity.getLowBatt()!=1);
            fenceButton.setChecked(mEntity.getFence()!=1);
            wifiButton.setChecked(mEntity.getWifi()!=1);
            downBtn.setChecked(mEntity.getDown()!=1);
            vibrateBtn.setChecked(mEntity.getVibrate()!=1);
            shiftBtn.setChecked(mEntity.getShift()!=1);
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
				MessageSwitchActivity.this.finish();
				break;
			case R.id.sosBtn:
                setMessageSwitch("sos", sosButton.isChecked());
                break;
			case R.id.lowBattBtn:
                setMessageSwitch("lowBatt", lowBattButton.isChecked());
				break;
			case R.id.fenceBtn:
                setMessageSwitch("fence", fenceButton.isChecked());
				break;
			case R.id.wifiBtn:
                setMessageSwitch("wifi", wifiButton.isChecked());
				break;
			case R.id.downBtn:
				setMessageSwitch("down", downBtn.isChecked());
				break;
			case R.id.vibrateBtn:
				setMessageSwitch("vibrate", vibrateBtn.isChecked());
				break;
			case R.id.shiftBtn:
				setMessageSwitch("shift", shiftBtn.isChecked());
				break;
			}
		}
	};
	
	/**
	 * 
	 */
//	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
//		@Override
//		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//			switch (buttonView.getId()){
//			case R.id.sosBtn:
//				sosButton.setChecked(isChecked);
//				setMessageSwitch("sos", isChecked);
//				break;
//			case R.id.lowBattBtn:
//				lowBattButton.setChecked(isChecked);
//				setMessageSwitch("lowBatt", isChecked);
//				break;
//			case R.id.fenceBtn:
//				fenceButton.setChecked(isChecked);
//				setMessageSwitch("fence", isChecked);
//				break;
//			case R.id.wifiBtn:
//				wifiButton.setChecked(isChecked);
//				setMessageSwitch("wifi", isChecked);
//				break;
//			}
//		}
//	};
	
	
	/**
	 * 获取初始开关状态
	 */
	private void getMessageSwitch(){
		try{
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("accesskey", Session.getInstance().getAccessKey());
		client.post(Utils.getMetaValue(MessageSwitchActivity.this,"base_url")+CommonRestPath.getMessageSwitch(), new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                CommUtil.showProcessing(getWindow().getDecorView(),false,true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                CommUtil.hideProcessing();
                try {
                    if(response.has("body")){
                        JSONObject object = response.getJSONObject("body");
                        mEntity = new MessageSwitchEntity(object);
                        initView();
                    }
                } catch (Exception e) {
                    CommUtil.sendMsg(CommHandler.TOAST_LONG, getString(R.string.retrieve_setting_sw_failed));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                CommUtil.hideProcessing();
                CommUtil.sendMsg(CommHandler.TOAST_LONG, getString(R.string.retrieve_setting_sw_failed));
            }

			@Override
			public void onFinish() {
			}

		});
		}catch (Exception e) {
			e.printStackTrace();
		//	System.out.println("Exception"+e);
		}
	}

    /**
     * 设置开关状态
     */
    private void setMessageSwitch(String type, boolean status) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", type);
            object.put("content", status ? 0 : 1);

            HttpEntity entity = new StringEntity(object.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("accesskey", Session.getInstance().getAccessKey());
            client.post(MessageSwitchActivity.this, Utils.getMetaValue(MessageSwitchActivity.this, "base_url") + CommonRestPath.setMessageSwitch(), entity, "",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onStart() {
                            CommUtil.showProcessing(getWindow().getDecorView(),false,true);
                        }

                        @Override
                        public void onFinish() {
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            CommUtil.hideProcessing();
                            ResponseData responseData = new ResponseData(response);
                            if (CommUtil.isBlank(responseData.getMsg())) {
                                CommUtil.sendMsg(CommHandler.TOAST_SHORT, getString(R.string.setting_sw_success));
                            } else {
                                CommUtil.sendMsg(CommHandler.TOAST_SHORT, responseData.getMsg());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            CommUtil.hideProcessing();
                            CommUtil.sendMsg(CommHandler.TOAST_SHORT, getString(R.string.setting_sw_failed));
                        }


                    });
        } catch (Exception e) {
            CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
            CommUtil.sendMsg(CommHandler.TOAST_SHORT, getString(R.string.setting_sw_failed));
        }
    }

    @OnCompoundButtonCheckedChange({R.id.voiceBtn,R.id.vibrateButton})
    private void onMessageSwitchChanged(CompoundButton btn, boolean isChecked){
        SharedPreferences pres=Utils.getPreferences(this);
        switch (btn.getId()){
            case R.id.voiceBtn:
                pres.edit().putBoolean(Constants.MSG_VOICE,isChecked).apply();
                break;
            case R.id.vibrateButton:
                pres.edit().putBoolean(Constants.MSG_VIBRATE,isChecked).apply();
                break;
        }
    }

}
