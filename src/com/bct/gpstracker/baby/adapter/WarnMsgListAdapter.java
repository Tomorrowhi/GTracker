package com.bct.gpstracker.baby.adapter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.PushMsg;
import com.bct.gpstracker.util.CommUtil;

/**
 * Created by HH
 * Date: 2015/8/21 0021
 * Time: 下午 3:17
 */
public class WarnMsgListAdapter extends BaseAdapter {
    private Context context;
    private List<PushMsg> msgList;
    private boolean isSelecting;
    public Map<Long, Boolean> selection = new ConcurrentHashMap<>();
    // 设置已读颜色,灰色
    public int readColor = Color.rgb(136, 136, 143);

    public WarnMsgListAdapter(Context context, List<PushMsg> msgList) {
        this.context = context;
        this.msgList = msgList;
    }

    public void setIsSelecting(boolean isSelecting) {
        this.isSelecting = isSelecting;
    }

    @Override
    public int getCount() {
        return msgList.size();
    }

    @Override
    public Object getItem(int position) {
        return msgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return msgList.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.warn_msg_item, null);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) view.findViewById(R.id.check_msg);
            holder.msg = (TextView) view.findViewById(R.id.warn_msg);
            holder.wtime = (TextView) view.findViewById(R.id.warn_msg_wtime);
            holder.ctime = (TextView) view.findViewById(R.id.warn_msg_ctime);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 获取消息实体类
        PushMsg pushMsg = msgList.get(position);
        //  设置消息内容
        holder.msg.setText(pushMsg.getMsg());
        // 判断是否已读,0未读,1已读
        if (pushMsg.getMsgState() == 0) {
            holder.msg.setTextColor(Color.BLACK);
        } else {
            holder.msg.setTextColor(readColor);
        }

        Long wtime = pushMsg.getUploadTime();
        String wdt = wtime == null ? "" : CommUtil.getDateTime(new Date(wtime));
        holder.wtime.setText(context.getString(R.string.warn_time) + wdt);

        Long time = pushMsg.getCreateTime();
        String dt = time == null ? "" : CommUtil.getDateTime(new Date(time));
        holder.ctime.setText(context.getString(R.string.receive_time) + dt);

        if (isSelecting) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        return view;
    }

    public static class ViewHolder {
        public CheckBox checkBox;
        public TextView msg;
        public TextView wtime;
        public TextView ctime;
    }
}
