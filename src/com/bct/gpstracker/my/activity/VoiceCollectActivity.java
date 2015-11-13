package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.my.adapter.CollectAdapter;
import com.bct.gpstracker.pojo.VoiceCollectEntity;

/**
 * 语音收藏页面
 * @author huangfei
 *
 */
public class VoiceCollectActivity extends BaseActivity {
	
	private ImageButton backButton;
	private ListView listView;
	private List<VoiceCollectEntity> entityList = new ArrayList<VoiceCollectEntity>();
	private CollectAdapter mAdapter ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_collect);
		
		backButton = (ImageButton) findViewById(R.id.backBtn);
		listView = (ListView) findViewById(R.id.listView1);
		mAdapter = new CollectAdapter(VoiceCollectActivity.this, entityList);
		listView.setAdapter(mAdapter);
		getData();
		
		backButton.setOnClickListener(clickListener);
	
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backBtn:
				VoiceCollectActivity.this.finish();
				break;
			}
		}
	};
	
	
	private void getData(){
		entityList.clear();
		for(int i=0;i<7;i++){
			VoiceCollectEntity entity = new VoiceCollectEntity();
			entity.setName("Rebecca"+(i+1));
			if(i%2==0){
				entity.setCollectTime("2015-01-20");
			}else {
				entity.setCollectTime("2015-01-28");
			}
			entityList.add(entity);
		}
		mAdapter.notifyDataSetChanged();
	}
	
}
