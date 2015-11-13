package com.bct.gpstracker.inter;

import com.bct.gpstracker.pojo.ResponseData;

/**
 *
 */
public interface BctClientCallback {

    public void onStart();

    public void onFinish();

    /**
     *
     * @param obj
     */
    public void onSuccess(ResponseData obj) throws Exception;

    /**
     *
     * @param message
     */
    public void onFailure(String message) throws Exception;
}
