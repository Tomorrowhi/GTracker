package com.bct.gpstracker.ui;

import java.io.*;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.util.FileUtils;
import com.bct.gpstracker.util.Utils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/9/23 0023.
 */
public class SendErrorActivity extends Activity implements View.OnClickListener {

    @ViewInject(R.id.ok)
    private Button ok;
    @ViewInject(R.id.cancel)
    private Button cancel;
    @ViewInject(R.id.upload_exception_state)
    private TextView uploadExceptionState;
    @ViewInject(R.id.upload_exception_ll)
    private LinearLayout uploadExceptionLl;
    @ViewInject(R.id.upload_exception_dialog)
    private LinearLayout uploadExceptionDialog;


    private String error_msg;
    private Context mContext = SendErrorActivity.this;
    private RequestParams mParams;
    private HttpUtils mHttp;
    private SharedPreferences mSharedPreferences;
    private String IMEI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_info);
        ViewUtils.inject(this);
        mSharedPreferences = Utils.getPreferences(mContext);
        error_msg = getIntent().getStringExtra("msg");
        initData();
        initEvent();

    }

    private void initData() {
        //获取IMEI
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

    }


    private void initEvent() {
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                uploadExceptionLl.setVisibility(View.VISIBLE);
                uploadExceptionDialog.setVisibility(View.GONE);
                Toast.makeText(this, R.string.send_err_msg, Toast.LENGTH_SHORT).show();
                sendExceptionLog();
                break;
            case R.id.cancel:
                rebootLoginActivity();
                finish();
                break;
            default:
                break;
        }

    }

    private void sendExceptionLog() {
        String dataStr = error_msg.substring(0, error_msg.indexOf("properties") - 1);
        File oldFile = new File(mContext.getFilesDir() + "/crashLog/" + error_msg);
        File newFile = new File(mContext.getFilesDir() + "/crashLog/" + IMEI + "_" + error_msg);
        File zipFile = new File(mContext.getFilesDir() + "/crashLog/" + IMEI + "_" + dataStr + ".zip");
        if (!oldFile.exists()) {
            Log.d("TAG", "异常捕获存储目录不存在");
            return;
        }
        //文件名添加IMEI信息,
        boolean renameTo = oldFile.renameTo(newFile);
        BufferedWriter addImei = null;
        try {
            //向文件中插入imei、time字段
            addImei = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(newFile, true)));
            addImei.write("imei=" + IMEI + "\r\n");
            addImei.write("time=" + dataStr + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert addImei != null;
                addImei.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (renameTo) {
            //压缩文件
            try {
                FileUtils.zipFile(newFile, new ZipOutputStream(new FileOutputStream(zipFile)), mContext.getFilesDir() + "/crashLog/");
                //发送数据
                Log.d("TAG", "异常捕获存_发送数据");
                submitExFile(zipFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * 发送日志数据
     */
    public void submitExFile(File file) {
        mParams = new RequestParams();
        mParams.addBodyParameter("file", file);
        mHttp = new HttpUtils(60 * 1000);
        Log.d("TAG", "异常捕获—开始上传");
        mHttp.send(HttpRequest.HttpMethod.POST, Constants.baseUrl + CommonRestPath.sendExFile(), mParams, new RequestCallBack<String>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                if (isUploading) {
                    uploadExceptionState.setText(current + "/" + total);
                } else {
//                          uploadExceptionState.setText(current + "/"+ total);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //{"head":{"retcode":1,"msg":""}}
                Log.e("TGA", "Exception上传成功");
                uploadExceptionState(true);
                rebootLoginActivity();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                //Toast.makeText(mContext, "Exception发送失败，请重试", Toast.LENGTH_SHORT).show();
                Log.e("TGA", "Exception上传失败");
                uploadExceptionState(false);
                rebootLoginActivity();
            }
        });

    }

    /**
     * @param flag 标记是否上传了错误日志
     */
    private void uploadExceptionState(boolean flag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(MyConstants.EXCEPTION_LOG_UPLOAD, flag);
        editor.apply();
    }

    private void rebootLoginActivity() {
        startActivity(new Intent(mContext, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        SendErrorActivity.this.finish();
    }


}
