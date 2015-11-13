package com.bct.gpstracker.found;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.vo.FoundBean;

public class FoundAdapter extends BaseAdapter {
    private Context context;

    private List<FoundBean> entityList;

    public FoundAdapter(Context context, List<FoundBean> list) {
        this.context = context;
        this.entityList = list;
    }

    @Override
    public int getCount() {
        return entityList.size();
    }

    @Override
    public FoundBean getItem(int position) {
        return entityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final FoundBean item = entityList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.found_item_view, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.nameView = (TextView) convertView.findViewById(R.id.nameTV);
            holder.photoView = (ImageView) convertView.findViewById(R.id.photoIV);
            holder.distanceView = (TextView) convertView.findViewById(R.id.distanceTV);
            holder.signView = (TextView) convertView.findViewById(R.id.signTV);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.nameView.setText(item.getUserName());
        // holder.distanceView.setText(item.getDistance());
        holder.signView.setText(item.getPublishContent());
        /*获得距离，并转换*/
        //四舍五入，保留三位数
        DecimalFormat decimal = new DecimalFormat("#.#");
        String str = decimal.format(Float.valueOf(item.getDistance()));
        Float aFloat = Float.valueOf(str);
        String distance;
        if (aFloat < 1 && (aFloat * 1000) > 50) {
            //如果距离小于1公里大于50米
            distance=(int)(aFloat * 1000) + "米";
        } else if((aFloat*1000)<50) {
          distance="50米以内";
        }else{
            distance=aFloat + "公里";
        }
        holder.distanceView.setText(distance);
        return convertView;
    }

    static class ViewHolder {
        ImageView photoView;
        TextView nameView;
        TextView distanceView;
        TextView signView;
    }

}
