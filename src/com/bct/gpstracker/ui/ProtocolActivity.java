package com.bct.gpstracker.ui;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.my.activity.SettingActivity;
import com.bct.gpstracker.pojo.Setting;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.FileUtils;
import com.lidroid.xutils.db.sqlite.Selector;

/**
 * 用户协议页面
 *
 * @author huangfei
 */
public class ProtocolActivity extends Activity {

    private ImageButton backButton;
    private TextView protocol,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        protocol=(TextView)findViewById(R.id.protocol);
        title=(TextView)findViewById(R.id.titleNameTV);
        loadProtocol();
        protocol.setMovementMethod(ScrollingMovementMethod.getInstance());
        backButton.setOnClickListener(clickListener);
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(ProtocolActivity.this, SettingActivity.class);
                startActivity(intent);
                return false;
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
                    ProtocolActivity.this.finish();
                    break;
            }
        }
    };

    private void loadProtocol() {
        try {
            String ptl=null;
            Setting setting= AppContext.db.findFirst(Selector.from(Setting.class).where("key","=", Constants.PROTOCOL_KEY));
            if(setting!=null&& CommUtil.isNotBlank(setting.getValue())){
                ptl=setting.getValue();
            }else{
                InputStream in=getAssets().open("protocol.txt");
                byte[] bytes=FileUtils.readInputStream(in);
                if(bytes!=null&&bytes.length>0){
                    ptl=new String(bytes);
                }
            }
            protocol.setText(ptl);
        } catch (Exception e) {
            Log.e(Constants.TAG,"载入用户协议失败！",e);
        }
    }

}
