package com.bct.gpstracker.my.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.NoWifiDialog;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.util.AppUpdataHandle;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.BadgeView;
import com.lurencun.service.autoupdate.Version;
import com.lurencun.service.autoupdate.internal.FoundVersionDialog;
import com.lurencun.service.autoupdate.internal.NetworkUtil;
import com.lurencun.service.autoupdate.internal.VersionDialogListener;
import com.lurencun.service.autoupdate.internal.VersionPersistent;

public class AboutUsActivity extends BaseActivity {

    private static final String TAG = AboutUsActivity.class.getSimpleName();
    private ImageButton backButton;
    private ImageView checkVerView;
//   private AppUpdate mAppUpdate;
    PackageInfo info;
    private EditText contentText;
    private TextView nofityView, titleNameTextView, aboutVersionTV;
    private Button commitButton;
    private RelativeLayout aboutOfficalwebRL,aboutVersionRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
//        mAppUpdate = AppUpdateService.getAppUpdate(this);
        try {
            info = AboutUsActivity.this.getPackageManager().getPackageInfo(AboutUsActivity.this.getApplicationContext().getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
//       mAppUpdate.checkLatestVersion(Utils.getMetaValue(this, "update_url"), new SimpleJSONParser());

        backButton = (ImageButton) findViewById(R.id.backBtn);
        checkVerView = (ImageView) findViewById(R.id.checkVerIV);
        contentText = (EditText) findViewById(R.id.contentET);
        nofityView = (TextView) findViewById(R.id.notifyTV);
        commitButton = (Button) findViewById(R.id.commitBtn);
        titleNameTextView = (TextView) findViewById(R.id.titleNameTV);
        aboutVersionTV = (TextView) findViewById(R.id.aboutVersionTV);
        aboutOfficalwebRL = (RelativeLayout) findViewById(R.id.aboutOfficalwebRL);
        aboutVersionRL = (RelativeLayout) findViewById(R.id.aboutVersionRL);

        TextView ver = (TextView) findViewById(R.id.versionTV);
        ver.setText(info.versionName);

        backButton.setOnClickListener(clickListener);
        checkVerView.setOnClickListener(clickListener);
        contentText.addTextChangedListener(textWatcher);
        commitButton.setOnClickListener(clickListener);
        aboutOfficalwebRL.setOnClickListener(clickListener);
        aboutVersionRL.setOnClickListener(clickListener);


        aboutVersionTV.setText(Constants.hasNewVersion ? getResources().getString(R.string.about_havenew_version) :
                getResources().getString(R.string.about_newest_version));

        if (Constants.hasNewVersion) {
            BadgeView badgeView = new BadgeView(AboutUsActivity.this);
            badgeView.setTargetView(aboutVersionRL);
            badgeView.setBadgeMargin(0,10,0,0);
            badgeView.showAsDot();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
//        if (mAppUpdate != null) {
//            mAppUpdate.callOnResume();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mAppUpdate != null) {
//            mAppUpdate.callOnPause();
//        }
    }

    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    AboutUsActivity.this.finish();
                    break;
                case R.id.checkVerIV:
                    getVersion();
                    break;
                case R.id.aboutVersionRL:
                    getVersion();
                    break;
                case R.id.commitBtn:
                    if (!contentText.getText().toString().trim().equals("")) {
                        sendFeedback(contentText.getText().toString());
                    } else {
                        Toast.makeText(AboutUsActivity.this, R.string.about_feedback_content_null, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.aboutOfficalwebRL:
                    String url = Utils.getMetaValue(AboutUsActivity.this, "site");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    break;
            }
        }
    };


    /**
     * 文本框输入内容监听器
     */
    private TextWatcher textWatcher = new TextWatcher() {
        private CharSequence temp;
        boolean over = false;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            over = s.length() > 500;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (s.length() <= 500) {
                temp = s.toString();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (over) {
                Toast.makeText(AboutUsActivity.this, R.string.about_feedback_num_larger, Toast.LENGTH_SHORT).show();
                contentText.setText(temp);
            }
            int len = contentText.length();
            nofityView.setText("(" + len + "/500)");
            contentText.setSelection(len);
        }
    };

    /**
     * 计算输入内容的字数，一个汉字=两个英文字母，一个中文标点=两个英文标点
     */
    private long calculateLength(CharSequence c) {
        double len = 0;
        for (int i = 0; i < c.length(); i++) {
            int tmp = (int) c.charAt(i);
            if (tmp > 0 && tmp < 127) {
                len += 0.5;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }


    Version version = null;

    /**
     * 获取终端版本信息
     */
    private void getVersion() {
        User.getAppVersion(this, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog("检查更新", AboutUsActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    JSONObject jsonObject = obj.getBody();
                    int versionCode = JSONHelper.getInt(jsonObject, "versionCode");
                    String versionName = JSONHelper.getString(jsonObject, "versionName");
                    String feature = JSONHelper.getString(jsonObject, "releaseNote");
                    String releaseUrl = JSONHelper.getString(jsonObject, "releaseUrl");
                    version = new Version(versionCode, versionName, feature, releaseUrl);
                    if (version.code <= info.versionCode) {
                        Toast.makeText(AboutUsActivity.this, R.string.about_version_new, Toast.LENGTH_SHORT).show();
                    } else {
                        FoundVersionDialog dialog = new FoundVersionDialog(AboutUsActivity.this, version, new VersionDialogListener() {
                            @Override
                            public void doUpdate(boolean laterOnWifi) {
                                if (!laterOnWifi) {
                                    checkNetworkAndUpdate(version);
                                } else {
                                    new VersionPersistent(AboutUsActivity.this).save(version);
                                }
                            }

                            @Override
                            public void doIgnore() {
                            }
                        });
                        dialog.show();
                    }
                } else {
                    Toast.makeText(AboutUsActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AboutUsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 发送吐槽信息到后台
     */
    private void sendFeedback(String content) {
        try {
            final JSONObject object = new JSONObject();
            object.put("id", "0");
            object.put("content", content);
            User.sendFeedback(this, object, new BctClientCallback() {
                @Override
                public void onStart() {
                    WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, AboutUsActivity.this);
                }

                @Override
                public void onFinish() {
                    WizardAlertDialog.getInstance().closeProgressDialog();
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(AboutUsActivity.this, R.string.about_feedback_succ, Toast.LENGTH_SHORT).show();
                        AboutUsActivity.this.finish();
                    } else {
                        Toast.makeText(AboutUsActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(AboutUsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void checkNetworkAndUpdate(final Version version) {
        if (NetworkUtil.getNetworkType(AboutUsActivity.this) == NetworkUtil.WIFI) {
            Log.d(Constants.TAG, "现在是wifi网络");
            AppUpdataHandle.getInstance(AboutUsActivity.this).downloadAndInstall(version);
        } else if (NetworkUtil.getNetworkType(AboutUsActivity.this) == NetworkUtil.MOBILE) {
            Log.d(Constants.TAG, "现在走的是流量");
            NoWifiDialog dialog = new NoWifiDialog(AboutUsActivity.this, version, new VersionDialogListener() {
                @Override
                public void doUpdate(boolean laterOnWifi) {
                    AppUpdataHandle.getInstance(AboutUsActivity.this).downloadAndInstall(version);
                }
                @Override
                public void doIgnore() {
                }
            });
            dialog.show();

        } else {
            Toast.makeText(AboutUsActivity.this, "请检查网络是否连接", Toast.LENGTH_SHORT).show();
        }
    }
}
