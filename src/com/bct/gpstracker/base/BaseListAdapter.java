package com.bct.gpstracker.base;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Administrator on 15-9-8.
 */
public  class BaseListAdapter<T> extends BaseAdapter{
    protected List<T> list;
    protected Context context;

    public BaseListAdapter(){}
    public BaseListAdapter(Context context,List<T> list){
        this.list =list;
        this.context =context;
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
        return null;
    }

}
