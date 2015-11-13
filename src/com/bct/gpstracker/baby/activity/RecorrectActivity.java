package com.bct.gpstracker.baby.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.amap.api.maps.MapView;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;

public class RecorrectActivity extends BaseActivity {
	
	private ImageButton backButton;
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorrect);
		
		mapView = (MapView)findViewById(R.id.bmapView);

		backButton = (ImageButton) findViewById(R.id.backBtn);
		backButton.setOnClickListener(clickListener);
		
	}
	
	/**
	 * 点击事件
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.backBtn:
				RecorrectActivity.this.finish();
				break;
			}
		}
	};
}
