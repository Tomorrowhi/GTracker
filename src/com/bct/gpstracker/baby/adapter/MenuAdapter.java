package com.bct.gpstracker.baby.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bct.gpstracker.R;

public class MenuAdapter extends BaseAdapter {
	private Context context;
	 
    private List<HashMap<String, Object>> entityList;
 
    public MenuAdapter(Context context, List<HashMap<String, Object>> list) {
        this.context = context;
        this.entityList = list;
    }
 
    @Override
    public int getCount() {
        return entityList.size();
    }
 
    @Override
    public HashMap<String, Object> getItem(int position) {
        return entityList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	HashMap<String, Object> item = entityList.get(position);
        ViewHolder holder;
        if (convertView==null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.menu_window_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.photoView = (ImageView) convertView.findViewById(R.id.photoIV);
            holder.notifyView = (ImageView) convertView.findViewById(R.id.notifyIV);
        }
        else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(context.getResources().getString((Integer) item.get("title")));
        holder.photoView.setBackgroundResource((Integer) item.get("id"));
//        if(((Integer)item.get("is_new"))==1){
//        	holder.notifyView.setVisibility(View.VISIBLE);
//        }else {
//        	holder.notifyView.setVisibility(View.GONE);
//		}
         
        return convertView;
    }
 
    static class ViewHolder {
    	ImageView photoView;
        TextView nameView;
        ImageView notifyView;
    }

}
