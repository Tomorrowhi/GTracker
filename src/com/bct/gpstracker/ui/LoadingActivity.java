package com.bct.gpstracker.ui;

import android.app.Activity;
import android.os.Bundle;

import com.bct.gpstracker.R;

/**
 * Created by Admin on 2015/9/24 0024.
 * 错误弹出框的背景界面
 */
public class LoadingActivity extends Activity {

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        if (count == 2) {
            this.finish();
        }
    }


}

