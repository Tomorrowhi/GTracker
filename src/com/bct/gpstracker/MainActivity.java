package com.bct.gpstracker;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.bct.gpstracker.baby.BabyFragment;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.babygroup.BabyGroupFragment;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.LocationProvider;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.dialog.VibrateDialog;
import com.bct.gpstracker.found.FoundFragment;
import com.bct.gpstracker.inter.BackHandledInterface;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.inter.NotifyCallback;
import com.bct.gpstracker.msg.MsgMainFragment;
import com.bct.gpstracker.my.MyFragment;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.service.CommunicationService;
import com.bct.gpstracker.service.ExceptionCatchService;
import com.bct.gpstracker.ui.BackHandlerFragment;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.DownLoaderTask;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.BadgeView;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.lurencun.service.autoupdate.Version;

/**
 * APP主页面
 *
 * @author huangfei
 */
public class MainActivity extends BaseActivity implements BackHandledInterface {

    private BabyFragment mBabyFragment;
    private BabyGroupFragment mBabyGroupFragment;
    private FoundFragment mFoundFragment;
    private MyFragment mMyFragment;
    private MsgMainFragment msgMainFragment;
    private Fragment mCurrentFragment;
    FragmentManager fragmentManager;
    private LinearLayout babyGroupLayout, nearbyLayout, myLayout, babyLayout, msgLayout;
    private ImageView babyIV, babyGroupIV, nearbyIV, myIV, msgIV;
    private TextView babyTextView, babyGroupTextView, nearbyTextView, myTextView, msgTextView;
    private BackHandlerFragment mBackHandlerFragment;

    public static List<String> mEmoticons = new ArrayList<String>();
    public static Map<String, Integer> mEmoticonsId = new HashMap<String, Integer>();
    public static List<String> mEmoticons_Zemoji = new ArrayList<String>();
    public static List<String> mEmoticons_Zgif = new ArrayList<String>();
    private BadgeView badge;
    private BadgeView badgeMSG;
    public static Map<String, Integer> unreadData = new HashMap<>();  //未读信息的条数记录
    private int unReadCount = 0;    //未读消息的总数量
    private SharedPreferences mSharedPreferences;
    private static MainActivity activity;
    private long currentBackPressedTime = 0;  //点击返回键的时间
    private static final int BACK_PRESSED_INTERVAL = 2000;  //退出的时间间隔
    // 定位相关
    LocationProvider locationProvider;
    Version version = null;
    PackageInfo info = null;

    public static MainActivity getActivity() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;

        mSharedPreferences = Utils.getPreferences(activity);
        //注册事件总线
        AppContext.getEventBus().register(this);

        //启动异常崩溃服务
        Intent exceptionIntent = new Intent(this, ExceptionCatchService.class);
        startService(exceptionIntent);

        initView();
        fragmentManager = getFragmentManager();

        /**
         * 程序首次运行初始化一个碎片
         */
        mBabyFragment = BabyFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.fragment_place, mBabyFragment).commit();
        this.mCurrentFragment = mBabyFragment;

        Intent intent = new Intent(this, CommunicationService.class);
        intent.setAction(Constants.ACTION_COMM_SERVICE);
        startService(intent);

        loadingEmoji();
        checkNewVersion();
        checkEmoticonVersion();
    }

    /**
     * 加载程序自带表情数据
     */
    private void loadingEmoji() {
        /**
         * 表情暂时使用的是png静态图，如果以后添加其他类型的图片，需要进行判断文件类型，然后将类型参数也对应添加到集合
         */
//        for (int i = 1; i < 59; i++) {
//            String emoticonsName = "[zemoji" + i + ".png]";
//            int emoticonsId = getResources().getIdentifier("zemoji_e" + i,
//                    "drawable", getPackageName());
//            mEmoticons.add(emoticonsName);
//            mEmoticons_Zemoji.add(emoticonsName);
//            mEmoticonsId.put(emoticonsName, emoticonsId);
//        }
        //加载APP自带表情
        for (int i = 1; i < 13; i++) {
            String num = i + "";
            if (i < 10) {
                num = "0" + num;
            }
            String emoticonsName = "[zgif" + num + ".gif]";
            int emoticonsId = getResources().getIdentifier("zgif" + num, "drawable", getPackageName());
            mEmoticons.add(emoticonsName);
            mEmoticons_Zgif.add(emoticonsName);
            mEmoticonsId.put(emoticonsName, emoticonsId);
        }

        String ZgifName = Utils.listToString(mEmoticons_Zgif);
        //保存表情数据到SP中
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MyConstants.EMOJI_APP_OLD, ZgifName);
        editor.apply();
    }

    /**
     * 检查是否有新的图片需要更新,如果不更新，则使用drawable内的表情，如果更新，则优先使用新表情
     */
    private void checkEmoticonVersion() {
        int emotIconLocalVersion = mSharedPreferences.getInt(MyConstants.EMOTI_VERSION, 1);
        try {
            JSONObject data = new JSONObject();
            data.put("version", emotIconLocalVersion);
            //使用异步请求链接对象
            BctClient.getInstance().POST(activity, CommonRestPath.AppEmoticonVersion(), data, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ResponseData obj = new ResponseData(response);
                    if (obj.getRetcode() == 1) {
                        JSONObject jsonObject = obj.getBody();
                        final int emotIconNetVersion = JSONHelper.getInt(jsonObject, "version");
                        boolean downloadState = JSONHelper.getState(jsonObject, "downloadState");
                        if (downloadState) {
                            //需要更新，获得新版本的下载链接
                            final String newEmotIconVersionUrl = obj.getMsg();
                            final Dialog dialog = new Dialog(activity, R.style.dialog);
                            dialog.setContentView(R.layout.dialog_found_emoji);
//                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                            TextView title = (TextView) dialog.findViewById(R.id.title);
                            TextView feature = (TextView) dialog.findViewById(R.id.feature);
                            title.setText(String.format(MainActivity.this.getResources().getString(R.string.latest_version_title), getString(R.string.emoji_package)));
                            feature.setText(R.string.new_emoji_downloader);

                            View ignore = dialog.findViewById(R.id.ignore);
                            View update = dialog.findViewById(R.id.update);
                            ignore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //忽略
                                    dialog.dismiss();
                                }
                            });
                            update.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    File filesDir = new File(activity.getFilesDir() + "/emoji/gif/");
                                    if (!filesDir.exists()) {
                                        //不存在，则创建文件目录
                                        filesDir.mkdirs();
                                    } else {
                                        //删除原files/emoji/gif目录下的文件
                                        File[] listFile = filesDir.listFiles();
                                        if (listFile != null) {
                                            for (File mFile : listFile) {
                                                if (!mFile.isDirectory()) {
                                                    mFile.delete();
                                                }
                                            }
                                        }
                                    }
                                    //下载文件
                                    DownLoaderTask downloaderTask = new DownLoaderTask(newEmotIconVersionUrl, activity.getFilesDir() + "/emoji/", activity.getFilesDir() + "/emoji/gif/", activity, emotIconNetVersion);
                                    downloaderTask.execute();
//                                  downloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.getWindow().setGravity(Gravity.CENTER);
                            dialog.show();

                        }
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(activity, R.string.new_emoji_downloader_err, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 检查是否存在未读信息
     */
    public void checkNewMeaasge() {
        unreadData.clear();
        unReadCount = 0;
        //获取数据库数据
        try {
            //查询未读消息的个数以及对应的IMEI
            List<DbModel> dbModels = AppContext.db.findDbModelAll(Selector.from(ChatMsg.class).where(WhereBuilder.b("is_read", "=", 0)
                    .and("user_id", "=", Session.getInstance().getLoginedUserId()))
                    .groupBy("imei").select("imei", "count(is_read) unRead"));

            Log.d("TAG", "未读数据总数标记：（1或以上代表有未读数据，0代表没有）：" + dbModels);

            for (DbModel dbModel : dbModels) {
                HashMap<String, String> dataMap = dbModel.getDataMap();
                int unRead = Integer.parseInt(dataMap.get("unRead"));
                unReadCount += unRead;
                String imei = dataMap.get("imei");
                unreadData.put(imei, unRead);
            }
            //清除旧提示
            if (unReadCount != 0) {
                badgeMSG.setText(Integer.valueOf(unReadCount).toString());
                badgeMSG.setVisibility(View.VISIBLE);
            } else {
                badgeMSG.setVisibility(View.GONE);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Subscriber(tag = Constants.EVENT_TAG_UNREAD_DATA)
    private synchronized void displayMsg(ChatMsg chatMsg) {
        String imei = chatMsg.getImei();
        Integer unreadCount = unreadData.get(imei);
        if (unreadCount != null) {
            unreadData.put(imei, ++unreadCount);
        } else {
            unreadData.put(imei, 1);
        }
        badgeMSG.setText(Integer.valueOf(++unReadCount).toString());
        badgeMSG.setVisibility(View.VISIBLE);
        AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_UNREAD_DATA_AFTER);
    }

    protected void onResume() {
        super.onResume();
        VibrateDialog.getInstance().bleDisconnected(MainActivity.this);
        if (Session.getInstance().getMainActivity() == null) {
            Session.getInstance().setMainActivity(MainActivity.this);
        }
        int flag = Session.getInstance().getiFlag();
        if (flag == 1) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ChatActivity.class);
            intent.putExtra("chat", Session.getInstance().getMapEntity());
            MainActivity.this.startActivity(intent);
            Session.getInstance().setiFlag(0);
        }

        if (!Constants.hasNewVersion) {
            if (null != badge) {
                badge.setVisibility(View.GONE);
                badge = null;
            }
        }

        //查询是否存在未读数据
        checkNewMeaasge();
        getLocation();
    }

    protected void onDestroy() {
        // 退出时销毁定位
        locationProvider.stop();
        AppContext.getEventBus().unregister(this);
        super.onDestroy();
    }

    private void checkNewVersion() {
        try {
            info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            User.getAppVersion(this, new BctClientCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        JSONObject jsonObject = obj.getBody();
                        int versionCode = JSONHelper.getInt(jsonObject, "versionCode");
                        String versionName = JSONHelper.getString(jsonObject, "versionName");
                        String feature = JSONHelper.getString(jsonObject, "releaseNote");
                        String releaseUrl = JSONHelper.getString(jsonObject, "releaseUrl");
                        version = new Version(versionCode, versionName, feature, releaseUrl);
                        if (version.code > info.versionCode) {
                            Constants.hasNewVersion = true;
                            badge = new BadgeView(MainActivity.this);
                            badge.setTargetView(myIV);
                            badge.showAsDot();
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLocation() {
        locationProvider = LocationProvider.getInstance(this);
        locationProvider.startLocation(new NotifyCallback() {
            @Override
            public void execute(AMapLocation location) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                Session.getInstance().setLatLng(ll);
                Session.getInstance().setGsmCellInfo(Utils.getGSMCellLocationInfo(MainActivity.this));
                Session.getInstance().setWifiInfo(Utils.getWifiInfo(MainActivity.this));
                //上传ＧＰＳ
                //User.postGPS();
            }
        });
    }

    /**
     * 初始化一些控件
     */
    private void initView() {
        babyGroupLayout = (LinearLayout) findViewById(R.id.babyGroupLayout);
        nearbyLayout = (LinearLayout) findViewById(R.id.nearbyLayout);
        myLayout = (LinearLayout) findViewById(R.id.myLayout);
        babyLayout = (LinearLayout) findViewById(R.id.babyLayout);
        msgLayout = (LinearLayout) findViewById(R.id.msgLayout);

        babyIV = (ImageView) findViewById(R.id.babyIV);
        babyGroupIV = (ImageView) findViewById(R.id.babyGroupIV);
        nearbyIV = (ImageView) findViewById(R.id.nearbyIV);
        myIV = (ImageView) findViewById(R.id.myIV);
        msgIV = (ImageView) findViewById(R.id.msgIV);

        babyTextView = (TextView) findViewById(R.id.babyTV);
        babyGroupTextView = (TextView) findViewById(R.id.babyGroupTV);
        nearbyTextView = (TextView) findViewById(R.id.nearbyTV);
        myTextView = (TextView) findViewById(R.id.myTV);
        msgTextView = (TextView) findViewById(R.id.msgTV);
        badgeMSG = (BadgeView) findViewById(R.id.badgeMSG);

        babyLayout.setOnClickListener(clickListener);
        babyGroupLayout.setOnClickListener(clickListener);
        nearbyLayout.setOnClickListener(clickListener);
        myLayout.setOnClickListener(clickListener);
        msgLayout.setOnClickListener(clickListener);
    }

    /**
     * 底部菜单选择事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.babyLayout:
                    clearFocus();
                    babyIV.setBackgroundResource(R.drawable.baby_checked);
                    babyTextView.setTextColor(getResources().getColor(R.color.bar_color_focus));
                    if (mBabyFragment == null) mBabyFragment = BabyFragment.newInstance();
                    switchFragment(mCurrentFragment, mBabyFragment);
                    break;
                case R.id.babyGroupLayout:
                    clearFocus();
                    babyGroupIV.setBackgroundResource(R.drawable.baby_group_checked);
                    babyGroupTextView.setTextColor(getResources().getColor(R.color.bar_color_focus));
                    if (mBabyGroupFragment == null) mBabyGroupFragment = BabyGroupFragment.newInstance();
                    switchFragment(mCurrentFragment, mBabyGroupFragment);
                    if (mBabyFragment.mTimer != null) {
                        mBabyFragment.mTimer.cancel();
                    }
                    break;
                case R.id.nearbyLayout:
                    clearFocus();
                    nearbyIV.setBackgroundResource(R.drawable.nearby_checked);
                    nearbyTextView.setTextColor(getResources().getColor(R.color.bar_color_focus));
                    if (mFoundFragment == null) mFoundFragment = FoundFragment.newInstance();
                    switchFragment(mCurrentFragment, mFoundFragment);
                    if (mBabyFragment.mTimer != null) {
                        mBabyFragment.mTimer.cancel();
                    }
                    break;
                case R.id.myLayout:
                    clearFocus();
                    myIV.setBackgroundResource(R.drawable.man_checked);
                    myTextView.setTextColor(getResources().getColor(R.color.bar_color_focus));
                    if (mMyFragment == null) mMyFragment = MyFragment.newInstance();
                    switchFragment(mCurrentFragment, mMyFragment);
                    if (mBabyFragment.mTimer != null) {
                        mBabyFragment.mTimer.cancel();
                    }
                    break;
                case R.id.msgLayout:
                    clearFocus();
                    msgIV.setBackgroundResource(R.drawable.msg_checked);
                    msgTextView.setTextColor(getResources().getColor(R.color.bar_color_focus));
                    if (msgMainFragment == null) {
                        msgMainFragment = MsgMainFragment.newInstance();
                    }
                    switchFragment(mCurrentFragment, msgMainFragment);
                    if (mBabyFragment.mTimer != null) {
                        mBabyFragment.mTimer.cancel();
                    }
                    break;
            }
        }
    };

    public void switchFragment(Fragment from, Fragment to) {
        if (from.equals(to)) return;
        if (to == null) return;
        FragmentTransaction transaction = this.fragmentManager.beginTransaction();
        if (!to.isAdded()) {
            // 隐藏当前的fragment，add下一个到Activity中
            transaction.hide(from).add(R.id.fragment_place, to).commit();
        } else {
            // 隐藏当前的fragment，显示下一个
            transaction.hide(from).show(to).commit();
        }
        this.mCurrentFragment = to;
    }

    /**
     * 清除所有的选中按钮
     */
    private void clearFocus() {
        babyIV.setBackgroundResource(R.drawable.baby_uncheck);
        babyGroupIV.setBackgroundResource(R.drawable.baby_group_uncheck);
        nearbyIV.setBackgroundResource(R.drawable.nearby_unckecked);
        myIV.setBackgroundResource(R.drawable.man_unchecked);
        msgIV.setBackgroundResource(R.drawable.msg_unckecked);

        TextView[] textViews = new TextView[]{babyTextView, babyGroupTextView, nearbyTextView, myTextView, msgTextView};
        for (TextView tv : textViews) {
            tv.setTextColor(getResources().getColor(R.color.bar_color_unfocus));
        }
        //清除可能存在的定位状态
        closeRealtimeLocation();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            logoutDialog();
//            return false;
//        }
//        return false;
//    }


    /**
     * 应用退出
     */
    protected void logoutDialog() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setMessage(R.string.confirm_logout_txt);
        builder.setTitle(R.string.msg_notify);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logout();
                AppContext.isEntered = false;
                dialogInterface.dismiss();

            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void setSelectedFragment(BackHandlerFragment selectedFragment) {
        this.mBackHandlerFragment = selectedFragment;
    }

    @Override
    public void onBackPressed() {
        //以后如果其他Fragment界面需要响应返回键，请将Fragment继承BackHandlerFragment,实现onBackPressed方法即可，可参考BabyFragment.java
        if (!mBackHandlerFragment.onBackPressed()) {
            closeRealtimeLocation();
        } else {
            // logoutDialog();
            //按两次返回键，隐藏当前应用
            if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
                currentBackPressedTime = System.currentTimeMillis();
                Toast.makeText(this, R.string.back_login_out_prompt, Toast.LENGTH_SHORT).show();
            } else {
                //虚假退出
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        }
    }


    /**
     * 清除定位状态
     */
    public void closeRealtimeLocation() {
        if (mBackHandlerFragment != null) {
            mBackHandlerFragment.onBackPressed();
        }
    }

}
