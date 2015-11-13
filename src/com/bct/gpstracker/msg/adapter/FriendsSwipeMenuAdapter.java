package com.bct.gpstracker.msg.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.bct.gpstracker.fix.swipemenu.SwipeMenu;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuItem;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuLayout;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuView;
import com.bct.gpstracker.msg.view.FriendsSwipeMenuListView;

/**
 *
 */
public class FriendsSwipeMenuAdapter extends BaseExpandableListAdapter implements SwipeMenuView.OnSwipeItemClickListener {

    private FriendsAdapter mAdapter;
    private Context mContext;
    private FriendsSwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener;

    public FriendsSwipeMenuAdapter(Context context, FriendsAdapter adapter) {
        mAdapter = adapter;
        mContext = context;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        SwipeMenuLayout layout;
//        View view;
//        View[] views = mAdapter.getViews(position, convertView, parent);
////        if (convertView == null) {
//        SwipeMenu menu = new SwipeMenu(mContext);
//        menu.setViewType(mAdapter.getItemViewType(position));
//        createMenu(menu);
//        SwipeMenuView menuView = new SwipeMenuView(menu, null);
//        menuView.setOnSwipeItemClickListener(this);
//        FriendsSwipeMenuListView listView = (FriendsSwipeMenuListView) parent;
//
//        layout = new SwipeMenuLayout(views[1], menuView,
//                listView.getCloseInterpolator(),
//                listView.getOpenInterpolator());
//        layout.setPosition(position);
//        layout.setTag(views[1].getTag());
//        view = layout;
//        if (views[0] != null) {
//            LinearLayout lay = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.friend_item_framework, null);
//            lay.addView(views[0]);
//            lay.addView(layout); //FIX 会导致null异常
//            lay.setTag(views[1].getTag());
//            AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            lay.setLayoutParams(params);
//            view = lay;
//        }
//        } else {
//            if (convertView instanceof SwipeMenuLayout) {
//                layout = (SwipeMenuLayout) convertView;
//                layout.closeMenu();
//                layout.setPosition(position);
//                view=layout;
//            } else {
//                view = convertView;
//            }
//        }
//        return view;
//    }

    public void createMenu(SwipeMenu menu) {
        // Test Code
        SwipeMenuItem item = new SwipeMenuItem(mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(Color.GRAY));
        item.setWidth(300);
        menu.addMenuItem(item);

        item = new SwipeMenuItem(mContext);
        item.setTitle("Item 2");
        item.setBackground(new ColorDrawable(Color.RED));
        item.setWidth(300);
        menu.addMenuItem(item);
    }

    @Override
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
        }
    }

    public void setOnMenuItemClickListener(
            FriendsSwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public int getGroupCount() {
        return mAdapter.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAdapter.getChildrenCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAdapter.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mAdapter.getChild(groupPosition,childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mAdapter.getGroupId(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mAdapter.getChildId(groupPosition,childPosition);
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return mAdapter.getGroupView(groupPosition,isExpanded,convertView,parent);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SwipeMenuLayout layout;
        View view = mAdapter.getChildView(groupPosition,childPosition,isLastChild,convertView,parent);
        if (convertView == null) {
            SwipeMenu menu = new SwipeMenu(mContext);
//            menu.setViewType(mAdapter.getItemViewType(position));
            createMenu(menu);
            SwipeMenuView menuView = new SwipeMenuView(menu, null);
            menuView.setOnSwipeItemClickListener(this);
            FriendsSwipeMenuListView listView = (FriendsSwipeMenuListView) parent;

            layout = new SwipeMenuLayout(view, menuView,
                    listView.getCloseInterpolator(),
                    listView.getOpenInterpolator());
            layout.setPosition(groupPosition);
            layout.setTag(view.getTag());
        }else{
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(groupPosition);
        }
        return layout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return mAdapter.isChildSelectable(groupPosition,childPosition);
    }
}
