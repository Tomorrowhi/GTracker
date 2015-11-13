package com.bct.gpstracker.inter;

import com.amap.api.location.AMapLocation;

/**
 * Created by HH
 * Date: 2015/6/25 0025
 * Time: 下午 1:41
 */
public interface NotifyCallback {
    public void execute(AMapLocation location);
}
