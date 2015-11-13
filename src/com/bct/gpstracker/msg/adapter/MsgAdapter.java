package com.bct.gpstracker.msg.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.Friend;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.BadgeView;
import com.bct.gpstracker.view.CircleImageView;
import com.bct.gpstracker.vo.TermType;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MsgAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> list = new ArrayList<>();
    private BadgeView badge;

    public MsgAdapter(Context context, List<Friend> list) {
        this.context = context;
        this.list.addAll(list);
    }

    static class ContViewHolder {
        CircleImageView photoView;
        TextView userName, recentMsg, msgTime, msgRelation;
        LinearLayout linearLayoutTitle;
        BadgeView badgeView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Friend getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContViewHolder holder;
        final Friend item = list.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_item_msg, null);
            holder = new ContViewHolder();
            convertView.setTag(holder);
            holder.linearLayoutTitle = (LinearLayout) convertView.findViewById(R.id.friend_title);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.recentMsg = (TextView) convertView.findViewById(R.id.recent_msg);
            holder.msgRelation = (TextView) convertView.findViewById(R.id.msg_relation);
            holder.msgTime = (TextView) convertView.findViewById(R.id.msg_time);
            holder.photoView = (CircleImageView) convertView.findViewById(R.id.photoIV);
            holder.badgeView = (BadgeView) convertView.findViewById(R.id.badgeview);
        } else {
            holder = (ContViewHolder) convertView.getTag();
        }
        //消息提示
        Integer unreadDataShow = MainActivity.unreadData.get(item.getImei());
        if (unreadDataShow != null && unreadDataShow > 0) {
            holder.badgeView.setVisibility(View.VISIBLE);
            holder.badgeView.setHideOnNull(true);
            holder.badgeView.setText(unreadDataShow.toString());
            holder.badgeView.setBadgeMargin(0, 0, 10, 0);
        } else {
            holder.badgeView.setVisibility(View.GONE);
        }
        //优先显示昵称
        String name = item.getNickName();
        if (name == null) {
            name = item.getName();
        }
        holder.userName.setText(name);
        holder.recentMsg.setText(item.getLastMsg());
        Long lastContactTime = item.getLastConnectTime();
        holder.msgTime.setText(formatRecentTime(lastContactTime));
        if (CommUtil.isNotBlank(item.getPhoto())) {
            ImageLoader.getInstance().displayImage(item.getPhoto(), holder.photoView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (!item.getOnline()) {
                        ((CircleImageView)view).setImageBitmap(Utils.toGrayscale(loadedImage));
                    }
                }
            });
        } else {
            holder.photoView.setImageResource(R.drawable.user_no_photo);
            if (!item.getOnline()) {
                Utils.setGrayImageView(holder.photoView);
            }
        }
        holder.msgRelation.setText(item.getTermType() == TermType.WATCH ? context.getString(R.string.keeper_object_manager) : context.getString(R.string.keeper_manager));
        return convertView;
    }

    public void refreshData(List<Friend> fds) {
        list.clear();
        if (CommUtil.isNotEmpty(fds)) {
            list.addAll(fds);
        }
        notifyDataSetChanged();
    }

    private String formatRecentTime(Long time) {
        if (time == null) {
            return Constants.DEFAULT_BLANK;
        }
        Log.i(Constants.TAG, new Date(time).toString());
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        String res;
        long left = time - calendar.getTimeInMillis();
        calendar.setTimeInMillis(time);
        if (left > 0 && left < 86400000) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            if (hour < 12) {
                res = String.format(context.getString(R.string.time_am), hour < 10 ? "0" + hour : hour, minute < 10 ? "0" + minute : minute);
            } else {
                if (hour > 12) {
                    hour -= 12;
                }
                res = String.format(context.getString(R.string.time_pm), hour < 10 ? "0" + hour : hour, minute < 10 ? "0" + minute : minute);
            }
        } else if (left < 0 && -left < 86400000) {
            res = context.getString(R.string.time_yesterday);
        } else {
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            res = String.format(context.getString(R.string.time_day_of_year), calendar.get(Calendar.YEAR), month < 10 ? "0" + month : month, day < 10 ? "0" + day : day);
        }
        return res;
    }
}
