package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.adapter.AlarmAdapter;
import com.bct.gpstracker.pojo.AlarmEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.vo.Session;

/**
 * 生活助手页面
 * @author huangfei
 *
 */
public class AlarmActivity extends BaseActivity {
	private ImageButton backButton,addButton;
	private ListView listView;
	private TextView alarmPrompt;

	private List<AlarmEntity> entityList = new ArrayList<AlarmEntity>();
	private AlarmAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);

		TextView titleTV = (TextView)findViewById(R.id.titleNameTV);
		titleTV.setText(getString(R.string.setup_alarm) + "-" + Session.getInstance().getSetupDevice().getName());
		listView = (ListView) findViewById(R.id.listView1);
		backButton = (ImageButton) findViewById(R.id.backBtn);
		addButton = (ImageButton) findViewById(R.id.addBtn);
		alarmPrompt = (TextView) findViewById(R.id.alarm_prompt);

		backButton.setOnClickListener(clickListener);
		addButton.setOnClickListener(clickListener);

		mAdapter = new AlarmAdapter(AlarmActivity.this, entityList);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long id) {
				Intent intent = new Intent(AlarmActivity.this, AddAlarmActivity.class);
				intent.putExtra("alarm", entityList.get(position));
				AlarmActivity.this.startActivity(intent);
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
				AlarmActivity.this.finish();
				break;
			case R.id.addBtn:
				Intent intent = new Intent(AlarmActivity.this, AddAlarmActivity.class);
				AlarmActivity.this.startActivity(intent);
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		getData();
	}

	/**
	 * 获取所有的设备
	 */
	private void getData(){
		AlarmEntity.getList(AlarmActivity.this,new BctClientCallback(){
			@Override
			public void onStart() {
                CommUtil.showProcessing(listView,true,true);
			}

			@Override
			public void onFinish() {
				CommUtil.hideProcessing();
			}

			@Override
			public void onSuccess(ResponseData obj) {
				if(obj.getRetcode() == 1){
					entityList.clear();
					for(int i=0;i<obj.getBodyArray().length();i++){
						AlarmEntity device = new AlarmEntity(JSONHelper.getJSONObject(obj.getBodyArray(), i));
						entityList.add(device);
					}
                    if(entityList.size()!=0)
                    {
                        listView.setVisibility(View.VISIBLE);
                        alarmPrompt.setVisibility(View.GONE);
                    }else{
                        listView.setVisibility(View.GONE);
                        alarmPrompt.setVisibility(View.VISIBLE);
                    }
					mAdapter.notifyDataSetChanged();
				}else{
					Toast.makeText(AlarmActivity.this,obj.getMsg(), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(String message) {
				Toast.makeText(AlarmActivity.this,message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
}
