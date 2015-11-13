package com.bct.gpstracker.util;

import android.util.Log;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by HH on 2015/8/20.
 */
public class JsonHttpResponseHelper {
    private BctClientCallback callback;

    public JsonHttpResponseHelper(BctClientCallback callback) {
        this.callback = callback;
    }

    public JsonHttpResponseHandler getHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ResponseData responseData=new ResponseData(response);
                    if (responseData.getRetcode() == 2) {
                        //如果超时，则直接退出到登录界面。
                        CommUtil.showMsgShort(responseData.getMsg());
                        AppContext.getEventBus().post(new Object(), Constants.EVENT_TAG_OFFLINE_NOTIFY);
                        return;
                    }
                    callback.onSuccess(responseData);
                } catch (Exception e) {
                    Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                try {
                    callback.onFailure(responseString);
                } catch (Exception e) {
                    Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
                }
            }

            @Override
            public void onStart() {
                callback.onStart();
            }

            @Override
            public void onFinish() {
                callback.onFinish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    callback.onFailure(Constants.DEFAULT_BLANK);
                } catch (Exception e) {
                    Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                try {
                    callback.onFailure(Constants.DEFAULT_BLANK);
                } catch (Exception e) {
                    Log.e(Constants.TAG, AppContext.getContext().getString(R.string.callback_err), e);
                }
            }
        };
    }
}
