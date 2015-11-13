package com.bct.gpstracker.util;

import android.util.SparseArray;
import android.view.View;

/**
 * ViewHolder 工具类，用法：
 * TextView phoneView = ViewHolder.get(convertView, R.id.phone);
 *
 * Created by HH on 2015/10/18.
 */
public class ViewHolder {
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
