package com.bct.gpstracker.my.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.my.adapter.TimeAdapter;
import com.bct.gpstracker.pojo.WeekDayTimes;
import com.bct.gpstracker.view.MyListView;

public class ChoiceTimeActivity extends BaseActivity{
	
	private String[] dateStrs = new String[] {
		    "", "01", "02", "03", "04","05", "06", "07", "08", "09",
		    "10", "11", "12", "13", "14","15", "16", "17", "18", "19",
		    "20", "21", "22", "23","24"
		    };
	
	private String[] mondayStrs = new String[] {
	    "一", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] tuesdayStrs = new String[] {
	    "二",  "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] wednesdayStrs = new String[] {
	    "三", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] thursdayStrs = new String[] {
	    "四", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] fridayStrs = new String[] {
	    "五",  "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] saturdayStrs = new String[] {
	    "六", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	private String[] sundayStrs = new String[] {
	    "日",  "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0", "0","0", "0", "0", "0", "0",
	    "0", "0", "0", "0","0"
	    };
	
	private MyListView dateListView,mondayListView,tuesdayListView,wednesdayListView,thursdayListView,
						fridayListView,saturdayListView,sundayListView;
	
	TimeAdapter dateAdapter;
	TimeAdapter mondayAdapter;
	TimeAdapter tuesdayAdapter;
	TimeAdapter wednesdayAdapter;
	TimeAdapter thursdayAdapter;
	TimeAdapter fridayAdapter;
	TimeAdapter saturdayAdapter;
	TimeAdapter sundayAdapter;
	
	private WeekDayTimes mDayTimes = null;
	
	private ImageButton backButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice_time);
		
		if(getIntent().getSerializableExtra("time")!=null){
			mDayTimes = (WeekDayTimes) getIntent().getSerializableExtra("time");
		}
		
		dateListView = (MyListView) findViewById(R.id.dateListView);
		mondayListView = (MyListView) findViewById(R.id.mondayListView);
		tuesdayListView = (MyListView) findViewById(R.id.tuesdayListView);
		wednesdayListView = (MyListView) findViewById(R.id.wendnesdayListView);
		thursdayListView = (MyListView) findViewById(R.id.thursdayListView);
		fridayListView = (MyListView) findViewById(R.id.fridayListView);
		saturdayListView = (MyListView) findViewById(R.id.saturdayListView);
		sundayListView = (MyListView) findViewById(R.id.sundayListView);
		backButton = (ImageButton) findViewById(R.id.backBtn);
		
		
		
		 dateAdapter = new TimeAdapter(ChoiceTimeActivity.this, dateStrs, "date");
		 mondayAdapter = new TimeAdapter(ChoiceTimeActivity.this, mondayStrs, "week");
		 tuesdayAdapter = new TimeAdapter(ChoiceTimeActivity.this, tuesdayStrs, "week");
		 wednesdayAdapter = new TimeAdapter(ChoiceTimeActivity.this, wednesdayStrs, "week");
		 thursdayAdapter = new TimeAdapter(ChoiceTimeActivity.this, thursdayStrs, "week");
		 fridayAdapter = new TimeAdapter(ChoiceTimeActivity.this, fridayStrs, "week");
		 saturdayAdapter = new TimeAdapter(ChoiceTimeActivity.this, saturdayStrs, "week");
		 sundayAdapter = new TimeAdapter(ChoiceTimeActivity.this, sundayStrs, "week");
		dateListView.setAdapter(dateAdapter);
		mondayListView.setAdapter(mondayAdapter);
		tuesdayListView.setAdapter(tuesdayAdapter);
		wednesdayListView.setAdapter(wednesdayAdapter);
		thursdayListView.setAdapter(thursdayAdapter);
		fridayListView.setAdapter(fridayAdapter);
		saturdayListView.setAdapter(saturdayAdapter);
		sundayListView.setAdapter(sundayAdapter);
		
		mondayListView.setOnItemClickListener(itemClickListener);
		tuesdayListView.setOnItemClickListener(itemClickListener);
		wednesdayListView.setOnItemClickListener(itemClickListener);
		thursdayListView.setOnItemClickListener(itemClickListener);
		fridayListView.setOnItemClickListener(itemClickListener);
		saturdayListView.setOnItemClickListener(itemClickListener);
		sundayListView.setOnItemClickListener(itemClickListener);
		
		backButton.setOnClickListener(clickListener);
	}
	
	/**
	 * 初始化视图
	 */
	private void initView(){
		if(mDayTimes!=null){
			if(mDayTimes.getMondayST()==null||mDayTimes.getMondayST().equals("")){
				
			}else {
				
			}
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
		        Bundle bundle = new Bundle(); 
		        bundle.putSerializable("time", mDayTimes);
				setResult(RESULT_OK, ChoiceTimeActivity.this.getIntent().putExtras(bundle)); 
				ChoiceTimeActivity.this.finish();
				break;
			}
		}
	};
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
	    // 是否触发按键为back键    
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        // 实例化 Bundle，设置需要传递的参数 
	        Bundle bundle = new Bundle(); 
	        bundle.putSerializable("time", mDayTimes);
	        setResult(RESULT_CANCELED, this.getIntent().putExtras(bundle)); 
	        this.finish(); 
	        return true; 
	    }
	    return super.onKeyDown(keyCode, event); 
	}
	
	/**
	 * 列表点击
	 */
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
			switch (adapterView.getId()){
			case R.id.mondayListView:
				if(position!=0){
					if(mondayStrs[position].equals("0")){
						int key = getKey(mondayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									mondayStrs[i] = "1";
								}
								mondayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									mondayStrs[i] = "1";
								}
								mondayAdapter.notifyDataSetChanged();
							}
						}else {
							mondayStrs[position] = "1";
							mondayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==mondayStrs.length){
							mondayStrs[position] = "0";
							mondayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							mondayStrs[position] = "0";
							mondayAdapter.notifyDataSetChanged();
						}else {
							if(!mondayStrs[position-1].equals("0")&&!mondayStrs[position+1].equals("0")){
							}else {
								mondayStrs[position] = "0";
								mondayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.tuesdayListView:
				if(position!=0){
					if(tuesdayStrs[position].equals("0")){
						int key = getKey(tuesdayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									tuesdayStrs[i] = "1";
								}
								tuesdayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									tuesdayStrs[i] = "1";
								}
								tuesdayAdapter.notifyDataSetChanged();
							}
						}else {
							tuesdayStrs[position] = "1";
							tuesdayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==tuesdayStrs.length){
							tuesdayStrs[position] = "0";
							tuesdayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							tuesdayStrs[position] = "0";
							tuesdayAdapter.notifyDataSetChanged();
						}else {
							if(!tuesdayStrs[position-1].equals("0")&&!tuesdayStrs[position+1].equals("0")){
							}else {
								tuesdayStrs[position] = "0";
								tuesdayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.wendnesdayListView:
				if(position!=0){
					if(wednesdayStrs[position].equals("0")){
						int key = getKey(wednesdayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									wednesdayStrs[i] = "1";
								}
								wednesdayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									wednesdayStrs[i] = "1";
								}
								wednesdayAdapter.notifyDataSetChanged();
							}
						}else {
							wednesdayStrs[position] = "1";
							wednesdayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==wednesdayStrs.length){
							wednesdayStrs[position] = "0";
							wednesdayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							wednesdayStrs[position] = "0";
							wednesdayAdapter.notifyDataSetChanged();
						}else {
							if(!wednesdayStrs[position-1].equals("0")&&!wednesdayStrs[position+1].equals("0")){
							}else {
								wednesdayStrs[position] = "0";
								wednesdayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.thursdayListView:
				if(position!=0){
					if(thursdayStrs[position].equals("0")){
						int key = getKey(thursdayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									thursdayStrs[i] = "1";
								}
								thursdayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									thursdayStrs[i] = "1";
								}
								thursdayAdapter.notifyDataSetChanged();
							}
						}else {
							thursdayStrs[position] = "1";
							thursdayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==thursdayStrs.length){
							thursdayStrs[position] = "0";
							thursdayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							thursdayStrs[position] = "0";
							thursdayAdapter.notifyDataSetChanged();
						}else {
							if(!thursdayStrs[position-1].equals("0")&&!thursdayStrs[position+1].equals("0")){
							}else {
								thursdayStrs[position] = "0";
								thursdayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.fridayListView:
				if(position!=0){
					if(fridayStrs[position].equals("0")){
						int key = getKey(fridayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									fridayStrs[i] = "1";
								}
								fridayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									fridayStrs[i] = "1";
								}
								fridayAdapter.notifyDataSetChanged();
							}
						}else {
							fridayStrs[position] = "1";
							fridayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==fridayStrs.length){
							fridayStrs[position] = "0";
							fridayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							fridayStrs[position] = "0";
							fridayAdapter.notifyDataSetChanged();
						}else {
							if(!fridayStrs[position-1].equals("0")&&!fridayStrs[position+1].equals("0")){
							}else {
								fridayStrs[position] = "0";
								fridayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.saturdayListView:
				if(position!=0){
					if(saturdayStrs[position].equals("0")){
						int key = getKey(saturdayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									saturdayStrs[i] = "1";
								}
								saturdayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									saturdayStrs[i] = "1";
								}
								saturdayAdapter.notifyDataSetChanged();
							}
						}else {
							saturdayStrs[position] = "1";
							saturdayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==saturdayStrs.length){
							saturdayStrs[position] = "0";
							saturdayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							saturdayStrs[position] = "0";
							saturdayAdapter.notifyDataSetChanged();
						}else {
							if(!saturdayStrs[position-1].equals("0")&&!saturdayStrs[position+1].equals("0")){
							}else {
								saturdayStrs[position] = "0";
								saturdayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			case R.id.sundayListView:
				if(position!=0){
					if(sundayStrs[position].equals("0")){
						int key = getKey(sundayStrs, position);
						if(key!=0){
							if(key<position){
								for(int i=key;i<position+1;i++){
									sundayStrs[i] = "1";
								}
								sundayAdapter.notifyDataSetChanged();
							}else {
								for(int i=position;i<key+1;i++){
									sundayStrs[i] = "1";
								}
								sundayAdapter.notifyDataSetChanged();
							}
						}else {
							sundayStrs[position] = "1";
							sundayAdapter.notifyDataSetChanged();
						}
					}else{
						if((position+1)==sundayStrs.length){
							sundayStrs[position] = "0";
							sundayAdapter.notifyDataSetChanged();
						}else if ((position-1)==0) {
							sundayStrs[position] = "0";
							sundayAdapter.notifyDataSetChanged();
						}else {
							if(!sundayStrs[position-1].equals("0")&&!sundayStrs[position+1].equals("0")){
							}else {
								sundayStrs[position] = "0";
								sundayAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				break;
			}
		}
	};
	
	/**
	 * 取得被选中的位置
	 * @param strings
	 * @param position
	 * @return
	 */
	private int getKey(String[] strings,int position){
		int key = 0;
		for(int i=1;i<strings.length;i++){
			if(strings[i].equals("1")){
				key = i;
			}
		}
		return key;
	}
	
	/**
	 * 处理最终的数据
	 */
	private void handleData(){
		boolean isFirst = true;
		for(int i=1;i<mondayStrs.length;i++){
			if(mondayStrs[i].equals("1")){
				mDayTimes.setMondayST("0"+i+":00");
			}
		}
	}
	
}
