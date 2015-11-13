package com.bct.gpstracker.msg;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.fix.swipemenu.SwipeMenu;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuCreator;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuItem;
import com.bct.gpstracker.msg.adapter.FriendsAdapter;
import com.bct.gpstracker.msg.view.FriendsSwipeMenuListView;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.Friend;
import com.bct.gpstracker.pojo.MapEntity;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.ClearEditText;
import com.bct.gpstracker.view.SideBar;
import com.bct.gpstracker.vo.Session;
import com.bct.gpstracker.vo.TermType;


/**
 * 消息界面
 */
public class ContactFragment extends Fragment {

    FriendsSwipeMenuListView listView;
    private List<Friend> friends = new ArrayList<>();
    FriendsAdapter friendsAdapter;
    SideBar sideBar;
    TextView dialog;
    ClearEditText clearEditText;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        listView = (FriendsSwipeMenuListView) view.findViewById(R.id.men_listview);
        listView.setDivider(new ColorDrawable(0xE7E7E7));
        listView.setDividerHeight(1);

        friendsAdapter=new FriendsAdapter(getActivity(),friends);
        listView.setAdapter(friendsAdapter);
        listView.setOnChildClickListener(itemClickListener);
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(onMenuItemClickListener);
        listView.setGroupIndicator(null);

        sideBar = (SideBar) view.findViewById(R.id.sidrbar);
        dialog=(TextView) view.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(sideBarTouchingLetterChangedListener);

        clearEditText=(ClearEditText)view.findViewById(R.id.filter_edit);
        clearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doFilterTextChanges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        init();
        return view;
    }

    private void doFilterTextChanges(String s) {
        List<Friend> fds=null;
        if(CommUtil.isBlank(s)){
            fds=friends;
        }else{
            for(Friend f:friends){
                if(containsAllSubSequence(f.getSortLetters().toUpperCase(), s.toUpperCase())
                        ||containsAllSubSequence(f.getName().toUpperCase(), s.toUpperCase())){
                    if(fds==null){
                        fds=new ArrayList<>();
                    }
                    fds.add(f);
                }
            }
        }
        friendsAdapter.refreshData(fds);
        expandListView();
    }

    private void expandListView() {
        for(int i=0;i<friendsAdapter.getGroupCount();i++){
            listView.expandGroup(i);
        }
    }

    private boolean containsAllSubSequence(String src, String sub) {
        int idx=-1;
        for(int i=0;i<sub.length();i++){
            if((idx=src.indexOf(sub.charAt(i),idx+1))==-1){
                return false;
            }
        }
        return true;
    }

    private void init() {
        List<Device> devices=Session.getInstance().getMonitors();
        if(CommUtil.isNotEmpty(devices)){
            for(int i=0;i<devices.size();i++){
                Friend friend=new Friend();
                friend.setId((long)i);
                friend.setName(devices.get(i).getName());
                friend.setImei(devices.get(i).getImei());
                friend.setSortLetters(getString(R.string.watch));
                friend.setTermType(TermType.WATCH);
                friends.add(friend);
            }
        }
//        String[] names=new String[]{"周杰伦","张学友","祁隆","降央卓玛","汪峰","刘德华","乌兰图雅","张惠妹","张杰","张碧晨","那英","郑源","孙露","龙梅子","小蓓蕾组合","林俊杰","莫文蔚","佚名","朴树","EXO","群星","刀郎","陈慧娴","许嵩","霍建华","李克勤","冷漠","许巍","梁静茹","张国荣","品冠","许茹芸","Maroon","5","谭咏麟","梅艳芳","卓依婷","A-Lin","李荣浩","张靓颖","DJ小可","少女时代","By2","齐秦","石进","马頔","杨钰莹","杨坤","伊能静","龚玥","周华健"};
//        int size=friends.size();
//        for(int i=0;i<names.length;i++){
//            Friend friend=new Friend();
//            friend.setId((long)i+size);
//            friend.setName(names[i]);
//            friend.setFriendId((long)i+3);
//            friend.setImei(CommUtil.generateUniqueId(friend.getFriendId(), 15));
//            friend.setTermType(TermType.APP);
//            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//            String py = CommUtil.toPinYin(names[i], format, true, Constants.SPELLING_SEPARATOR);
//            if (CommUtil.isNotBlank(py)) {
//                friend.setSortLetters(py.trim());
//            }
//            friend.setLastPost("全球最美的五十个地方，不去后悔一辈子！");
//            friends.add(friend);
//        }

        friendsAdapter.notifyDataSetChanged();
        expandListView();
    }

    private ExpandableListView.OnChildClickListener itemClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(Constants.REFRESH_FLAG, true);
            Friend friend=(Friend)friendsAdapter.getChild(groupPosition, childPosition);
            MapEntity mEntity = Session.getInstance().getMapEntityByImei(friend.getImei());
            mEntity.setName(friend.getName());
            mEntity.setTermType(friend.getTermType());
            intent.putExtra("chat", mEntity);
            startActivity(intent);
            return true;
        }
    };

    private SideBar.OnTouchingLetterChangedListener sideBarTouchingLetterChangedListener = new SideBar.OnTouchingLetterChangedListener() {
        @Override
        public void onTouchingLetterChanged(String s) {
            // 该字母首次出现的位置
            int position = friendsAdapter.getPositionForSection(s.charAt(0));
            if (position != -1) {
                listView.setSelectedGroup(position);
            }

        }
    };

    private SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem openItem = new SwipeMenuItem(AppContext.getContext());
            openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
            openItem.setWidth(Utils.dp2px(AppContext.getContext(),90f));
            openItem.setTitle(R.string.mark);
            openItem.setTitleSize(18);
            openItem.setTitleColor(Color.WHITE);
            menu.addMenuItem(openItem);
        }
    };

    private FriendsSwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = new FriendsSwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    Toast.makeText(getActivity(),"Test!",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
}
