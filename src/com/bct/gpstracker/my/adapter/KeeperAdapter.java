package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.Keeper;
import com.bct.gpstracker.util.CommUtil;

public class KeeperAdapter extends BaseAdapter {
	private Context context;
	 
    private List<Keeper> entityList;
 
    public KeeperAdapter(Context context, List<Keeper> list) {
        this.context = context;
        this.entityList = list;
    }
 
    @Override
    public int getCount() {
        return entityList.size();
    }
 
    @Override
    public Keeper getItem(int position) {
        return entityList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	final Keeper item = entityList.get(position);
        ViewHolder holder;
        if (convertView==null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.keeper_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.editButton = (ImageButton) convertView.findViewById(R.id.editBtn);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(CommUtil.isNotBlank(item.getAppIdentity())?item.getAppIdentity():item.getName());
//        holder.editButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(context, AddDeviceActivity.class);
//				intent.putExtra("device", item);
//				context.startActivity(intent);
//			}
//		});
        return convertView;
    }
 
    static class ViewHolder {
//    	ImageView photoView;
//    	CircleImageView photoView;
        TextView nameView;
        ImageButton editButton;
    }

}
