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
import com.bct.gpstracker.pojo.VoiceCollectEntity;
import com.bct.gpstracker.view.CircleImageView;

public class CollectAdapter extends BaseAdapter {
	private Context context;
	 
    private List<VoiceCollectEntity> entityList;
 
    public CollectAdapter(Context context, List<VoiceCollectEntity> list) {
        this.context = context;
        this.entityList = list;
    }
 
    @Override
    public int getCount() {
        return entityList.size();
    }
 
    @Override
    public VoiceCollectEntity getItem(int position) {
        return entityList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	final VoiceCollectEntity item = entityList.get(position);
        ViewHolder holder;
        if (convertView==null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.voice_collect_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.timeView=(TextView) convertView.findViewById(R.id.timeTV);
            holder.photoView = (CircleImageView) convertView.findViewById(R.id.photoIV);
            holder.editButton = (ImageButton) convertView.findViewById(R.id.updateBtn);
            holder.playButton = (ImageButton) convertView.findViewById(R.id.playBtn);
            holder.deleteButton = (ImageButton) convertView.findViewById(R.id.deleteBtn);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(item.getName());
        holder.timeView.setText(item.getCollectTime());
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
        TextView timeView;
        ImageButton playButton;
        ImageButton editButton;
        ImageButton deleteButton;
    }

}
