package com.bct.gpstracker.my.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.FenceEntity;

public class FenceAdapter extends BaseAdapter {
	private Context context;

    private List<FenceEntity> entityList;

    public FenceAdapter(Context context, List<FenceEntity> list) {
        this.context = context;
        this.entityList = list;
    }
 
    @Override
    public int getCount() {
        return entityList.size();
    }
 
    @Override
    public FenceEntity getItem(int position) {
        return entityList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	final FenceEntity item = entityList.get(position);
        ViewHolder holder;
        if (convertView==null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.fence_item_view, null);
            holder=new ViewHolder();
            convertView.setTag(holder);
            holder.nameView=(TextView) convertView.findViewById(R.id.nameTV);
            holder.editButton = (ImageView) convertView.findViewById(R.id.editBtn);
//            holder.weekView=(TextView) convertView.findViewById(R.id.weekTV);
//            holder.timeView=(TextView) convertView.findViewById(R.id.timeTV);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(item.getAreaName());
//        holder.timeView.setText(item.getStartTime()+"-"+item.getEndTime());
//        String[] str = item.getWeekDays().split(",");
//        List<String> weeks = new ArrayList<String>();
//        for(String s : str){
//            if(s.equals("1")){
//                weeks.add("周一");
//            }else if(s.equals("2")){
//                weeks.add("周二");
//            }else if(s.equals("3")){
//                weeks.add("周三");
//            }else if(s.equals("4")){
//                weeks.add("周四");
//            }else if(s.equals("5")){
//                weeks.add("周五");
//            }else if(s.equals("6")){
//                weeks.add("周六");
//            }else if(s.equals("7")){
//                weeks.add("周日");
//            }
//        }
//        holder.weekView.setText(Utils.join(",", weeks));
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
        TextView nameView;
        ImageView editButton;
        TextView weekView;
        TextView timeView;
        
    }

}
