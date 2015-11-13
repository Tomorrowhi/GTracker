package com.bct.gpstracker.util;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.vo.Session;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by liaoxw on 15/3/12.
 *
 */
public class BctClient extends AsyncHttpClient {

    public static String TAG = Constants.TAG;
    private static BctClient instance = null;
    private String baseUrl;

    public synchronized static BctClient getInstance() {
//        if (instance == null) {
        instance = new BctClient();
        instance.setEnableRedirects(true);
        instance.baseUrl = Constants.baseUrl;
//        }
        synchronized (instance) {
            return instance;
        }
    }

    private BctClient() {
    }

    public void POST(Context context, String url, JSONObject json, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
        HttpEntity entity;
        if (json != null){
            Log.d(TAG, json.toString());
            entity = new StringEntity(json.toString(), "utf-8");
        }else{
            entity = new StringEntity("");
        }

        this.post(context, this.createUrl(url), entity, "application/json", responseHandler);
    }

    public void GET(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (params != null) Log.i(TAG, params.toString());
        this.get(this.createUrl(url), params, responseHandler);
    }

    /**
     *
     * @param context
     * @param url
     * @param json
     * @param responseHandler
     * @throws UnsupportedEncodingException
     * @deprecated Use POST or GET method please!
     */
    public void PUT(Context context, String url, JSONObject json, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
        if (json != null) Log.i(TAG, json.toString());
        HttpEntity entity = new StringEntity(json.toString(), "utf-8");
        this.put(context, this.createUrl(url), entity, "application/json", responseHandler);
    }

    /**
     *
     * @param url
     * @param responseHandler
     * @deprecated Use POST or GET method please!
     */
    public void DELETE(String url, AsyncHttpResponseHandler responseHandler) {
        this.delete(this.createUrl(url), responseHandler);
    }

    public String createUrl(String url) {
        String str = this.baseUrl + url;
        Log.d(TAG, "REQUEST FULL URL:" + str);
        this.signature();
        return str;
    }

    private void signature() {
        String accessKey = Session.getInstance().getAccessKey();
        if (CommUtil.isNotBlank(accessKey)) {
            this.addHeader("accesskey", accessKey);
        }
    }

    private String createPassword(String username, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
            mac.init(secret);
            byte[] hash = mac.doFinal(username.getBytes());
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void setEnableRedirects(final boolean enableRedirects) {
        getHttpClient().getParams().setParameter(ClientPNames.MAX_REDIRECTS, 3);
        getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        ((DefaultHttpClient) getHttpClient()).setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 301 || statusCode == 302) {
                    return enableRedirects;
                }
                return false;
            }
        });
    }
}
