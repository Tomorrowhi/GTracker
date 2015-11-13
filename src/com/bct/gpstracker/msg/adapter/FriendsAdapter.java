package com.bct.gpstracker.msg.adapter;

import java.util.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.pojo.Friend;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendsAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Friend> list;
    private TreeMap<String, List<Friend>> friends;
    private final static String DEFAULT_SORT_LETTER = "#";

    public FriendsAdapter(final Context context, List<Friend> list) {
        this.context = context;
        friends = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                String watchTxt = context.getString(R.string.watch);
                if (watchTxt.equals(lhs) && !watchTxt.equals(rhs)) {
                    return -1;
                } else if (!watchTxt.equals(lhs) && watchTxt.equals(rhs)) {
                    return 1;
                }
                return lhs.compareTo(rhs);
            }
        });
        this.list=list;
        refreshDataSet(list);
    }

    private void refreshDataSet(List<Friend> list) {
        friends.clear();
        if(list==null||list.isEmpty()){
            return;
        }
        String watchTxt=context.getString(R.string.watch);
        for (Friend friend : list) {
            String letter;
            if(CommUtil.isBlank(friend.getSortLetters())){
                letter=DEFAULT_SORT_LETTER;
            }else if(friend.getSortLetters().contains(watchTxt)){
                letter=friend.getSortLetters();
            }else{
                letter=friend.getSortLetters().substring(0, 1).toUpperCase();
            }
            List<Friend> fds = friends.get(letter);
            if (fds == null) {
                fds = new ArrayList<>();
                friends.put(letter, fds);
            }
            fds.add(friend);
        }
        for (Map.Entry<String, List<Friend>> entry : friends.entrySet()) {
            Collections.sort(entry.getValue());
        }
    }

    public TreeMap<String, List<Friend>> getFriends() {
        return friends;
    }

    public int getPositionForSection(char c) {
        int i = 0;
        for (Map.Entry<String, List<Friend>> entry : friends.entrySet()) {
            if (entry.getKey().charAt(0) == c) {
                return i;
            }
            i++;
        }
        return 0;
    }

    static class HeaderViewHolder {
        TextView groupTitle;
    }

    static class ContViewHolder {
        CircleImageView photoView;
        TextView userName, recentMsg;
    }

    @Override
    public int getGroupCount() {
        return friends.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Iterator<Map.Entry<String, List<Friend>>> it = friends.entrySet().iterator();
        for (int i = 0; i < friends.size(); i++) {
            Map.Entry<String, List<Friend>> entry = it.next();
            if (i == groupPosition) {
                return entry.getValue().size();
            }
        }
        return 0;
    }

    /**
     * 返回 Map 键值对
     *
     * @param groupPosition
     * @return Map.Entry<String,List<Friend>>
     */
    @Override
    public Object getGroup(int groupPosition) {
        Iterator<Map.Entry<String, List<Friend>>> it = friends.entrySet().iterator();
        for (int i = 0; i < friends.size(); i++) {
            Map.Entry<String, List<Friend>> entry = it.next();
            if (i == groupPosition) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Iterator<Map.Entry<String, List<Friend>>> it = friends.entrySet().iterator();
        for (int i = 0; i < friends.size(); i++) {
            Map.Entry<String, List<Friend>> entry = it.next();
            if (i == groupPosition) {
                return entry.getValue().get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Iterator<Map.Entry<String, List<Friend>>> it = friends.entrySet().iterator();
        for (int i = 0; i < friends.size(); i++) {
            Map.Entry<String, List<Friend>> entry = it.next();
            if (i == groupPosition) {
                return entry.getValue().get(childPosition).getId();
            }
        }
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_item_header,null);
            holder = new HeaderViewHolder();
            convertView.setTag(holder);
            holder.groupTitle = (TextView) convertView.findViewById(R.id.group_title);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        Map.Entry<String, List<Friend>> entry = (Map.Entry<String, List<Friend>>) getGroup(groupPosition);
        if (entry != null) {
            holder.groupTitle.setText(entry.getKey());
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ContViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_item_cont, null);
            holder = new ContViewHolder();
            convertView.setTag(holder);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.recentMsg = (TextView) convertView.findViewById(R.id.recent_msg);
            holder.photoView = (CircleImageView) convertView.findViewById(R.id.photoIV);
        } else {
            holder = (ContViewHolder) convertView.getTag();
        }
        Friend item = (Friend) getChild(groupPosition, childPosition);
        holder.userName.setText(item.getName());
        holder.recentMsg.setText(item.getLastPost());
        if (CommUtil.isNotBlank(item.getPhoto())) {
            ImageLoader.getInstance().displayImage(item.getPhoto(),holder.photoView);
        } else {
            holder.photoView.setImageResource(R.drawable.user_no_photo);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        refreshDataSet(list);
        super.notifyDataSetChanged();
    }

    public void refreshData(List<Friend> list){
        refreshDataSet(list);
        super.notifyDataSetChanged();
    }
}
