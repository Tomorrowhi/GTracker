package com.bct.gpstracker.my.activity;

import android.os.Bundle;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.lidroid.xutils.ViewUtils;

/**
 * Created by Admin on 2015/8/27 0027.
 */
public class MessageNotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_notification);
        ViewUtils.inject(this);

    }
}
