package com.bct.gpstracker.my.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bct.gpstracker.R;

public class TimeAdapter extends BaseAdapter{
	
	private Context mContext;
	private String[] strs;
	private String tag;	//区分日期和星期
	public TimeAdapter(Context context,String[] strs,String tag){
		this.mContext = context;
		this.strs = strs;
		this.tag = tag;
	}
	@Override
	public int getCount() {
		return strs.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String item = strs[position];
        if (convertView==null) {
            convertView=LayoutInflater.from(mContext).inflate(R.layout.choice_time_item, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.item_tv);
        if(tag.equals("date")){
        	textView.setText(item);
        }else if (tag.equals("week")) {
			if(item.equals("0")){
				textView.setBackgroundResource(R.color.white);
			}else if(item.equals("1")){
				textView.setBackgroundResource(R.color.choice_bg);
			}else {
				textView.setText(item);
			}
		}
        return convertView;
	}

}
