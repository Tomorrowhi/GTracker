package com.bct.gpstracker.base;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.util.CommUtil;

/**
 * Created by HH
 * Date: 2015/7/17 0017
 * Time: 上午 9:48
 */
public class CommTitleActivity extends BaseActivity implements View.OnClickListener{
    protected ImageView moreFunction;//更多功能
    protected ImageButton back;//返回键
    protected TextView title;//标题
    protected TextView complet;//完成
    protected LinearLayout rootView;
    private boolean isLoading;//是否正在上传，如果正在上传，锁定此操作
    protected String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (LinearLayout) View.inflate(this,R.layout.layout_top_bar,null);
        setContentView(rootView);
        initView();
        initImm();
        initParams();
    }
    private void initParams(){
        isLoading =false;
    }
    private void initImm(){
        if(imm==null){
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }
    private void initView(){
        title = (TextView) findViewById(R.id.titleNameTV);
        back = (ImageButton) findViewById(R.id.backBtn);
        back.setOnClickListener(this);
        moreFunction = (ImageView) findViewById(R.id.im_more_function);
        moreFunction.setOnClickListener(this);
        complet = (TextView) findViewById(R.id.btn_complet);
        complet.setOnClickListener(this);
    }

    protected void setContentViewAddTop(int layId){
        View view =View.inflate(this,layId,null);
        rootView.addView(view);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.btn_complet:
                if(isLoading){
                    CommUtil.showProcessing(v,true,true);
                    break;
                }else{
                    if(checkIsOk()){
                        setComplet();
                        isLoading =true;
                    }
                }
                break;
        }
    }

    /**
     * 当有提交功能的时候，实现类需要重写该方法
     * */
    protected void setComplet(){

    }
    /**
     * 检查是否可以上传
     * */
    protected boolean checkIsOk(){
        return false;
    }
    protected void resetLoading(boolean flag){
        isLoading =flag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        IBinder binder = getCurrentFocus() == null ? null : getCurrentFocus().getApplicationWindowToken();
        if (binder != null) {
            imm.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.onTouchEvent(event);
    }
    protected static InputMethodManager imm ;
}
