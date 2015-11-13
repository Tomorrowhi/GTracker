package com.bct.gpstracker.babygroup;

import java.util.List;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.vo.CommentBean;

/**
 * Created by Admin on 2015/8/11 0011.
 */
public class FriendCommentAdapter extends BaseAdapter {
    private Application mApplication;
    private Context mContext;
    private List<CommentBean> mComments;

    public FriendCommentAdapter(Application application,
                                CommentFileActivity commentFileActivity,
                                List<CommentBean> mComments) {
        mContext = commentFileActivity;
        mApplication = application;
        this.mComments = mComments;
    }



    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CommentBean item = mComments.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_comment_item, null);
            holder = new ViewHolder();
            holder.commentUser = (TextView) convertView.findViewById(R.id.friend_comment_item_etv_name);
            holder.commentContent = (TextView) convertView.findViewById(R.id.friend_comment_item_etv_content);
            holder.commentTime = (TextView) convertView.findViewById(R.id.friend_comment_item_htv_time);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.commentUser.setText(item.getUserName());
        String timeStr;
       if ((System.currentTimeMillis()-Long.parseLong(item.getCommTime()))>86400000)
       {
           timeStr= Constants.COMM_DATE_FMT.format(Long.parseLong(item.getCommTime()));
       }else{
           timeStr= Constants.COMM_TIME_FMT.format(Long.parseLong(item.getCommTime()));
       }
        holder.commentTime.setText(timeStr);
        String content = "";
        if (item.getId() == 2) {
            //二级评论
            if ("".equals(item.getReplyMsg())) {
                content = item.getCommentContent();
            } else {
                String replyMsg = item.getReplyMsg();
                //截取指定的字符内容
                String[] str = replyMsg.split("]回复");
                String replyUser = str[1].substring(1, str[1].length() - 1);
                content = "回复" + replyUser + ":" + item.getCommentContent();
            }
        } else {
            //一级评论
            content = item.getCommentContent();
        }


        holder.commentContent.setText(content);
        return convertView;

    }




private static class ViewHolder {
    /*一级评论*/
    TextView commentUser;
    TextView commentContent;
    TextView commentTime;
}


}
