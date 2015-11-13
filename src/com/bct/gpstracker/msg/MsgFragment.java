package com.bct.gpstracker.msg;

import java.util.*;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import org.simple.eventbus.Subscriber;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.fix.swipemenu.SwipeMenu;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuCreator;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuItem;
import com.bct.gpstracker.fix.swipemenu.SwipeMenuListView;
import com.bct.gpstracker.msg.adapter.MsgAdapter;
import com.bct.gpstracker.pojo.*;
import com.bct.gpstracker.util.ByteUtil;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.ClearEditText;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Msg;
import com.bct.gpstracker.vo.Session;
import com.bct.gpstracker.vo.TermType;
import com.lidroid.xutils.db.sqlite.SqlInfo;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;


/**
 * 消息界面
 */
public class MsgFragment extends Fragment implements View.OnTouchListener,Observer {
    SwipeMenuListView listView;
    private List<Friend> friends = new ArrayList<>();
    MsgAdapter msgAdapter;
    ClearEditText clearEditText;
    private boolean strangerFlag = false;   //联系人数据的标记（用于判断是否是陌生人）
    private boolean deviceFlag = false; //devie数据的标记，用来判读是否有重复数据

    public static MsgFragment newInstance() {
        return new MsgFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg, container, false);

        //注册事件总线
        AppContext.getEventBus().register(this);
        Session.getInstance().addObserver(this);

        listView = (SwipeMenuListView) view.findViewById(R.id.msg_listview);
        listView.setDivider(new ColorDrawable(0xE7E7E7));
        listView.setDividerHeight(1);

        msgAdapter = new MsgAdapter(getActivity(), friends);
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(onMenuItemClickListener);

        clearEditText = (ClearEditText) view.findViewById(R.id.filter_edit);
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.REFRESH_FLAG, true);
                Friend friend = friends.get(position);
                MapEntity mEntity = Session.getInstance().getMapEntityByImei(friend.getImei());
                mEntity.setName(CommUtil.isNotBlank(friend.getNickName()) ? friend.getNickName() : friend.getName());
                mEntity.setTermType(friend.getTermType());
                mEntity.setPhone(friend.getPhone());
                mEntity.setPortrait(friend.getPhoto());
                intent.putExtra("chat", mEntity);
                startActivity(intent);
            }
        });
        listView.setOnTouchListener(this);

        return view;
    }

    /**
     * 必须要等未读条数逻辑完成以后才执行
     * @param chatMsg
     */
    @Subscriber(tag = Constants.EVENT_TAG_UNREAD_DATA_AFTER)
    private synchronized void displayMsg(ChatMsg chatMsg) {
        processStrangers(chatMsg);
        msgAdapter.refreshData(friends);

    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        listView.setAdapter(msgAdapter);
        processStrangers(null);
        msgAdapter.refreshData(friends);
    }

    @Override
    public void onDestroy() {
        AppContext.getEventBus().unregister(this);
        Session.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    private void processStrangers(ChatMsg chatMsg) {
        //判断当前Friends集合中是否存在未读消息的联系人信息（即判断是否为陌生人）
        Set<String> keys = MainActivity.unreadData.keySet();
        if (keys.size() == 0) {
            return;
        }
        int countSize = friends.size();
        for (String key : keys) {
            for (Friend friend : friends) {
                if (friend.getImei().equals(key)) {
                    strangerFlag = false;
                    if (chatMsg == null) {
                        break;
                    }
                    if (friend.getImei().equals(chatMsg.getImei())) {
                        friend.setLastConnectTime(chatMsg.getTime());
                        friend.setLastMsg(Utils.getPrettyDescribe(AppContext.getContext(), ContType.getType(chatMsg.getType()), chatMsg.getContent(), 20));
                    }
                    break;
                } else {
                    strangerFlag = true;
                }

            }
            if (strangerFlag) {
                Stranger stranger = new Stranger();
                stranger.setId((long) (countSize++));
                stranger.setImei(key);
                stranger.setName(getString(R.string.stranger));
                friends.add(stranger);
            }
        }
    }


    private void doFilterTextChanges(String s) {
        List<Friend> fds = null;
        if (CommUtil.isBlank(s)) {
            fds = friends;
        } else {
            for (Friend f : friends) {
                if (containsAllSubSequence(f.getSortLetters().toUpperCase(), s.toUpperCase())
                        || containsAllSubSequence(f.getName().toUpperCase(), s.toUpperCase())) {
                    if (fds == null) {
                        fds = new ArrayList<>();
                    }
                    fds.add(f);
                }
            }
        }
        msgAdapter.refreshData(fds);
    }


    private boolean containsAllSubSequence(String src, String sub) {
        int idx = -1;
        for (int i = 0; i < sub.length(); i++) {
            if ((idx = src.indexOf(sub.charAt(i), idx + 1)) == -1) {
                return false;
            }
        }
        return true;
    }

    private void init() {
        friends.clear();
        List<Device> devices = Session.getInstance().getMonitors();

        if (CommUtil.isNotEmpty(devices)) {
            for (int i = 0; i < devices.size(); i++) {
                if(!devices.get(i).isBinded()){
                    continue;
                }
                Friend friend = new Friend();
                friend.setId((long) i);
                friend.setName(devices.get(i).getName());
                friend.setImei(devices.get(i).getImei());
                friend.setPhoto(devices.get(i).getPortrait());
                friend.setPhone(devices.get(i).getPhone());
                friend.setSortLetters(getString(R.string.watch));
                friend.setTermType(TermType.WATCH);
                friend.setOnline(devices.get(i).getOnline()==1);
                friends.add(friend);
            }
        }

        List<Keeper> userList = Session.getInstance().getUserList();

        if (CommUtil.isNotEmpty(userList)) {
            //更新当前登录用户的信息(目前主要为更新头像信息)
            User user = Session.getInstance().getUser();
            for (Keeper mUser : userList) {
                if (user.getId() == mUser.getId()) {
                    user.setPortrait(mUser.getPortrait());
                    Session.getInstance().setUser(user);
                    break;
                }
            }
            int i = 0;
            if (CommUtil.isNotEmpty(devices)) {
                i = devices.size();
                for (; i < devices.size() + userList.size(); i++) {
                    Keeper keeper = userList.get(i - devices.size());
                    Friend friend = new Friend();
                    friend.setId((long) (i - devices.size()));
                    friend.setName(keeper.getName());
                    friend.setImei(keeper.getImei());
                    friend.setSortLetters("App");
                    friend.setNickName(keeper.getNickName());
                    friend.setTermType(TermType.APP);
                    friend.setPhoto(keeper.getPortrait());
                    friend.setPhone(keeper.getCellPhone());
                    friends.add(friend);
                }
            } else {
                i = 0;
                for (; i < userList.size(); i++) {
                    Keeper keeper = userList.get(i);
                    Friend friend = new Friend();
                    friend.setId((long) i);
                    friend.setName(keeper.getName());
                    friend.setNickName(keeper.getNickName());
                    friend.setImei(keeper.getImei());
                    friend.setPhoto(keeper.getPortrait());
                    friend.setPhone(keeper.getCellPhone());
                    friend.setSortLetters("App");
                    friend.setTermType(TermType.APP);
                    friends.add(friend);
                }
            }
        }
        fillLastMsg(friends);
        msgAdapter = new MsgAdapter(getActivity(), friends);
    }

    private void fillLastMsg(List<Friend> friends) {
        StringBuilder sb = new StringBuilder();
        for (Friend f : friends) {
            sb.append('\'').append(f.getImei()).append('\'').append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        try {
            Map<String, String> msgMap = new HashMap<>();
            Map<String, Long> timeMap = new HashMap<>();
            String sql = "select id,imei,content,type,time from chat_msg where id in (select max(id) id from chat_msg c where user_id=? and imei in (" + sb.toString() + ") group by imei)";
            SqlInfo sqlInfo = new SqlInfo(sql, Session.getInstance().getLoginedUserId());
            List<DbModel> models = AppContext.db.findDbModelAll(sqlInfo);
            for (DbModel model : models) {
                String cont = Utils.getPrettyDescribe(getActivity(), ContType.getType(model.getInt("type")), model.getString("content"), 20);
                msgMap.put(model.getString("imei"), cont);
                timeMap.put(model.getString("imei"),model.getLong("time"));
            }
            for (Friend f : friends) {
                f.setLastMsg(msgMap.get(f.getImei()));
                f.setLastConnectTime(timeMap.get(f.getImei()));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem delItem = new SwipeMenuItem(getActivity());
            delItem.setBackground(R.color.red);
            delItem.setWidth(Utils.dp2px(getActivity(), 80f));
            delItem.setTitle(R.string.delete_msg);
            delItem.setTitleSize(18);
            delItem.setTitleColor(Color.WHITE);
            menu.addMenuItem(delItem);
        }
    };

    private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.notice)
                            .setMessage(String.format(getString(R.string.confirm_msg), getString(R.string.delete_all_msg)))
                            .setPositiveButton(R.string.yes, menuDialogListener.with(position))
                            .setNegativeButton(R.string.no, null)
                            .show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    MenuDialogListener menuDialogListener=new MenuDialogListener();
    private class MenuDialogListener implements DialogInterface.OnClickListener{
        private int position;

        public MenuDialogListener with(int position) {
            this.position = position;
            return this;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            try {
                Friend friend=friends.get(position);
                AppContext.db.delete(ChatMsg.class,WhereBuilder.b("imei", "=", friend.getImei()).and("user_id","=",Session.getInstance().getLoginedUserId()));
                friend.setLastMsg(null);
                MainActivity.unreadData.put(friend.getImei(),null);
                msgAdapter.notifyDataSetChanged();
                MainActivity.getActivity().checkNewMeaasge();
                CommUtil.showMsgShort(getString(R.string.delete_success));
            } catch (DbException e) {
                CommUtil.showMsgShort(getString(R.string.delete_failed));
            }
        }
    }

    private void hideSoftInputWindow() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clearEditText.getWindowToken(), 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftInputWindow();
        return false;
    }

    /**
     * 更新消息界面在线状态
     * @param msg
     */
    @Subscriber(tag = Constants.EVENT_TAG_TERM_STATUS)
    private void updateTermStatus(Msg msg) {
        if (msg == null || msg.getFrom() == null) {
            return;
        }
        String imei = msg.getFrom();
        int status = ByteUtil.byteArrayToInt(msg.getData());
        for (Friend friend : friends) {
            if (imei.equals(friend.getImei())) {
                friend.setOnline(status==1);
                break;
            }
        }
        if (msgAdapter != null) {
            msgAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        try {
            init();
            processStrangers(null);
            msgAdapter.refreshData(friends);
        } catch (Exception e) {
            Log.e(MsgFragment.class.getName(),"更新失败",e);
        }
    }
}
