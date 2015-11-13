package com.bct.gpstracker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bct.gpstracker.ui.SendErrorActivity;
import com.bct.gpstracker.ui.LoadingActivity;

/**
 * Created by Admin on 2015/9/23 0023.
 */
public class ExceptionCatchService extends Service {

    private static ExceptionCatchService mInstance = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static ExceptionCatchService getInstance() {
        return mInstance;
    }

    public void sendError(final String message) {
        startActivity(new Intent(this, LoadingActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        Intent intent = new Intent(this, SendErrorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("msg", message);
        startActivity(intent);
        stopSelf();
    }
}
