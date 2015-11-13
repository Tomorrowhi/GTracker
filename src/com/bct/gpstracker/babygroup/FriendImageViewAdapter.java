package com.bct.gpstracker.babygroup;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.ui.ImageZoomActivity;
import com.lidroid.xutils.BitmapUtils;

/**
 * Created by Admin on 2015/8/25 0025.
 */
public class FriendImageViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;
    private ImageView image;
    private BitmapUtils mBitmapUtils;

    public FriendImageViewAdapter(Context context, List<String> list,BitmapUtils bitmapUtils) {
        this.context = context;
        this.list = list;

        this.mBitmapUtils=bitmapUtils;
       initData();
    }

    private void initData() {
        mBitmapUtils.configDefaultLoadFailedImage(R.drawable.cancel);
        mBitmapUtils.configDefaultLoadingImage(R.drawable.clear_notify_complete);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String picPath = list.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.publish_gridview_item, null);
            holder.image = (ImageView) convertView.findViewById(R.id.gridview_image_default);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (!TextUtils.isEmpty(picPath)) {
        mBitmapUtils.display(holder.image, Constants.baseUrl + "/" + picPath);
        final File bitmapFileFromDiskCache = mBitmapUtils.getBitmapFileFromDiskCache(Constants.baseUrl + "/" + picPath);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ImageZoomActivity.class);
                if(bitmapFileFromDiskCache!=null) {
                    String path = bitmapFileFromDiskCache.getPath();
                    intent.putExtra(Constants.PIC_PATH, path);
                    context.startActivity(intent);
                }
            }
        });
       }

        return convertView;
    }

    private class ViewHolder {
        ImageView image;
    }
}
