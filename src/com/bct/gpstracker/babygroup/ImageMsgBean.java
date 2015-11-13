package com.bct.gpstracker.babygroup;

import android.net.Uri;

/**
 * Created by Admin on 2015/8/22 0022.
 */
public class ImageMsgBean {
    public Uri image_uri;

    public ImageMsgBean(Uri uri) {
        super();
        this.image_uri = uri;
    }

    public Uri getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(Uri image_uri) {
        this.image_uri = image_uri;
    }

}
