package com.bct.gpstracker.dialog;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bct.gpstracker.R;

public class CommLoadingDialog {

    private static CommLoadingDialog dialog;
    private PopupWindow mWindow;
    private View mView;
    private ImageView imageView;
    private TextView timeCounter;
    private AnimationDrawable ani;
    private int time;
    private static final int TIME_COUNTER = 1;
    private TimeHandler timeHandler=new TimeHandler();
    private int timerStatus=1;

    private CommLoadingDialog() {
    }

    public static synchronized CommLoadingDialog getDialog() {
        if (dialog == null) {
            dialog = new CommLoadingDialog();
        }
        return dialog;
    }

    public void show(Context activityContext, View view, boolean cancelAble, boolean isModal) {
        if (mWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = layoutInflater.inflate(R.layout.custom_progressing, null);
            imageView = (ImageView) mView.findViewById(R.id.loadingComm);
            timeCounter = (TextView) mView.findViewById(R.id.timeCounter);
            imageView.getBackground().setAlpha(0);
            imageView.setBackgroundColor(Color.TRANSPARENT);
            mView.getBackground().setAlpha(0);
            mView.setBackgroundColor(Color.TRANSPARENT);
            ani = (AnimationDrawable) imageView.getDrawable();
            // 创建一个PopuWidow对象
            mWindow = new PopupWindow(mView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (isModal) {
            mView.setFocusable(true);
            mView.setFocusableInTouchMode(true);
            mView.setOnKeyListener(keyListener);
            mWindow.setFocusable(true);
            mWindow.setOutsideTouchable(false);
        } else {
            ColorDrawable drawable = new ColorDrawable();
            drawable.setAlpha(0);
            drawable.setColor(Color.TRANSPARENT);
            mWindow.setBackgroundDrawable(drawable);
            mWindow.setOutsideTouchable(cancelAble);
            mWindow.setFocusable(false);
        }
        time = 0;
        timerStatus=1;
        timeHandler.sendEmptyMessage(TIME_COUNTER);
        ani.start();
        if (view == null) {
            close();
            return;
        }
        mWindow.showAtLocation(view, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void close() {
        if (mWindow != null && mWindow.isShowing()) {
            ani.stop();
            mWindow.dismiss();
            mWindow = null;
            dialog = null;
        }
        time=0;
        timerStatus=0;
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                close();
                return true;
            }
            return false;
        }
    };

    private class TimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_COUNTER:
                    time++;
                    counterTime();
                    if(timerStatus==1) {
                        timeHandler.sendEmptyMessageDelayed(TIME_COUNTER, 1000);
                    }
            }
        }

        private void counterTime() {
            int minute, second;
//            minute = time / 60 % 60;
            second = time % 999;
            StringBuilder sb=new StringBuilder();
//            if(minute<10){
//                sb.append(0);
//            }
//            sb.append(minute).append(':');
            if(second<10){
                sb.append(0);
            }
            sb.append(second);
            timeCounter.setText(sb.toString());
        }
    }
}
