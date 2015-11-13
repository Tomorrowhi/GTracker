package com.bct.gpstracker.babygroup;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.vo.BabyFriend;
import com.bct.gpstracker.vo.FirstLevelComment;
import com.bct.gpstracker.vo.SecondLevelComment;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;

public class FriendAdapter extends BaseAdapter {
    private Context mContext;
    private List<BabyFriend> entityList;
    private List<String> mImagePath = new ArrayList<>();
    private ListView mListView;
    private BitmapUtils bitmapUtils;

    public FriendAdapter(Context context, List<BabyFriend> list) {
        this.mContext = context;
        this.entityList = list;
    }

    /**
     * 设置listview对象
     *
     * @param
     */
    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    @Override
    public int getCount() {
        return entityList.size();
    }

    @Override
    public BabyFriend getItem(int position) {
        return entityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final BabyFriend item = entityList.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_item_view, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.nameView = (TextView) convertView.findViewById(R.id.baby_group_userName);
            holder.contentView = (TextView) convertView.findViewById(R.id.baby_group_user_message_content);
            holder.timeView = (TextView) convertView.findViewById(R.id.baby_group_message_time);
            //图片控件
            holder.imagViewMsg = (GridView) convertView.findViewById(R.id.feed_item_gridview_content);
            holder.textViewMsg = (TextView) convertView.findViewById(R.id.feed_item_gridview_content_textview);
            holder.commentCount = (TextView) convertView.findViewById(R.id.baby_group_item_htv_commentcount);
            /*根控件对象*/
            holder.root = (RelativeLayout) convertView.findViewById(R.id.baby_group_item_layout_root);
            holder.bottomcontent = (RelativeLayout) convertView.findViewById(R.id.baby_group_comment);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
          /*设置显示信息*/
        holder.nameView.setText(item.getUserName());
        if ("".equals(item.getPublishContent())) {
            holder.contentView.setVisibility(View.GONE);
        } else {
            holder.contentView.setVisibility(View.VISIBLE);
            holder.contentView.setText(item.getPublishContent());
        }

        mImagePath.clear();
        //判断是否存在图片信息
        if (item.getPublishPath() != null && !("".equals(item.getPublishPath()))) {
            //存在图片路径信息，加载图片

            holder.imagViewMsg.setVisibility(View.VISIBLE);
            String[] picPath = item.getPublishPath().split(",");
            Log.e("TGA", picPath + ":picPath");
            if (picPath.length > 3) {
                holder.imagViewMsg.setNumColumns(3);
                holder.textViewMsg.setVisibility(View.VISIBLE);
            } else {
                holder.imagViewMsg.setNumColumns(GridView.AUTO_FIT);
                holder.textViewMsg.setVisibility(View.GONE);
            }
            for (int i = 0; i < picPath.length; i++) {
                if (i < 3) {
                    //在此只显示前三张图片
                    mImagePath.add(picPath[i]);
                }
            }
            /*创建Xutils对象，同时设置缓存路径*/
            bitmapUtils = new BitmapUtils(mContext, mContext.getCacheDir() + "/pic/");
            holder.imagViewMsg.setOnScrollListener(new PauseOnScrollListener(bitmapUtils, false, true));
            FriendImageViewAdapter friendImageViewAdapter = new FriendImageViewAdapter(mContext, mImagePath, bitmapUtils);
            holder.imagViewMsg.setAdapter(friendImageViewAdapter);
            friendImageViewAdapter.notifyDataSetChanged();
        } else {
            holder.imagViewMsg.setVisibility(View.GONE);
            holder.textViewMsg.setVisibility(View.GONE);
        }

        //转换时间，将毫秒值转换为具体的时间
        String timeStr;
        if ((System.currentTimeMillis() - Long.parseLong(item.getPublishTime())) > 86400000) {
            //大于一天
            timeStr = Constants.COMM_DATE_FMT.format(Long.parseLong(item.getPublishTime()));
        } else {
            //小于一天
            timeStr = Constants.COMM_TIME_FMT.format(Long.parseLong(item.getPublishTime()));
        }
        holder.timeView.setText(timeStr);

        int countComment = 0;
        /*显示评论*/
        if (item.getFirstLevelComment() != null && item.getFirstLevelComment().size() > 0) {
            List<FirstLevelComment> entityFirstList = item.getFirstLevelComment();
            /*计算有多少个二级评论*/
            countComment = entityFirstList.size();
            for (FirstLevelComment anEntityFirstList : entityFirstList) {
                List<SecondLevelComment> entitySecond = anEntityFirstList.getSecondLevelComment();
                countComment += entitySecond.size();
            }

        }
        holder.commentCount.setText(countComment + "");

        return convertView;
    }


    public static class ViewHolder {
        TextView nameView;
        TextView commentCount;
        TextView contentView;
        TextView timeView;
        TextView textViewMsg;
        GridView imagViewMsg;
        /*根控件*/
        RelativeLayout root;
        /*信息条*/
        RelativeLayout bottomcontent;
    }

}
