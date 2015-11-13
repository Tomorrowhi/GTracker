package com.bct.gpstracker.baby.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class BabyAdapter extends BaseAdapter {
	private Context context;
	 
    private List<Device> entityList;
	private int selectedPosition = -1;// 选中的位置 
 
    public BabyAdapter(Context context, List<Device> list) {
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
            convertView=LayoutInflater.from(context).inflate(R.layout.baby_window_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.photoView = (CircleImageView) convertView.findViewById(R.id.photoIV);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(item.getName());
        if(CommUtil.isNotBlank(item.getPortrait())){
            ImageLoader.getInstance().displayImage(item.getPortrait(), holder.photoView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (item.getOnline() != 1) {
                        ((CircleImageView)view).setImageBitmap(Utils.toGrayscale(loadedImage));
                    }
                }
            });
        }else {
			holder.photoView.setImageResource(R.drawable.user_no_photo);
            if (item.getOnline() != 1) {
                Utils.setGrayImageView(holder.photoView);
            }
        }
//        if((selectedPosition!=-1)&&(position==selectedPosition)){
//        	convertView.setBackgroundResource(R.color.red);
//        }else {
//        	convertView.setBackgroundColor(Color.parseColor("#14b9f5")); 
//		}
         
        return convertView;
    }
 
    public int getSelectedPosition() {
		return selectedPosition;
	}

	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
	}

	static class ViewHolder {
//    	ImageView photoView;
    	CircleImageView photoView;
        TextView nameView;
    }

}
