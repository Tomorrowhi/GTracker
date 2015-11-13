package com.bct.gpstracker.vo;

import android.view.View;

/**
 * Created by HH
 * Date: 2015/9/9
 * Time: 上午 11:11
 */
public class DialogConfig {
    private View view;
    private boolean canCancel;
    private boolean isModal;

    public DialogConfig(View view, boolean canCancel, boolean isModal) {
        this.view = view;
        this.canCancel = canCancel;
        this.isModal = isModal;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public boolean isModal() {
        return isModal;
    }

    public void setIsModal(boolean isModal) {
        this.isModal = isModal;
    }
}
