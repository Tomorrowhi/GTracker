package com.bct.gpstracker.my.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;

public class SettingActivity extends BaseActivity {

    private ImageButton backButton;
    private EditText chatServerIp,serverUrl;
    private Button commitButton,closeSocket;
    private TextView loginSN;

    private RadioButton naviMapExternal,naviMapInternal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ViewUtils.inject(this);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        chatServerIp = (EditText) findViewById(R.id.chat_server_ip);
        serverUrl = (EditText) findViewById(R.id.server_url);
        commitButton = (Button) findViewById(R.id.commitBtn);
        closeSocket=(Button)findViewById(R.id.close_socket);
        loginSN=(TextView)findViewById(R.id.curr_login_sn);
        naviMapExternal=(RadioButton)findViewById(R.id.navi_map_external);
        naviMapInternal=(RadioButton)findViewById(R.id.navi_map_internal);

        long loginedUserId= Session.getInstance().getLoginedUserId();
        if(loginedUserId>0){
            loginSN.setText("当前用户编号："+Session.getInstance().getImei());
        }

        init();

        backButton.setOnClickListener(clickListener);
        commitButton.setOnClickListener(clickListener);
        closeSocket.setOnClickListener(clickListener);
    }

    private void init() {
        SharedPreferences preferences= Utils.getPreferences(this);
        serverUrl.setText(preferences.getString(Constants.SETTING_SERVER_URL,Constants.DEFAULT_BLANK));
        chatServerIp.setText(preferences.getString(Constants.SETTING_SERVER_IP,Constants.DEFAULT_BLANK));
        naviMapExternal.setChecked(preferences.getBoolean(Constants.SETTING_NAVI_MAP,true));
        naviMapInternal.setChecked(!naviMapExternal.isChecked());
    }

    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    SettingActivity.this.finish();
                    break;
                case R.id.commitBtn:
                    saveAll();
                    break;
                case R.id.close_socket:
                    CommunicationService conn=CommunicationService.get();
                    if(conn!=null) {
                        conn.client.close();
                    }
                    Toast.makeText(SettingActivity.this,String.format(getString(R.string.success),getString(R.string.close_socket)),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void saveAll() {
        SharedPreferences preferences = Utils.getPreferences(this);
        String ip = chatServerIp.getText().toString().trim();
        if (CommUtil.isNotBlank(ip) && !ip.matches(Constants.REGX_IP_PORT)) {
            Toast.makeText(this, R.string.error_ip_port, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(Constants.SETTING_SERVER_IP, ip);

        String seURL = serverUrl.getText().toString().trim();
        if (CommUtil.isNotBlank(seURL) && !seURL.startsWith("http://")) {
            seURL = "http://" + seURL;
        }
        editor.putString(Constants.SETTING_SERVER_URL, seURL);

        //导航地图使用
        editor.putBoolean(Constants.SETTING_NAVI_MAP, naviMapExternal.isChecked());

        editor.apply();
        Toast.makeText(this, String.format(getString(R.string.success), getString(R.string.save)), Toast.LENGTH_SHORT).show();
    }
}
