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
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DeviceAdapter extends BaseAdapter {
	private Context context;
	 
    private List<Device> entityList;
 
    public DeviceAdapter(Context context, List<Device> list) {
        this.context = context;
        this.entityList = list;
    }
 
    @Override
    public int getCount() {
        return entityList.size();
    }
 
    @Override
    public Device getItem(int position) {
        return entityList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	final Device item = entityList.get(position);
        ViewHolder holder;
        if (convertView==null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.device_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.photoView = (CircleImageView) convertView.findViewById(R.id.photoIV);
            holder.editButton = (ImageButton) convertView.findViewById(R.id.editBtn);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(item.getName());
        if(CommUtil.isNotBlank(item.getPortrait())){
            ImageLoader.getInstance().displayImage(item.getPortrait(),holder.photoView);
		}else{
            holder.photoView.setImageResource(R.drawable.user_no_photo);
        }
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
    	CircleImageView photoView;
        TextView nameView;
        ImageButton editButton;
    }

}
