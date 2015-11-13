package com.bct.gpstracker.baby.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.JSONObject;
import org.simple.eventbus.Subscriber;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.adapter.MessageViewAdapter;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.AudioRecorder;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.msg.MsgMainFragment;
import com.bct.gpstracker.pojo.ChatMsg;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.MapEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.receiver.TrackerReceiver;
import com.bct.gpstracker.server.MessageSender;
import com.bct.gpstracker.ui.LoginActivity;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.view.EmoticonsEditText;
import com.bct.gpstracker.vo.ContType;
import com.bct.gpstracker.vo.Session;
import com.bct.gpstracker.vo.TermType;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

public class ChatActivity extends BaseActivity implements ChatTerMianlAddOneFragment.ChatAddOneFragmentCallBack,
        ChatTerMainEmojiFragment.ChatTerMainEmojiFragmentCallBack {

    private static int MIX_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1
    private static int RECORD_NO = 0; // 不在录音
    private static int RECORD_ING = 1; // 正在录音
    private static int RECODE_ED = 2; // 完成录音
    private static int RECODE_STATE = 0; // 录音的状态
    private static float recodeTime = 0.0f; // 录音的时间
    private static double voiceValue = 0.0; // 麦克风获取的音量值
    private ImageView dialog_img;
    private ProgressBar dialog_pro;//录音进度条
    private Dialog dialog;
    private AudioRecorder mr;
    private Thread recordThread;
    private boolean hasVoice = false, isInVoice = false;

    //-------------------

    private Context mContext = ChatActivity.this;
    private CameraUtil mCameraUtil;

    private ImageButton backButton, locationButton, voiceButton, expressionButton, addMorBtn, phoneButton;
    private LinearLayout chatAddSelect, selectImageLayout, selectVideoLayout, selectCameraLayout;
    MapEntity mEntity;
    private EmoticonsEditText messageText;
    private TextView titleView;
    private ViewPager mViewPager, mSelectEmoji;
    private LinearLayout mTabLineLLayout, mTablineEmojiLayout;
    private ImageView mTabLineIv, mTabLineEmojiIv;
    private List<Fragment> mFragmentAddList = new ArrayList<>();
    //	private LinearLayout playLayout;
//	private TextView userNameView;
//	private ImageView playView;
    private ListView listView;
    private Button sendButton, voiceBar;
    private boolean selecState = true;  //+号的点击状态
    private boolean selecEmojiState = true;  //表情的点击状态
    private int currentIndex;   // ViewPager的当前选中页
    private int screenWidth;    //屏幕的宽度
    public static String mEntityImei;   //IMEI
    protected static Device device;     //Device
    public static List<String> mEmoticonsNewGifCopy = new ArrayList<>();
    public static List<String> mEmoticons_ZgifCopy = new ArrayList<>();
    private InputMethodManager imm; //输入法管理器
    private SharedPreferences mSharedPreferences;


    /**
     * 选择照片
     */
    private final static int OPEN_GALLERY = 799;

    private List<ChatMsg> messages = new ArrayList<>();
    private MessageViewAdapter mAdapter;
    private String folderName = "";    //文件夹的名称
    private long amrName;//语音文件的名称

    private Messenger messenger;
    private static ChatActivity chatActivity = null;
    private ChatHandler chatHandler = new ChatHandler();

    public static boolean isActive = false;
    public final int UPDATE_CHAT_UI = 1;
    public final int UPDATE_CHAT_FAILED = 2;
    public final int SEND_CHAT_COUNT = 3;

    public static ChatActivity getChatActivity() {
        return chatActivity;
    }

    public ChatHandler getHandler() {
        return chatHandler;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        //关闭消息提示
        TrackerReceiver.clearNotification(99);
        //隐藏底部菜单界面
        hideFootMenu();
        selecState = true;
//        initNewMsg(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initNewMsg(intent);
    }

    @Override
    protected void onDestroy() {
        mEntityImei = "";
        device = null;
        isActive = false;
        if (serviceConn != null) {
            try {
                unbindService(serviceConn);
            } catch (Exception e) {
                //
            }
        }

        AppContext.getEventBus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatActivity = this;
        //对于已退出的，需要重新登录
        if (CommUtil.isEmpty(Session.getInstance().getUserList())) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
//        ViewUtils.inject(this);
        //Android 5.0之后需要采用显式意图启动Service,旧有方法为隐式意图
        // bindService(new Intent(Constants.ACTION_COMM_SERVICE), serviceConn, BIND_AUTO_CREATE);
        Intent intent = new Intent().setAction(Constants.ACTION_COMM_SERVICE).setPackage(Constants.PACKAGER);
        bindService(intent, serviceConn, BIND_AUTO_CREATE);
        mCameraUtil = new CameraUtil(this);

        mSharedPreferences = Utils.getPreferences(mContext);

        AppContext.getEventBus().register(this);

        setContentView(R.layout.activity_chat);

        mEntity = (MapEntity) getIntent().getSerializableExtra("chat");
        //获得IMEI
        mEntityImei = mEntity.getImei();
        //获得Device
        device = Session.getInstance().getDevice(mEntityImei);
        folderName = String.valueOf(Math.abs((mEntity.getImei()).hashCode()) % 100);
//		 try {
//			FileUtils.saveFolder(ChatActivity.this, Utils.Md5(mEntity.getImei()+Session.getInstance().getUser().getPhone()));
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}

        backButton = (ImageButton) findViewById(R.id.backBtn);
        messageText = (EmoticonsEditText) findViewById(R.id.et_sendmessage);
        titleView = (TextView) findViewById(R.id.titleNameTV);
        locationButton = (ImageButton) findViewById(R.id.locationBtn);
        voiceButton = (ImageButton) findViewById(R.id.voiceBtn);
        phoneButton = (ImageButton) findViewById(R.id.phoneBtnChat);
        sendButton = (Button) findViewById(R.id.sendBtn);
//        表情控件
        expressionButton = (ImageButton) findViewById(R.id.expressionBtn);

        voiceBar = (Button) findViewById(R.id.voiceBar);
        chatAddSelect = (LinearLayout) findViewById(R.id.chat_add_select);
        selectImageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        selectCameraLayout = (LinearLayout) findViewById(R.id.cameraLayout);
        selectVideoLayout = (LinearLayout) findViewById(R.id.videoLayout);
        listView = (ListView) findViewById(R.id.listview);
        //更多控件
        addMorBtn = (ImageButton) findViewById(R.id.addMoreBtn);
        mViewPager = (ViewPager) findViewById(R.id.chat_add_select_vp);
        mTabLineLLayout = (LinearLayout) findViewById(R.id.chat_add_tab_line_llayout);
        mTabLineIv = (ImageView) findViewById(R.id.chat_add_tab_line_iv);


		 /*播放声音*/
//		 playLayout = (LinearLayout) findViewById(R.id.playLayout);
//		 userNameView = (TextView) findViewById(R.id.userNameTV);
//		 playView = (ImageView) findViewById(R.id.playIV);
//		 if(mEntity.getVoiceUrl()!=null){
//			 playLayout.setVisibility(View.VISIBLE);
//		 }
//		 userNameView.setText(mEntity.getName());
//		 playView.setOnClickListener(clickListener);
         /**/


        titleView.setText(mEntity.getName());
        backButton.setOnClickListener(clickListener);
        locationButton.setOnClickListener(clickListener);
        expressionButton.setOnClickListener(clickListener);
        selectImageLayout.setOnClickListener(clickListener);
        selectCameraLayout.setOnClickListener(clickListener);
        selectVideoLayout.setOnClickListener(clickListener);
        addMorBtn.setOnClickListener(clickListener);
        mViewPager.setOnClickListener(clickListener);
        phoneButton.setOnClickListener(clickListener);

//		 uiHandler = new UIHandler();
//		 messageText.setOnClickListener(clickListener);
        messageText.setMovementMethod(ScrollingMovementMethod.getInstance());
            /*初始化数据 */
        mAdapter = new MessageViewAdapter(ChatActivity.this, messages, mEntity.getPortrait());
        listView.setAdapter(mAdapter);

        //保存新表情数据到sp中（暂时存放在此处）
        saveEmoji();

        initData();
        initEvents();

    }

    public ListView getListView() {
        return listView;
    }

    /**
     * 考虑到后期对更新表情包的位置修改，将表情数据存储到sp中,
     * 暂时放置到此处，以后可根据需求进行挪动
     */
    private void saveEmoji() {
        if (MsgMainFragment.mEmoticonsNewGif.size() != 0) {
            String newEmoji = CommUtil.join(MsgMainFragment.mEmoticonsNewGif.toArray(), ",");//Utils.listToString(MsgMainFragment.mEmoticonsNewGif);
            //保存表情数据到SP中
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(MyConstants.EMOJI_APP_NEW, newEmoji);
            editor.apply();
        }

    }

    private void initEvents() {
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageText.getText().length() > 0) {
                    addMorBtn.setVisibility(View.GONE);
                    sendButton.setVisibility(View.VISIBLE);
                } else {
                    sendButton.setVisibility(View.GONE);
                    addMorBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        messageText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText.requestFocus();
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                imm.showSoftInput(messageText, 0);
                imm.showSoftInputFromInputMethod(messageText.getWindowToken(), InputMethodManager.SHOW_FORCED);
                //隐藏底部菜单界面
                hideFootMenu();
            }
        });
        messageText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    imm.showSoftInput(messageText, 0);
                    imm.showSoftInputFromInputMethod(messageText.getWindowToken(), InputMethodManager.SHOW_FORCED);
                } else {
                    imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);
                }
                //隐藏底部菜单界面
                hideFootMenu();
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = messageText.getText().toString().trim();
//                if (CommUtil.isBlank(txt)) {
//                    return;
//                }
                if (!TextUtils.isEmpty(txt)) {
                    sendTextMsg(txt);

                    //隐藏底部菜单界面
                    hideFootMenu();
                }
                // messageText.requestFocus();

            }
        });
        voiceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (isInVoice) {
                    messageText.setVisibility(View.VISIBLE);
                    voiceButton.setImageResource(R.drawable.chat_icon_voice);
                    voiceBar.setVisibility(View.GONE);
                    messageText.requestFocus();
                    imm.showSoftInput(messageText, 0);
                    imm.showSoftInputFromInputMethod(messageText.getWindowToken(), InputMethodManager.SHOW_FORCED);
                    isInVoice = false;
                } else {
                    voiceButton.setImageResource(R.drawable.keyboard);
                    messageText.setVisibility(View.GONE);
                    voiceBar.setVisibility(View.VISIBLE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);
                    }
                    isInVoice = true;
                }
            }
        });
        voiceBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(voiceBar.getLayoutParams());
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (RECODE_STATE != RECORD_ING) {
                            //voiceBar.setBackgroundResource(R.drawable.chat_content_edit_dark);
                            voiceBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_content_edit_dark));
                            voiceBar.setLayoutParams(params);
                            String sdState = Environment.getExternalStorageState();// 获得sd卡的状态
                            if (!sdState.equals(Environment.MEDIA_MOUNTED)) { // 判断SD卡是否存在
                                // 提示sd卡不存在
                                Toast.makeText(mContext, R.string.media_mounted_no, Toast.LENGTH_SHORT).show();
                                return false;
                            }
//                            deleteOldFile();
                            amrName = System.currentTimeMillis();
                            mr = new AudioRecorder(ChatActivity.this, folderName, amrName + "");
                            RECODE_STATE = RECORD_ING;
                            showVoiceDialog();
                            try {
                                mr.start();
                            } catch (Exception e) {
                                // FIXME 在华为cxxxx上会崩溃
                                e.printStackTrace();
                            }
                            recordThread = new Thread(RecordThread);
                            recordThread.start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (RECODE_STATE == RECORD_ING) {
                            RECODE_STATE = RECODE_ED;
//                            voiceBar.setBackground(getResources().getDrawable(R.drawable.chat_content_edit,null));
                            voiceBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_content_edit));
                            voiceBar.setLayoutParams(params);
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            try {
                                if (mr != null) {
                                    mr.stop();
                                }
                                voiceValue = 0.0;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (recodeTime < MIX_TIME) {
                                showWarnToast();
//								record.setText("按住开始录音");
                                RECODE_STATE = RECORD_NO;
                            } else {
                                // TO-DO
//								record.setText("录音完成!点击重新录音");
                                hasVoice = true;
                                sendVoice();
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }


    private void sendTextMsg(String txt) {
        messageText.setText(null);
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setIsSend(true);
        chatMsg.setImei(mEntity.getImei());
        chatMsg.setType(ContType.TXT.getType());
        chatMsg.setTermType(mEntity.getTermType().getType());
        chatMsg.setContent(txt);
        chatMsg.setUserId(Session.getInstance().getLoginedUserId());
        chatMsg.setTime(System.currentTimeMillis());
        sendMsg(chatMsg);
    }

    public void hideFootMenu() {
        chatAddSelect.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        mTabLineLLayout.setVisibility(View.GONE);
    }

    @Subscriber(tag = Constants.EVENT_TAG_CHAT_DISPLAYMSG)
    private void displayMsg(ChatMsg chatMsg) {
        if (mEntityImei != null && mEntityImei.equals(chatMsg.getImei())) {
            messages.add(chatMsg);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sendMsg(ChatMsg chatMsg) {
        messages.add(chatMsg);
        mAdapter.notifyDataSetChanged();
        try {
            AppContext.db.saveBindingId(chatMsg);
        } catch (Exception e) {
            Log.e(Constants.TAG, "保存聊天信息到数据库失败！", e);
        }
        AppContext.getEventBus().post(chatMsg, Constants.EVENT_TAG_CHAT_SEND);
    }

    /**
     * 初始化语音记录
     */
    private void initData() {
        try {
            long userId = Session.getInstance().getLoginedUserId();
            if (userId == 0) {
                return;
            }
            markReaded();
            List<ChatMsg> msgs = AppContext.db.findAll(Selector.from(ChatMsg.class).where(WhereBuilder.b("user_id", "=", userId)
                    .and("imei", "=", mEntity.getImei())).orderBy("time", true).limit(30));
            Collections.reverse(msgs);
            messages.addAll(msgs);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改当前用户的当前好友的消息为已读
     *
     * @throws DbException
     */
    private void markReaded() {
        try {
            long userId = Session.getInstance().getLoginedUserId();
            ChatMsg readMsg = new ChatMsg();
            readMsg.setIsRead(1);
            AppContext.db.update(readMsg, WhereBuilder.b("user_id", "=", userId).and("imei", "=", mEntity.getImei()), "is_read");
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    hideSoftInputWindow();
                    ChatActivity.this.finish();
                    break;
//			case R.id.et_sendmessage:
//				
//				break;
//			case R.id.playIV:
//				downloadVoice(mEntity.getVoiceUrl());
//				break;
                case R.id.imageLayout:
                    //选择图片
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, OPEN_GALLERY);
                    break;
                case R.id.cameraLayout:
                    mCameraUtil.openCamera();
                    break;
                case R.id.addMoreBtn:
                    //点击+号按钮
                    if (selecState) {
                        viewPagerClear();
                        //加载底部菜单数据
                        footTerMinalMenuData();
                        if (mEntity.getTermType().equals(TermType.APP)) {
                            //非终端数据，在此指代监护人数据(以后如果涉及的功能增多，一页显示不完，可以使用Fragment的方法显示，可参考ChatTerMianlAddOneFragment.java)
                            mViewPager.setVisibility(View.GONE);
                            mTabLineLLayout.setVisibility(View.GONE);
                            chatAddSelect.setVisibility(View.VISIBLE);
                        } else {
                            //显示选择界面
                            chatAddSelect.setVisibility(View.GONE);
                            mViewPager.setVisibility(View.VISIBLE);
                            mTabLineLLayout.setVisibility(View.VISIBLE);
                        }
                        selecState = !selecState;
                        selecEmojiState = true;
                        hideSoftInputWindow();
                    } else {
                        //隐藏选择界面
                        hideFootMenu();
                        //清除底部菜单数据
                        mFragmentAddList.clear();
                        mViewPager.removeAllViews();

                        selecState = !selecState;

                    }
                    break;
                case R.id.expressionBtn:
                    //表情
                    Log.d(Constants.TAG, "这里是点的了");

                    if (selecEmojiState) {
                        hideSoftInputWindow();
                        //清除底部菜单数据
                        viewPagerClear();
                        //清除变量数据
                        mEmoticonsNewGifCopy.clear();
                        mEmoticons_ZgifCopy.clear();
                        //设置中间数据
                        String newEmoji = mSharedPreferences.getString(MyConstants.EMOJI_APP_NEW, "");
                        if (!"".equals(newEmoji)) {
                            String[] split = newEmoji.split(",");
                            for (String emoji : split) {
                                mEmoticonsNewGifCopy.add(emoji);
                            }
                        } else {
                            //没有新表情数据，则加载APP自带的表情
                            String APPEmoji = mSharedPreferences.getString(MyConstants.EMOJI_APP_OLD, "");
                            if (!"".equals(APPEmoji)) {
                                String[] split = APPEmoji.split(",");
                                for (String emoji : split) {
                                    mEmoticons_ZgifCopy.add(emoji);
                                }
                            }
                        }
                        int tab;
                        if (mEmoticonsNewGifCopy.size() != 0) {
                            tab = mEmoticonsNewGifCopy.size() % 8 == 0 ? mEmoticonsNewGifCopy.size() / 8 : mEmoticonsNewGifCopy.size() / 8 + 1;
                        } else {
                            tab = mEmoticons_ZgifCopy.size() % 8 == 0 ? mEmoticons_ZgifCopy.size() / 8 : mEmoticons_ZgifCopy.size() / 8 + 1;
                        }
                        //根据Tab个数，来设置有几个fragment界面
                        for (int i = 0; i < tab; i++) {
                            ChatTerMainEmojiFragment emojiFragment = ChatTerMainEmojiFragment.newInstance(i + 1, tab);
                            mFragmentAddList.add(emojiFragment);
                        }
                        bottomViewPager(mFragmentAddList);
                        //设置输入框
                        messageText.setVisibility(View.VISIBLE);
                        voiceButton.setImageResource(R.drawable.chat_icon_voice);
                        voiceBar.setVisibility(View.GONE);
                        //显示选择界面
                        mViewPager.setVisibility(View.VISIBLE);
                        mTabLineLLayout.setVisibility(View.VISIBLE);

                        selecState = true;
                        selecEmojiState = !selecEmojiState;
                    } else {
                        //隐藏选择界面
                        hideFootMenu();
                        //清除底部菜单数据
                        mFragmentAddList.clear();
                        mViewPager.removeAllViews();

                        selecEmojiState = !selecEmojiState;
                    }
                    if (chatAddSelect.isShown()) {
                        chatAddSelect.setVisibility(View.GONE);
                    }
                    break;
                case R.id.phoneBtnChat:
                    onPhoneButtonClick(v);
                    break;
            }
        }
    };

    private void viewPagerClear() {
        //清除底部菜单数据
        mFragmentAddList.clear();
        mViewPager.removeAllViews();
    }

    private void hideSoftInputWindow() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);
    }


    /**
     * 终端底部“+”菜单
     */
    public void footTerMinalMenuData() {

        //.beginTransaction()
        //获得Fragment对象
        ChatTerMianlAddOneFragment mChatTerMianlAddOneFragment = ChatTerMianlAddOneFragment.newInstance();
        ChatTerMianlAddTwoFragment mChatTerMianlAddTwoFragment = ChatTerMianlAddTwoFragment.newInstance();

        mFragmentAddList.add(mChatTerMianlAddOneFragment);
        mFragmentAddList.add(mChatTerMianlAddTwoFragment);
        bottomViewPager(mFragmentAddList);
    }

    /**
     * 底部ViewPager，显示“+”菜单和表情
     */
    private void bottomViewPager(final List<Fragment> fragmentLists) {
        final int tabNum = fragmentLists.size();
        //设置滑动条
        initTabLineWidth(tabNum);
        //创建Fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentAdapter mFragmenAdapter = new FragmentAdapter(fragmentManager, fragmentLists);
        mViewPager.setAdapter(mFragmenAdapter);
        //默认显示第一个Fragment
        mViewPager.setCurrentItem(0);

        //滑动切换
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            /**
             * @param position  当前页面，及你点击滑动的页面
             * @param offset    当前页面偏移的百分比
             * @param offsetPixels  当前页面偏移的像素位置
             */
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
                        .getLayoutParams();
                /**
                 * 利用currentIndex(当前所在页面)和position(下一个页面)以及offset来
                 * 设置mTabLineIv的左边距 滑动场景：
                 * 记2个页面,
                 * 从左到右分别为0,1
                 * 0->1; 1->0
                 */
                if (currentIndex == 0 && position == 0)// 0->1
                {
                    lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 2) + currentIndex
                            * (screenWidth / 2));

                } else if (currentIndex == 1 && position == 0) // 1->0
                {
                    lp.leftMargin = (int) (-(1 - offset)
                            * (screenWidth * 1.0 / 2) + currentIndex
                            * (screenWidth / 2));

                }
                /*如果有第三个界面，直接使用下面代码即可*/
                else if (currentIndex == 1 && position == 1 && tabNum == 3) // 1->2
                {
                    lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));
                } else if (currentIndex == 2 && position == 1 && tabNum == 3) // 2->1
                {
                    lp.leftMargin = (int) (-(1 - offset)
                            * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));
                }
                mTabLineIv.setLayoutParams(lp);
            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //滑动状态， 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
            }
        });
    }

    /**
     * 设置滑动条的宽度为屏幕的1/2(根据Tab的个数而定)
     */
    private void initTabLineWidth(int tab) {
        DisplayMetrics dpMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(dpMetrics);
        screenWidth = dpMetrics.widthPixels;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
                .getLayoutParams();
        lp.width = screenWidth / tab;
        mTabLineIv.setLayoutParams(lp);
    }

    @Override
    public void selectData(Intent data) {
        if (data != null && CommUtil.isNotBlank(data.getData())) {
            ContentResolver resolver = getContentResolver();
            byte[] bytes = null;
            try {
                InputStream in = resolver.openInputStream(data.getData());
                bytes = FileUtils.readInputStream(in);
            } catch (Exception e) {
                Log.e(Constants.TAG, "读取文件失败！", e);
            }
            if (bytes == null || bytes.length == 0) {
                return;
            }

            Bitmap bitmap = MediaUtil.scaleToSettingSize(bytes);
            if (bitmap == null) {
                Toast.makeText(ChatActivity.this, R.string.send_pic_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bout);

            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setIsSend(true);
            chatMsg.setImei(mEntity.getImei());
            chatMsg.setType(ContType.PIC.getType());
            chatMsg.setTermType(mEntity.getTermType().getType());
            chatMsg.setUserId(Session.getInstance().getLoginedUserId());
            chatMsg.setTime(System.currentTimeMillis());

            try {
                String path = FileUtils.saveFile(this, folderName, System.currentTimeMillis() + ".jpg", bout.toByteArray());
                chatMsg.setLocalUrl(path);
            } catch (IOException e) {
                Log.e(Constants.TAG, "保存图片失败！", e);
            }
            sendMsg(chatMsg);
        }
    }

    @Override
    public void emojiData(String emojiFlag) {
        if (emojiFlag != null && CommUtil.isNotBlank(emojiFlag)) {
            sendTextMsg(emojiFlag);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//       if (requestCode == OPEN_GALLERY&&resultCode==RESULT_OK) {
//			if(data.getData()!=null){
//				startPhotoZoom(data.getData());
//			}
//		}else if (requestCode==3) {


        if (requestCode == Constants.CAMERA_REQUEST_CODE) { //拍照完成后
            Log.d(Constants.TAG, "拍照完成");

            byte[] phono = mCameraUtil.getPhono();
            Bitmap bitmap = null;
            if (null != phono && phono.length > 0) {
                bitmap = MediaUtil.scaleToSettingSize(mCameraUtil.getPhono());
            }
            if (null != bitmap) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bout);
                Log.d(Constants.TAG, "图片的大小：：：：：" + bout.toByteArray().length / 1024);

                ChatMsg chatMsg = new ChatMsg();
                chatMsg.setIsSend(true);
                chatMsg.setImei(mEntity.getImei());
                chatMsg.setType(ContType.PIC.getType());
                chatMsg.setTermType(mEntity.getTermType().getType());
                chatMsg.setUserId(Session.getInstance().getLoginedUserId());
                chatMsg.setTime(System.currentTimeMillis());
                try {
                    String path = FileUtils.saveFile(this, folderName, System.currentTimeMillis() + ".jpg", bout.toByteArray());
                    chatMsg.setLocalUrl(path);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "保存图片失败！", e);
                }
                sendMsg(chatMsg);
            } else {
                Log.d(Constants.TAG, "photo为空！！！");
            }

//            mCameraUtil.receiveResult();
//            if (null != data) {
//                //这里，先保存图片，再发送
//                mCameraUtil.saveImage(data);
//
//                Bundle extras = data.getExtras();
//                if (extras != null) {
//                    final Bitmap bitmap = extras.getParcelable("data");
//                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
//
//                    ChatMsg chatMsg = new ChatMsg();
//                    chatMsg.setIsSend(true);
//                    chatMsg.setImei(mEntity.getImei());
//                    chatMsg.setType(ContType.PIC.getType());
//                    chatMsg.setTermType(mEntity.getTermType().getType());
//                    chatMsg.setUserId(Session.getInstance().getLoginedUserId());
//                    chatMsg.setTime(System.currentTimeMillis());
//                    try {
//                        String path = FileUtils.saveFile(this, folderName, System.currentTimeMillis() + ".jpg", bout.toByteArray());
//                        chatMsg.setLocalUrl(path);
//                    } catch (IOException e) {
//                        Log.e(Constants.TAG, "保存图片失败！", e);
//                    }
//                    sendMsg(chatMsg);
//
//                }
//            }
        }

        if (data != null && CommUtil.isNotBlank(data.getData())) { //选取图片发送
            ContentResolver resolver = getContentResolver();
            byte[] bytes = null;
            try {
                InputStream in = resolver.openInputStream(data.getData());
                bytes = FileUtils.readInputStream(in);
            } catch (Exception e) {
                Log.e(Constants.TAG, "读取文件失败！", e);
            }
            if (bytes == null || bytes.length == 0) {
                return;
            }

            Bitmap bitmap = MediaUtil.scaleToSettingSize(bytes);
            if (bitmap == null) {
                Toast.makeText(ChatActivity.this, R.string.send_pic_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bout);

            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setIsSend(true);
            chatMsg.setImei(mEntity.getImei());
            chatMsg.setType(ContType.PIC.getType());
            chatMsg.setTermType(mEntity.getTermType().getType());
            chatMsg.setUserId(Session.getInstance().getLoginedUserId());
            chatMsg.setTime(System.currentTimeMillis());

            try {
                String path = FileUtils.saveFile(this, folderName, System.currentTimeMillis() + ".jpg", bout.toByteArray());
                chatMsg.setLocalUrl(path);
            } catch (IOException e) {
                Log.e(Constants.TAG, "保存图片失败！", e);
            }
            sendMsg(chatMsg);
        }
//		}
    }


    // 删除老文件
    private void deleteOldFile() {
        File file = new File(Utils.getAvailableStoragePath(), "gpstracker/voice.amr");
        if (file.exists()) {
            file.delete();
        }
    }

    // 录音时显示麦克风图片的Dialog
    protected void showVoiceDialog() {
        dialog = new Dialog(ChatActivity.this, R.style.DialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.my_dialog);
        dialog_img = (ImageView) dialog.findViewById(R.id.dialog_img);
        dialog_pro = (ProgressBar) dialog.findViewById(R.id.dialog_pro);
        dialog_pro.setMax(Constants.VOICE_MAX_TIME * 10);
        dialog.show();
    }

    // 录音时间太短时Toast显示
    void showWarnToast() {
        //deleteOldFile();
        Toast toast = new Toast(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(20, 20, 20, 20);

        // 定义一个ImageView
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.voice_to_short); // 图标

        TextView mTv = new TextView(mContext);
        mTv.setGravity(0x01);
        mTv.setText(R.string.recording_fail);
        mTv.setTextSize(14);
        mTv.setTextColor(Color.WHITE);// 字体颜色
        // mTv.setPadding(0, 10, 0, 0);

        // 将ImageView和ToastView合并到Layout中
        linearLayout.addView(imageView);
        linearLayout.addView(mTv);
        linearLayout.setGravity(Gravity.CENTER);// 内容居中
        linearLayout.setBackgroundResource(R.drawable.record_bg);// 设置自定义toast的背景

        toast.setView(linearLayout);
        toast.setGravity(Gravity.CENTER, 0, 0);// 起点位置为中间 100为向下移100dp
        toast.show();
    }

    // 录音线程
    private Runnable RecordThread = new Runnable() {

        @Override
        public void run() {
            recodeTime = 0.0f;
            while (RECODE_STATE == RECORD_ING) {
                if (recodeTime >= Constants.VOICE_MAX_TIME && Constants.VOICE_MAX_TIME != 0) {
                    //System.out.println("超过时间了");
                    imgHandle.sendEmptyMessage(0);
                } else {
                    try {
                        Thread.sleep(200);
                        recodeTime += 0.2;
                        if (RECODE_STATE == RECORD_ING) {
                            voiceValue = mr.getAmplitude();
                            imgHandle.sendEmptyMessage(1);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Handler imgHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:
                        // 录音超过15秒自动停止
                        if (RECODE_STATE == RECORD_ING) {
                            RECODE_STATE = RECODE_ED;
                            voiceBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_content_edit));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(voiceBar.getLayoutParams());
                            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            voiceBar.setLayoutParams(params);
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                //更改 录音超时Toast @author zhongjiayuan
//                                Toast timeOuToast = Toast.makeText(ChatActivity.this, "录音超时,自动发送", Toast.LENGTH_SHORT);
//                                timeOuToast.setGravity(Gravity.CENTER, 0, 0);
//                                timeOuToast.show();
                            }
                            try {
                                mr.stop();
                                voiceValue = 0.0;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (recodeTime < 1.0) {
                                showWarnToast();
//									record.setText("按住开始录音");
                                RECODE_STATE = RECORD_NO;
                            } else {
//									record.setText("录音完成!点击重新录音");
                                hasVoice = true;
                                sendVoice();
                            }
                        }
                        break;
                    case 1:
                        setDialogImage();
                        setDialogPro();
                        break;

                    default:
                        break;
                }
            }
        };
    };

    //设置dialog计时进度条
    private void setDialogPro() {
        dialog_pro.setProgress((int) recodeTime * 10);
    }

    // 录音Dialog图片随声音大小切换
    void setDialogImage() {
        if (voiceValue < 200.0) {
            dialog_img.setImageResource(R.drawable.record_animate_01);
        } else if (voiceValue > 200.0 && voiceValue < 400) {
            dialog_img.setImageResource(R.drawable.record_animate_02);
        } else if (voiceValue > 400.0 && voiceValue < 800) {
            dialog_img.setImageResource(R.drawable.record_animate_03);
        } else if (voiceValue > 800.0 && voiceValue < 1600) {
            dialog_img.setImageResource(R.drawable.record_animate_04);
        } else if (voiceValue > 1600.0 && voiceValue < 3200) {
            dialog_img.setImageResource(R.drawable.record_animate_05);
        } else if (voiceValue > 3200.0 && voiceValue < 5000) {
            dialog_img.setImageResource(R.drawable.record_animate_06);
        } else if (voiceValue > 5000.0 && voiceValue < 7000) {
            dialog_img.setImageResource(R.drawable.record_animate_07);
        } else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_08);
        } else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_09);
        } else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_10);
        } else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_11);
        } else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_12);
        } else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_13);
        } else if (voiceValue > 28000.0) {
            dialog_img.setImageResource(R.drawable.record_animate_14);
        }
    }


    /**
     * 发送语音
     */
    private void sendVoice() {
        String filePath = folderName + "/" + amrName + ".amr";
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setTime(System.currentTimeMillis());
        chatMsg.setLocalUrl(filePath);
        chatMsg.setType(ContType.AUDIO.getType());
        chatMsg.setTermType(mEntity.getTermType().getType());
        chatMsg.setIsSend(true);
        chatMsg.setImei(mEntity.getImei());
        chatMsg.setUserId(Session.getInstance().getLoginedUserId());
        try {
            Long duration = MediaUtil.getAmrDuration(this.getFilesDir() + "/" + filePath);
            if (duration > 0) {
                int dura = (int) (duration.doubleValue() / 1000 + 0.5);
                if (dura > Constants.VOICE_MAX_TIME) {
                    dura = Constants.VOICE_MAX_TIME;
                }
                chatMsg.setDuration(dura);
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "获取AMR文件时长失败！", e);
        }
        sendMsg(chatMsg);
    }

    private void initNewMsg(Intent intent) {
        LinkedList<Long> ids = new LinkedList<>();
        synchronized (AppContext.newChatMsgIds) {
            ids.addAll(AppContext.newChatMsgIds);
            AppContext.newChatMsgIds.clear();
        }
        if (CommUtil.isEmpty(ids)) {
            return;
        }
        long userId = Session.getInstance().getLoginedUserId();
        if (userId == 0) {
            return;
        }
        MapEntity mapEntity = (MapEntity) intent.getSerializableExtra("chat");
        if (!mapEntity.getImei().equals(mEntity.getImei())) {
            return;
        }
        mEntity = mapEntity;

        if (isActive) {
            markReaded();
        }
        try {
            long id = intent.getLongExtra("id", -1);
            ChatMsg chatMsg = AppContext.db.findById(ChatMsg.class, id);
            if (chatMsg != null && ContType.SOS_ALERT.getType() == chatMsg.getType()) {
                retrieveGeo(chatMsg, ids);
            } else {
                refreshMessages(ids);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void retrieveGeo(ChatMsg chatMsg, LinkedList<Long> ids) {
        if (chatMsg != null && CommUtil.isNotBlank(chatMsg.getOriginalContent())) {
            ResponseData resp = new ResponseData(chatMsg.getOriginalContent());
            if (resp.getBodyArray() == null) {
                return;
            }
            JSONObject json = resp.getBodyArray().optJSONObject(0);
            if (json == null) {
                return;
            }
            LatLonPoint pt = new LatLonPoint(json.optDouble("googlelat"), json.optDouble("googlelng"));
            //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
            RegeocodeQuery query = new RegeocodeQuery(pt, 5, GeocodeSearch.AMAP);
            GeocodeSearch mSearch = new GeocodeSearch(this);
            mSearch.setOnGeocodeSearchListener(new GeoCodeListener(ids, chatMsg));
            mSearch.getFromLocationAsyn(query);
        }
    }

    private void refreshMessages(LinkedList<Long> ids) {
        try {
            long userId = Session.getInstance().getLoginedUserId();
            List<ChatMsg> newMsgs = AppContext.db.findAll(Selector.from(ChatMsg.class).where(WhereBuilder.b("id", "in", ids).and("user_id", "=", userId)
                    .and("imei", "=", mEntity.getImei())).orderBy("time"));
            if (CommUtil.isNotEmpty(newMsgs)) {
                if (messages == null) {
                    messages = newMsgs;
                } else {
                    messages.addAll(newMsgs);
                    Collections.sort(messages, new Comparator<ChatMsg>() {
                        @Override
                        public int compare(ChatMsg lhs, ChatMsg rhs) {
                            return lhs.getTime().compareTo(rhs.getTime());
                        }
                    });
                }
                mAdapter.notifyDataSetChanged();
                if (isActive) {
                    TrackerReceiver.clearNotification(99);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "接收聊天消息出错", e);
            e.printStackTrace();
        }
    }


    private ServiceConnection serviceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
            String currUid = Session.getInstance().getImei();
            if (CommUtil.isNotBlank(currUid)) {
                Message msg = Message.obtain();
                msg.what = Constants.CHAT_LOGIN;
                msg.obj = currUid;
                try {
                    messenger.send(msg);
                } catch (Exception e) {
                    Log.e(Constants.TAG, "尝试登录失败！", e);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public class ChatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CHAT_UI:
                    Intent intent = (Intent) msg.obj;
                    initNewMsg(intent);
                    break;
                case UPDATE_CHAT_FAILED:
                    long id = (long) msg.obj;
                    markMsgFailed(id);
                    break;
                default:
                    break;
            }
        }
    }

    private void markMsgFailed(long id) {
        for (ChatMsg msg : messages) {
            if (msg.getId() == id) {
                msg.setSucc(0);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Subscriber(tag = Constants.EVENT_TAG_CHAT_PROGRESS)
    private void updateProgress(MessageSender.SenderMsgProgress progress) {
        int start = listView.getFirstVisiblePosition();
        int end = listView.getLastVisiblePosition();
        View view = null;
        int index = -1;
        for (int i = end; i >= start; i--) {
            if (messages.get(i).getId().longValue() == progress.getId().longValue()) {
                view = listView.getChildAt(i - start);
                index = i;
                break;
            }
        }
        mAdapter.updateProgress(view, index, progress.getProgress());
    }

    //@OnClick(R.id.phoneBtnChat)
    private void onPhoneButtonClick(View v) {
        //打电话
        String phoneNumber = mEntity.getPhone();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(mContext, R.string.phone_num_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Utils.isMobileNO(phoneNumber)) {
            Toast.makeText(mContext, R.string.phone_num_err, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
        startActivity(phoneIntent);
    }


    @Override
    public void onBackPressed() {
        hideSoftInputWindow();
        if (chatAddSelect.isShown() || mViewPager.isShown()) {
            hideFootMenu();
        } else {
            super.onBackPressed();
        }
    }

    private class GeoCodeListener implements GeocodeSearch.OnGeocodeSearchListener {
        private LinkedList<Long> latestMsgIds;
        private ChatMsg chatMsg;

        public GeoCodeListener(LinkedList<Long> latestMsgIds, ChatMsg chatMsg) {
            this.latestMsgIds = latestMsgIds;
            this.chatMsg = chatMsg;
        }

        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int i) {
            if (i == 0) {
                String address = result.getRegeocodeAddress().getFormatAddress();
                chatMsg.setContent(address);
                try {
                    AppContext.db.saveBindingId(chatMsg);
                    refreshMessages(latestMsgIds);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    }
}
