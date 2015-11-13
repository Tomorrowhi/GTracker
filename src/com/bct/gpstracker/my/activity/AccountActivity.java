package com.bct.gpstracker.my.activity;

import java.lang.ref.WeakReference;
import java.util.*;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.CommTitleActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.fix.swipemenu.*;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.my.adapter.AllUserAdapter;
import com.bct.gpstracker.pojo.*;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.vo.GroupEntity;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * 账号管理页面
 *
 * @author huangfei
 */
public class AccountActivity extends CommTitleActivity implements Observer {
    private final static String TAG = AccountActivity.class.getSimpleName();

    @ViewInject(R.id.rlv_my_list)
    private SwipeMenuListView allMsgLstView;

    private List<ManaRelation> listAll = new ArrayList<>();
    private List<Keeper> keeperList = new ArrayList<>();
    private List<Keeper> friendList = new ArrayList<>();//历史遗留，管理员里存在的好友
    private List<TermFriend> listFriend = new ArrayList<>();//真正好友

    public static final int REQUEST_CODE_ADD_DEVICE = 0;
    public static final int REQUEST_CODE_ADD_KEEPER = 1;
    public static final int REQUEST_CODE_ADD_FRIEND = 2;

    private AllUserAdapter mAdapter;
    private User currentUser;//当前设备的使用用户
    private Set<Integer> monitorDeviceIds;//监控终端状态的终端列表

    private Device currentDelDevice;//被选中删除的监护对象
    private Keeper currentDelKeeper;//被选中删除的监护人或者好友对象
    private TermFriend currentDelFriend;//当前被选中的好友

    private AccountHandler handler;
    private static int counter;
    private static boolean querying = false;

    public static final String TAG_RECONFIRM = "TAG_RECONFIRM";
    public static final String TAG_DELETE = "TAG_DELETE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAddTop(R.layout.activity_account);
        ViewUtils.inject(this);
        AppContext.getEventBus().register(this);
        Session.getInstance().addObserver(this);

        allMsgLstView = (SwipeMenuListView) findViewById(R.id.rlv_my_list);
        /**
         * 从本地获取到监护人和朋友的列表
         * */
        //Session.getInstance().getUser();此方法返回一个当前使用设备的User对象，该对象中有一个字段appUserNum，当为1时表示是管理员，否则的话不是
        //管理员的功能能够增删改列表，而普通用户则不能,默认爸爸妈妈，以及双亲的直系亲属为监护人，其他关系全为朋友

        currentUser = Session.getInstance().getUser();
        //会出现currentUser为空的情况
        if (!"1".equals(currentUser.getAppUserNum())) {//如果不是主用户  是无法修改相关信息的
            moreFunction.setVisibility(View.GONE);
        }


        initCacheData();
        mAdapter = new AllUserAdapter(this, listAll);
        allMsgLstView.setAdapter(mAdapter);
        allMsgLstView.setMenuCreator(creator);
        allMsgLstView.setOnMenuItemClickListener(listener);
        allMsgLstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hiddenPop();
                allMsgLstView.smoothOpenMenu(position);
            }
        });
        moreFunction.setOnClickListener(this);
        allMsgLstView.setViewProcessor(new SwipeMenuListView.ViewProcessor() {
            @Override
            public void execute(SwipeMenuLayout layout, int position) {
                User user = Session.getInstance().getUser();
                LinearLayout delLayout = (LinearLayout) layout.findViewWithTag(TAG_DELETE);
                if (Constants.MANAGER_USER_NUM.equals(user.getAppUserNum())) {
                    delLayout.setVisibility(View.VISIBLE);
                } else {
                    delLayout.setVisibility(View.GONE);
                }

                LinearLayout reLayout = (LinearLayout) layout.findViewWithTag(TAG_RECONFIRM);
                ManaRelation rel = listAll.get(position);
                if ((rel instanceof Device) && !((Device) rel).isBinded()) {
                    reLayout.setVisibility(View.VISIBLE);
                } else {
                    reLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadingData();
        checkIfCompleteManagerInfo();
//        mAdapter = new AllUserAdapter(this, listAll);
//        allMsgLstView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        handler = null;
        AppContext.getEventBus().unregister(this);
        Session.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    private void loadingData() {
//        getMonitorObjects();
//        getUser();
//        getFriends();
//        getDevice();
    }

    private void initCacheData() {
        listAll.clear();
        listAll.addAll(Session.getInstance().getUserList());
        listAll.addAll(Session.getInstance().getFriendList());
        listAll.addAll(Session.getInstance().getMonitors());
        Collections.sort(listAll);
    }

    private void checkIfCompleteManagerInfo() {
        if (!AppContext.managerInfoChecked) {
            List<Keeper> userList = Session.getInstance().getUserList();
            for (Keeper keeper : userList) {
                if (checkNoFullInfo(keeper)) {
                    CommUtil.showMsgShort(getString(R.string.edit_admin_msg));
                    Intent intent = new Intent(AccountActivity.this, AccountManagerActivity.class);
                    intent.putExtra("keep", keeper);
                    startActivity(intent);
                    break;
                }
            }
        }
        AppContext.managerInfoChecked = true;
    }

    private boolean checkNoFullInfo(Keeper keeper) {
        if (!Constants.MANAGER_USER_NUM.equals(keeper.getAppUserNum())) {
            return false;
        }
        return CommUtil.isBlank(keeper.getNickName()) || CommUtil.isBlank(keeper.getAppIdentity());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.im_more_function:
                showPopWindow(v);
                break;
            case R.id.tv_add_child://添加监护对象
                hiddenPop();
                Device device = null;
                intent = new Intent(this, AddDeviceActivity.class);
                intent.putExtra("device", device);
                //startActivityForResult(intent,REQUEST_CODE_ADD_DEVICE);
                startActivity(intent);
                break;
            case R.id.tv_add_friend://添加好友
                hiddenPop();
                intent = new Intent(this, AddGoodFriendsActivity.class);
                //startActivityForResult(intent, REQUEST_CODE_ADD_FRIEND);
                startActivity(intent);
                break;
            case R.id.tv_add_parent://添加监护人
                hiddenPop();
                int size = 0;
                for (Keeper keeper : keeperList) {
                    if (!"1".equals(keeper.getAppUserNum())) {
                        size++;
                    }
                }
                if (size >= 4) {
                    Toast.makeText(this, getString(R.string.add_user_num_larger), Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(this, AddUserActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_KEEPER);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode != RESULT_OK) {
//            return;
//        }
//        switch (resultCode) {
//            case REQUEST_CODE_ADD_DEVICE:
//                //Device device = (Device) data.getSerializableExtra("device");
//                //addNewMenber(device);
//                break;
//            case REQUEST_CODE_ADD_FRIEND:
//                //TermFriend friend = (TermFriend) data.getSerializableExtra("friend");
//                //addNewMenber(friend);
//                break;
//            case REQUEST_CODE_ADD_KEEPER:
//
//                //Keeper keeper = (Keeper) data.getSerializableExtra("keeper");
//                //addNewMenber(keeper);
//                break;
//        }
    }


    /**
     * 目前接口可通
     */
    private void getFriends() {
        TermFriend me = new TermFriend();
        User user = Session.getInstance().getUser();
        me.setId(CommUtil.toInteger(user.getId()));
        me.setCellPhone(user.getPhone());
        me.setName("");
        me.setRelationship("");
        me.friendsQuery(this, new BctClientCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    if (listFriend == null) {
                        listFriend = new ArrayList<>();
                    } else {
                        listFriend.clear();
                    }
                    TermFriend friend = null;
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        friend = new TermFriend(JSONHelper.getJSONObject(obj.getBodyArray(), i));
                        friend.setAuthDiscrible(getString(R.string.rel_good_friend));
                        listFriend.add(friend);
                    }
                    int position = -1;
                    if (listAll.size() == 0) {
                        listAll.addAll(listFriend);
                    } else {
                        synchronized (this) {
                            for (int i = listAll.size() - 1; i >= 0; i--) {
                                if (getString(R.string.rel_friend).equals(listAll.get(i).getAuthDiscrible())) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position == -1) {
                            listAll.addAll(listFriend);
                        } else {
                            listAll.addAll(position + 1, listFriend);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AccountActivity.this, "返回信息：" + obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AccountActivity.this, message + "错误信息", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 获取所有的设备
     */
    private void getDevice() {
//        Session.getInstance().getDevices().clear();
        Device.getList(this, new BctClientCallback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
                //WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        Device device = new Device(JSONHelper.getJSONObject(obj.getBodyArray(), i));
//                        Session.getInstance().getDevices().add(device);
//                        Log.d("AccountActivity.java", "取出的蓝牙mac:" + device.getBlueMac());
                    }
                } else {
                    Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(String message) {
                if (CommUtil.isNotBlank(message)) {
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获取所有的监护对象
     */
    private void getMonitorObjects() {
        Session.getInstance().getMonitors().clear();
        Device.getMonitorList(this, new BctClientCallback() {
            @Override
            public void onStart() {
                //WizardAlertDialog.getInstance().showProgressDialog(R.string.get_device_data, AccountActivity.this);
            }

            @Override
            public void onFinish() {
                //WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                List<Device> listTemp = Session.getInstance().getMonitors();
                if (obj.getRetcode() == 1) {
                    Session.getInstance().getMonitors().clear();
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        Device device = new Device(JSONHelper.getJSONObject(obj.getBodyArray(), i));
                        device.setAuthDiscrible(getString(R.string.rel_guardian_obj));
                        listTemp.add(device);
                        Log.d("AccountActivity.java", "取出的蓝牙mac:" + device.getMac());
                    }
                } else {
                    Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
                listAll.addAll(listTemp);
                mAdapter.notifyDataSetChanged();
                listTemp = null;

            }

            @Override
            public void onFailure(String message) {
                if (CommUtil.isNotBlank(message)) {
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获取所有监护人
     */
    private void getUser() {
        //getMonitor----------
        Session.getInstance().getMonitors().clear();
        Keeper.getList(this, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.update_data, AccountActivity.this);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                WizardAlertDialog.getInstance().closeProgressDialog();
                listAll.clear();
//                Session.getInstance().getDevices().clear();


                if (obj.getRetcode() == 1) {//如果成功返回数据，那么就重新设置缓存
                    //getMonitor----------
                    Session.getInstance().getMonitors().clear();
                    //getUser-------------------
                    Session.getInstance().getUserList().clear();
                    //getFriends-----------
                    friendList.clear();
                    keeperList.clear();
                    listFriend.clear();
                    TermFriend friend;
                    Device deviceMonitor;
                    Keeper keeper;
                    Device device;
                    //getDevice---------------
                    try {
                        //getMonitor----------
                        JSONObject jobj = obj.getBody();
                        if (jobj == null) {
                            return;
                        }
                        JSONArray arrMonitor = obj.getBody().getJSONArray("monitorObjects");
                        if (arrMonitor != null && arrMonitor.length() > 0) {
                            for (int i = 0; i < obj.getBody().getJSONArray("monitorObjects").length(); i++) {
                                Log.i(TAG, "monitorObjects:" + obj.getBody().getJSONArray("monitorObjects").length());
                                deviceMonitor = new Device(JSONHelper.getJSONObject(obj.getBody().getJSONArray("monitorObjects"), i));
                                deviceMonitor.setAuthDiscrible(getString(R.string.rel_guardian_obj));
                                deviceMonitor.setSort(Constants.SORT_MONITOR_OBJECT);
                                Session.getInstance().getMonitors().add(deviceMonitor);
                                Log.d("AccountActivity.java", "取出的蓝牙mac:" + deviceMonitor.getMac());
                            }
                        }
                        //getUser-------------------
                        JSONArray arrKeeper = obj.getBody().getJSONArray("keeper");
                        if (arrKeeper != null && arrKeeper.length() > 0) {
                            for (int i = 0; i < obj.getBody().getJSONArray("keeper").length(); i++) {
                                Log.i(TAG, "keeper:" + obj.getBody().getJSONArray("keeper").length());
                                keeper = new Keeper(JSONHelper.getJSONObject(obj.getBody().getJSONArray("keeper"), i));
                                if (isKeeper(keeper)) {
                                    if ("1".equals(keeper.getAppUserNum())) {
                                        keeper.setSort(Constants.SORT_MANAGER);
                                        keeper.setAuthDiscrible(getString(R.string.rel_manager));
                                    } else {
                                        keeper.setSort(Constants.SORT_KEEPER);
                                        keeper.setAuthDiscrible(getString(R.string.rel_guardian));
                                    }
                                    keeperList.add(keeper);//这是监护人列表

                                    //根据获取到的最新的用户信息，更新本地存储的头像
                                    User user = Session.getInstance().getUser();
                                    if (user.getId() == keeper.getId()) {
                                        //当前用户，更新头像
                                        user.setPortrait(keeper.getPortrait());
                                        Session.getInstance().setUser(user);
                                    }

                                } else {
                                    keeper.setAuthDiscrible(getString(R.string.rel_friend));
                                    keeper.setSort(Constants.SORT_FRIEND);
                                    friendList.add(keeper);//这是朋友列表
                                }
                                Session.getInstance().getUserList().add(keeper);//更新缓存
                            }
                        }


                        //getFriends-----------
                        JSONArray arrFriends = obj.getBody().getJSONArray("friends");
                        if (arrFriends != null && arrFriends.length() > 0) {
                            for (int i = 0; i < obj.getBody().getJSONArray("friends").length(); i++) {
                                Log.i(TAG, "friends:" + obj.getBody().getJSONArray("friends").length());
                                friend = new TermFriend(JSONHelper.getJSONObject(obj.getBody().getJSONArray("friends"), i));
                                friend.setAuthDiscrible(getString(R.string.rel_friend));
                                friend.setSort(Constants.SORT_FRIEND);
                                listFriend.add(friend);
                            }
                        }
                        //getDevice---------------
//                        JSONArray arrDevice = obj.getBody().getJSONArray("devices");
//                        if (arrDevice != null && arrDevice.length() > 0) {
//                            for (int i = 0; i < obj.getBody().getJSONArray("devices").length(); i++) {
//                                Log.i(TAG, "devices:" + obj.getBody().getJSONArray("devices").length());
//                                device = new Device(JSONHelper.getJSONObject(obj.getBody().getJSONArray("devices"), i));
//                                Session.getInstance().getDevices().add(device);
//                            }
//                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
                listAll.addAll(keeperList);
                listAll.addAll(friendList);
                listAll.addAll(listFriend);
                listAll.addAll(Session.getInstance().getMonitors());
                Collections.sort(listAll);
                mAdapter.notifyDataSetChanged();
                TermFriend friend = null;
                Device deviceMonitor = null;
                Keeper keeper = null;
                Device device = null;

                ContactsUtil.getInstance(AccountActivity.this).insertOrUpdataAllContacts();

            }

            @Override
            public void onFailure(String message) {
                WizardAlertDialog.getInstance().closeProgressDialog();
                if (CommUtil.isNotBlank(message)) {
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isKeeper(Keeper keeper) {
        String[] keeperGroup = new String[]{getString(R.string.rel_father), getString(R.string.rel_mother),
                getString(R.string.rel_grand_father), getString(R.string.rel_grand_mother)};
        for (String rel : keeperGroup) {
            if (rel.equals(keeper.getAppIdentity())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除目标
     */
    private void deleteOthers(int position) {
        ManaRelation rel = listAll.get(position);
        if (AccountActivity.this.getString(R.string.rel_guardian_obj).equals(rel.getAuthDiscrible())) {//表示删除的是监护对象
            currentDelDevice = (Device) rel;
            currentDelDevice.deleteMonitorObject(AccountActivity.this, new BctClientCallback() {
                @Override
                public void onStart() {
                    CommUtil.showProcessing(allMsgLstView, true, true);
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(AccountActivity.this, R.string.delete_device_success, Toast.LENGTH_SHORT).show();
                        listAll.remove(currentDelDevice);
                        List<Device> devices = Session.getInstance().getMonitors();
                        for (int i = 0; i < devices.size(); i++) {
                            if (currentDelDevice.getImei().equals(devices.get(i).getImei())) {
                                devices.remove(i);
                            }
                        }
                        Session.getInstance().setMonitors(devices);
                        mAdapter.notifyDataSetChanged();
                        AppContext.getEventBus().post(currentDelDevice, Constants.EVENT_TAG_DELETE_TEL_ACCOUNT);
                    } else {
                        Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (getString(R.string.rel_friend).equals(rel.getAuthDiscrible()) & rel instanceof TermFriend) {
            TermFriend termFriend = (TermFriend) listAll.get(position);
            deleteFriend(termFriend);
        } else {
            //删除监护人
            currentDelKeeper = (Keeper) listAll.get(position);
            deleteKeeper(currentDelKeeper);
            List<Keeper> userList = Session.getInstance().getUserList();
            for (int i = 0; i < userList.size(); i++) {
                if (currentDelKeeper.getImei().equals(userList.get(i).getImei())) {
                    userList.remove(i);
                }
            }
            Session.getInstance().setUserList(userList);
        }
    }

    /**
     * 删除好友,传入需要删除的id数组，管理员具有批量删除的功能，而其他用户只能删除自己
     */
    private void deleteFriend(final TermFriend termFriend) {
        JSONObject json = new JSONObject();
        try {
            json.put("ids", new JSONArray(Collections.singleton(termFriend.getId())));
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Delete TermFriend failded.", e);
            Toast.makeText(AccountActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e(TAG, json.toString());
        termFriend.friendsDelete(this, json, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(getString(R.string.delete_msg_loading), AccountActivity.this);
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                WizardAlertDialog.getInstance().closeProgressDialog();
                if (obj.getRetcode() == 1) {
                    Toast.makeText(AccountActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                    listAll.remove(termFriend);
                    List<TermFriend> friends = Session.getInstance().getFriendList();
                    for (TermFriend fd : friends) {
                        if (fd.getId() == termFriend.getId()) {
                            friends.remove(fd);
                            break;
                        }
                    }
                    Session.getInstance().setFriendList(friends);
                    mAdapter.notifyDataSetChanged();
                    AppContext.getEventBus().post(termFriend, Constants.EVENT_TAG_DELETE_TEL_ACCOUNT);
                } else {
                    Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 删除监护人
     */
    private void deleteKeeper(final Keeper mKeeper) {
        if (mKeeper != null) {
            mKeeper.delete(this, new BctClientCallback() {
                @Override
                public void onStart() {
                    WizardAlertDialog.getInstance().showProgressDialog("正在删除", AccountActivity.this);
                }

                @Override
                public void onFinish() {
                    WizardAlertDialog.getInstance().closeProgressDialog();
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(AccountActivity.this, R.string.delete_user_success, Toast.LENGTH_SHORT).show();
                        listAll.remove(mKeeper);
                        List<Keeper> keepers = Session.getInstance().getUserList();
                        for (Keeper kp : keepers) {
                            if (kp.getId() == mKeeper.getId()) {
                                keepers.remove(kp);
                                break;
                            }
                        }
                        Session.getInstance().setUserList(keepers);
                        mAdapter.notifyDataSetChanged();
                        AppContext.getEventBus().post(mKeeper, Constants.EVENT_TAG_DELETE_TEL_ACCOUNT);
                    } else {
                        Toast.makeText(AccountActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hiddenPop();
        return super.onTouchEvent(event);
    }

    protected PopupWindow popFunction;

    /**
     * 更多功能选择框
     */
    protected void showPopWindow(View view) {
        if (popFunction == null) {
            LinearLayout popView = (LinearLayout) View.inflate(this, R.layout.layout_pop_more_function, null);
            popFunction = new PopupWindow(popView, Utils.dp2px(this, 150f), RelativeLayout.LayoutParams.WRAP_CONTENT, false);
            popView.findViewById(R.id.tv_add_friend).setOnClickListener(this);
            popView.findViewById(R.id.tv_add_parent).setOnClickListener(this);
            popView.findViewById(R.id.tv_add_child).setOnClickListener(this);
        }
        if (popFunction.isShowing()) {
            popFunction.dismiss();
            return;
        }
        popFunction.setOutsideTouchable(true);
        int[] vLocation = new int[2];
        view.getLocationOnScreen(vLocation);
        popFunction.showAsDropDown(view, vLocation[0], (rootView.getChildAt(0).getHeight() - view.getHeight()) / 2);
    }

    protected void hiddenPop() {
        if (popFunction == null) {
            return;
        }
        if (popFunction.isShowing()) {
            popFunction.dismiss();
        }
    }

    private SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem edit = new SwipeMenuItem(AccountActivity.this);
            edit.setBackground(new ColorDrawable(Color.parseColor("#FF924E")));
            edit.setWidth(Utils.dp2px(AccountActivity.this, 75f));
            edit.setTitle(R.string.update);
            edit.setTitleSize(18);
            edit.setTitleColor(Color.WHITE);
            menu.addMenuItem(edit);

            SwipeMenuItem delete = new SwipeMenuItem(AccountActivity.this);
            delete.setBackground(new ColorDrawable(Color.parseColor("#FF0033")));
            delete.setWidth(Utils.dp2px(AccountActivity.this, 75f));
            delete.setTitle(R.string.alarm_delete);
            delete.setTitleSize(18);
            delete.setTitleColor(Color.WHITE);
            delete.setTag(TAG_DELETE);
            menu.addMenuItem(delete);

            SwipeMenuItem confirm = new SwipeMenuItem(AccountActivity.this);
            confirm.setBackground(new ColorDrawable(Color.parseColor("#CC9400")));
            confirm.setWidth(Utils.dp2px(AccountActivity.this, 75f));
            confirm.setTitle(R.string.rel_reconfirm);
            confirm.setTitleSize(18);
            confirm.setTitleColor(Color.WHITE);
            confirm.setTag(TAG_RECONFIRM);
            menu.addMenuItem(confirm);
        }
    };
    private SwipeMenuListView.OnMenuItemClickListener listener = new SwipeMenuListView.OnMenuItemClickListener() {
        /**
         * index是在creator里面添加的子控件所在的位置
         * position应该是被选中item的索引
         * */
        @Override
        public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
            User user = Session.getInstance().getUser();
            switch (index) {
                case 0:
                    modifyUser(position, user);
                    break;
                case 1:
                    deleteUser(position, user);
                    break;
                case 2:
                    resendConfirm(position, user);
                    monitorDeviceStatus();
                    break;
            }
            return false;//返回false时会关闭侧拉菜单，为true的时候侧拉栏不会关闭
        }
    };

    private void resendConfirm(int position, User user) {
        String imei = ((Device) listAll.get(position)).getImei();
        CommService.get().sendCommand(AccountActivity.this, imei, "bd", user.getPhone(), null);
        CommUtil.showMsgShort(getString(R.string.re_confirm_success));
    }

    private boolean deleteUser(final int position, User user) {
        //完成删除用户的工作
        if (!"1".equals(user.getAppUserNum())) {
            //只有管理员才能删除其他账号
            Toast.makeText(AccountActivity.this, getString(R.string.rel_mana_can_delete_others), Toast.LENGTH_SHORT).show();
            return true;
        }
        //表示是管理员删除
        new AlertDialog.Builder(AccountActivity.this)
                .setTitle(R.string.notice)
                .setMessage(String.format(getString(R.string.confirm_msg), getString(R.string.delete)))
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteOthers(position);

                    }
                })
                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .show();
        return false;
    }

    private void modifyUser(int position, User user) {
        Intent intent = new Intent();
        ManaRelation rel = listAll.get(position);
        if ("1".equals(user.getAppUserNum())) {
            //如果当前登录用户为管理员，可修改所有用户信息
            if (rel instanceof Keeper) {
                currentDelKeeper = (Keeper) rel;
                intent.putExtra("keep", currentDelKeeper);
                if (getString(R.string.rel_guardian).equals(rel.getAuthDiscrible())) {//表示是监护人
                    intent.setClass(AccountActivity.this, AddUserActivity.class);
                } else {
                    intent.setClass(AccountActivity.this, AccountManagerActivity.class);
                }
                AccountActivity.this.startActivity(intent);
            } else if (rel instanceof Device) {//表示是监护对象
                currentDelDevice = (Device) rel;
                intent.putExtra("device", currentDelDevice);
                intent.setClass(AccountActivity.this, AddDeviceActivity.class);
                AccountActivity.this.startActivity(intent);
            } else if (rel instanceof TermFriend) {
                currentDelFriend = (TermFriend) rel;
                intent.putExtra("friend", currentDelFriend);
                intent.setClass(AccountActivity.this, AddGoodFriendsActivity.class);
                AccountActivity.this.startActivity(intent);
            } else {
                Toast.makeText(AccountActivity.this, getString(R.string.rel_mana_can_edit_err), Toast.LENGTH_SHORT).show();
            }

        } else {
            //非管理员账户，只能修改自己
            if (rel instanceof Keeper) {
                currentDelKeeper = (Keeper) rel;
                intent.putExtra("keep", currentDelKeeper);
                if (user.getId() == currentDelKeeper.getId()) {
                    //修改自己
                    intent.setClass(AccountActivity.this, AddUserActivity.class);
                    AccountActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(AccountActivity.this, getString(R.string.rel_mana_can_no_authority), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AccountActivity.this, getString(R.string.rel_mana_can_edit_self), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_UPDATE_ACCOUNT)
    private void updateAccountsInfo(ManaRelation user) {
        if (user == null) {
            return;
        }
        sendHeartBeat();
        boolean isAdd = true;
        if (user instanceof Keeper) {
            Keeper keeper = (Keeper) user;
            if (keeper.getId() == 0) {
                return;
            }
            int i = 0;
            if ("1".equals(keeper.getAppUserNum())) {
                keeper.setSort(Constants.SORT_MANAGER);
                keeper.setAuthDiscrible(getString(R.string.rel_manager));
            } else {
                keeper.setSort(Constants.SORT_KEEPER);
                keeper.setAuthDiscrible(getString(R.string.rel_guardian));
            }
            List<Keeper> keepers = Session.getInstance().getUserList();
            for (Keeper kp : keepers) {
                if (kp.getId() == keeper.getId()) {
                    keepers.set(i, keeper);
                    isAdd = false;
                    break;
                }
                i++;
            }
            if (isAdd) {
                keepers.add(keeper);
            }
            Session.getInstance().setUserList(keepers);
        } else if (user instanceof Device) {
            Device device = (Device) user;
            if (device.getId() == 0) {
                return;
            }
            int i = 0;
            device.setAuthDiscrible(getString(R.string.rel_guardian_obj));
            device.setSort(Constants.SORT_MONITOR_OBJECT);
            List<Device> devices = Session.getInstance().getMonitors();
            for (Device dv : devices) {
                if (dv.getId() == device.getId()) {
                    devices.set(i, device);
                    isAdd = false;
                    break;
                }
                i++;
            }
            if (isAdd) {
                devices.add(device);
            }
            Session.getInstance().setMonitors(devices);
            monitorDeviceStatus();
//            Session.getInstance().setDevices(devices);
        } else if (user instanceof TermFriend) {
            TermFriend friend = (TermFriend) user;
            if (friend.getId() == 0) {
                return;
            }
            int i = 0;
            friend.setAuthDiscrible(getString(R.string.rel_friend));
            friend.setSort(Constants.SORT_FRIEND);
            List<TermFriend> friends = Session.getInstance().getFriendList();
            for (TermFriend fd : friends) {
                if (fd.getId() == friend.getId()) {
                    friends.set(i, friend);
                    isAdd = false;
                    break;
                }
                i++;
            }
            if (isAdd) {
                friends.add(friend);
            }
            Session.getInstance().setFriendList(friends);
        }
//        initCacheData();
//        mAdapter.notifyDataSetChanged();
        Session.getInstance().setChanged();
        Session.getInstance().notifyObservers();
    }

    private synchronized void monitorDeviceStatus() {
        for (Device device : Session.getInstance().getMonitors()) {
            if (!device.isBinded()) {
                if (monitorDeviceIds == null) {
                    monitorDeviceIds = new HashSet<>();
                }
                monitorDeviceIds.add(device.getId());
            }
        }
        if (!querying && CommUtil.isNotEmpty(monitorDeviceIds)) {
            querying = true;
            if (handler == null) {
                handler = new AccountHandler(new WeakReference<>(this));
            }
            handler.sendEmptyMessage(0);
        }
    }

    private void queryDeviceStatusImpl() {
        try {
            if (CommUtil.isEmpty(monitorDeviceIds)) {
                querying = false;
                handler.removeMessages(0);
                return;
            }
            JSONObject json = new JSONObject();
            json.putOpt("ids", new JSONArray(monitorDeviceIds));
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onSuccess(ResponseData obj) throws Exception {
                    if (obj.getRetcode() == 1) {
                        JSONArray jsonArray = obj.getBodyArray();
                        if (jsonArray == null || jsonArray.length() == 0) {
                            return;
                        }
                        boolean updated = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.optJSONObject(i);
                            if (json == null) {
                                continue;
                            }
                            if (json.optInt("appBind") == 1) {
                                int id = json.optInt("termId");
                                List<Device> devices = Session.getInstance().getMonitors();
                                for (Device device : devices) {
                                    if (id == device.getId()) {
                                        device.setBinded(true);
                                        updated = true;
                                        Iterator<Integer> iterator = monitorDeviceIds.iterator();
                                        while (iterator.hasNext()) {
                                            if (id == iterator.next()) {
                                                iterator.remove();
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if (updated) {
                            Session.getInstance().setChanged();
                            Session.getInstance().notifyObservers();
                        }
                    }
                }

                @Override
                public void onFailure(String message) throws Exception {

                }
            };
            BctClient.getInstance().POST(this, CommonRestPath.queryDeviceStatus(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_UPDATE_TEL_ACCOUNT, mode = ThreadMode.ASYNC)
    private void updateTelAccount(ManaRelation user) {
        if (user == null) {
            return;
        }
        GroupEntity group = checkContactGroup();
        if (user instanceof Device) {
            Device device = (Device) user;
            deleteContact(group, device.getOldPhone());
            addContact(group, device.getName(), device.getPhone());
        } else if (user instanceof Keeper) {
            Keeper keeper = (Keeper) user;
            deleteContact(group, keeper.getOldCellPhone());
            addContact(group, keeper.getNickName(), keeper.getCellPhone());
        } else if (user instanceof TermFriend) {
            TermFriend fd = (TermFriend) user;
            deleteContact(group, fd.getOldCellPhone());
            addContact(group, fd.getName(), fd.getCellPhone());
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_DELETE_TEL_ACCOUNT, mode = ThreadMode.ASYNC)
    private void deleteContact(ManaRelation user) {
        if (user == null) {
            return;
        }
        GroupEntity group = checkContactGroup();
        if (user instanceof Device) {
            Device device = (Device) user;
            deleteContact(group, device.getPhone());
        } else if (user instanceof Keeper) {
            Keeper keeper = (Keeper) user;
            deleteContact(group, keeper.getCellPhone());
        } else if (user instanceof TermFriend) {
            TermFriend fd = (TermFriend) user;
            deleteContact(group, fd.getCellPhone());
        }
    }

    /**
     * 检查和添加联系人群组
     *
     * @return 已创建或已存在的群组
     */
    private GroupEntity checkContactGroup() {
        String groupName = getString(R.string.app_name);
        GroupEntity gp = getContactGroup(groupName);
        if (gp != null) {
            return gp;
        } else {
            ContentValues values = new ContentValues();
            values.put(ContactsContract.Groups.TITLE, groupName);
            getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, values);
            return getContactGroup(groupName);
        }
    }

    /**
     * 获取联系人群组
     *
     * @param groupName
     * @return
     */
    private GroupEntity getContactGroup(String groupName) {
        GroupEntity ge = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                String gn = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE)); // 组名
                if (CommUtil.isNotBlank(gn) && gn.equals(groupName)) {
                    int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups._ID)); // 组id
                    ge = new GroupEntity();
                    ge.setGroupId(groupId);
                    ge.setGroupName(groupName);
                    break;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ge;
    }

    /**
     * 删除联系人
     *
     * @param phone 旧的名称
     */
    private void deleteContact(GroupEntity group, String phone) {
        //根据姓名求id
        Cursor cursor = null;
        Cursor groupTypeCur = null;
        try {
            ContentResolver resolver = getContentResolver();
            //查找在指定群组下所有人员的raw_contact_id
            groupTypeCur = resolver.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.RAW_CONTACT_ID},
                    ContactsContract.Data.DATA1 + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                    new String[]{String.valueOf(group.getGroupId()), ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE}, null);
            if (groupTypeCur.isAfterLast()) {
                return;
            }
            //遍历，找到对应号码所属记录，并删掉
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "=? and " + ContactsContract.Data.RAW_CONTACT_ID + "=?";
            while (groupTypeCur.moveToNext()) {
                int rawContactId = groupTypeCur.getInt(0);
                cursor = resolver.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.RAW_CONTACT_ID}, selection, new String[]{phone, String.valueOf(rawContactId)}, null);
                if (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    //根据id删除data中的相应数据
                    resolver.delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Data._ID + "=?", new String[]{String.valueOf(id)});
                    resolver.delete(ContactsContract.Data.CONTENT_URI, ContactsContract.Data.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(id)});
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(AccountActivity.class.getName(), "出错", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (groupTypeCur != null) {
                groupTypeCur.close();
            }
        }
    }


    /**
     * 添加联系人
     *
     * @param name  联系人姓名
     * @param phone 联系人电话
     */
    private void addContact(GroupEntity group, String name, String phone) {
        /**
         * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
         *
         * 这是后面插入data表的数据，只有执行空值插入，才能使插入的联系人在通讯录里可见
         */

        ContentValues values = new ContentValues();
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        //往data表入姓名数据
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        //往data表入电话数据
        values.clear();
        values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        //往data表入群组数据
        values.clear();
        values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.GroupMembership.DATA1, group.getGroupId());
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
    }

    @Override
    public void update(Observable observable, Object data) {
        initCacheData();
        mAdapter.notifyDataSetChanged();
    }

    private static class AccountHandler extends Handler {
        private WeakReference<AccountActivity> activity;

        public AccountHandler(WeakReference<AccountActivity> activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    if (counter++ < 6) {
                        Log.d(AccountActivity.class.getName(), "开始主动获取第" + counter + "次");
                        activity.get().queryDeviceStatusImpl();
                        sendEmptyMessageDelayed(0, 10000);
                    } else {
                        Log.d(AccountActivity.class.getName(), "主动获取超时,结束！");
                        counter = 0;
                        querying = false;
                    }
                    break;
            }
        }
    }
}
