package com.bct.gpstracker.baby;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.*;
import com.amap.api.maps.model.*;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.CommHandler;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.baby.activity.NavigatorEntryActivity;
import com.bct.gpstracker.baby.activity.WarnActivity;
import com.bct.gpstracker.baby.adapter.BabyAdapter;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.LocationProvider;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.dialog.AddDeviceDialog;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.inter.NotifyCallback;
import com.bct.gpstracker.pojo.*;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.ui.BackHandlerFragment;
import com.bct.gpstracker.util.*;
import com.bct.gpstracker.view.CircleImageView;
import com.bct.gpstracker.view.TextMoveLayout;
import com.bct.gpstracker.vo.Msg;
import com.bct.gpstracker.vo.Session;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import static android.widget.Toast.makeText;

public class BabyFragment extends BackHandlerFragment implements AMap.OnMapClickListener, AMap.OnMarkerClickListener,
        GeocodeSearch.OnGeocodeSearchListener, AMap.InfoWindowAdapter, AMap.OnMapLoadedListener,Observer {

    private static final String TAG = BabyFragment.class.getSimpleName();
    private MapView mMapView;
    private AMap aMap;
    public List<MapEntity> mapList = new ArrayList<MapEntity>();
    //以后与终端相关的均取监控对象 Session.getInstance().getMonitors() 不再取终端
    private List<Device> deviceList = new ArrayList<>();
    private Device mDevice; // 当前选中的设备
    private MapEntity mEntity; // 当前选中设备的定位数据
    /**
     * 点击左上角宝宝列表的时候 弹出的下拉选择终端
     */
    private PopupWindow popupWindow;
    private ListView termListView;
    private View view;
    private int selectedPosition = 0;// 选中的位置
    private BabyAdapter groupAdapter;
    /**
     * 托动条的移动步调
     */
    private float moveStep = 0;
    /**
     * 自定义随着拖动条一起移动的空间
     */
    private TextMoveLayout textMoveLayout;
    private ViewGroup.LayoutParams layoutParams;

    private int screenWidth;
    private TextView textTime;

    private MarkerOptions deviceMarkerOptions;
    public Timer mTimer;
    private boolean isFirst = true;
    private boolean isNeedUpdate = false;
    private boolean isShowPointPauseSeekbar = false; //画历史轨迹是否暂停，用于seekbar的控制
    private boolean isShowPointPauseIV = false; //画历史轨迹是否暂停，用于开始暂停按钮的控制

    private View view1;
    private ImageView voiceView, hisControlStarOrPauseIV;
    private LinearLayout listLayout;
    private CircleImageView photoView;// 显示的头像
    private TextView nameView; // 显示的名称
    private LinearLayout reNotifyLayout;// 点击纠错之后出现的提示
    private LinearLayout successLayout;// 点击纠错之后出现的提示
    private LinearLayout historyControlLL;
    private RelativeLayout historyRL;
    // private boolean showLayout = false;
    private Button confirmButton, hisControlStopBtn;
    private ImageButton locationButton;
    private LocationProvider provider = null;
    private boolean realTimeLocation = false;
    private SeekBar historySeekBAr;  //控件历史轨迹长短的进度条
    private int historyTime = 0;  //从几点开始显示轨迹，通过进度条控制
    private int indexPoint = 1;
    private TextView historyHourTV; //用以显示seekbar所指示的时间
    private ScheduledExecutorService showNextPointThread; //用于画点的调度线程
    private ShowNextPointTask ShowNextPointTask;
    private int pointHour = 0; //根据轨迹点时间算出来的当天秒数
//    private boolean isSetCamera = false; //显示历史轨迹时，是否将第一个点居中显示

    /**
     * 反编码地理位置
     */
    private GeocodeSearch mSearch = null;
    private boolean isLatestPosition=true;


    public static BabyFragment newInstance() {
        BabyFragment newFragment = new BabyFragment();
        return newFragment;
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        isTicking = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        if (provider != null) {
            provider.stop();
        }
        // 关闭定位图层
        aMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        AppContext.getEventBus().unregister(this);
        Session.getInstance().deleteObserver(this);
        super.onDestroy();
    }


    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();

        if (deviceList.size() == 0) {
            //getDevice();
            getMoniter();
        } else {
            mDevice = deviceList.get(selectedPosition);
            Session.getInstance().setDevice(mDevice);
        }

        if (mDevice != null) { // 初始的时候显示
            if (CommUtil.isNotBlank(mDevice.getPortrait())) {
                ImageLoader.getInstance().displayImage(mDevice.getPortrait(), photoView, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (mDevice.getOnline() != 1) {
                            photoView.setImageBitmap(Utils.toGrayscale(loadedImage));
                        }
                    }
                });
                nameView.setText(mDevice.getName());
            } else {
                nameView.setText(mDevice.getName());
                photoView.setImageResource(R.drawable.user_no_photo);
                if (mDevice.getOnline() != 1) {
                    Utils.setGrayImageView(photoView);
                }
            }
            if (mEntity != null) {
                //System.out.println("实体不为空");
                initView();
            }else{
                getBabyData1();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view1 = inflater.inflate(R.layout.frament_baobao, container, false);
        AppContext.getEventBus().register(this);
        Session.getInstance().addObserver(this);

        voiceView = (ImageView) view1.findViewById(R.id.voiceBtn);
        listLayout = (LinearLayout) view1.findViewById(R.id.listLayout);
        photoView = (CircleImageView) view1.findViewById(R.id.photoIV);
        nameView = (TextView) view1.findViewById(R.id.nameTV);
        historyHourTV = (TextView) view1.findViewById(R.id.historyHourTV);
        reNotifyLayout = (LinearLayout) view1
                .findViewById(R.id.recorrectLayout);
        successLayout = (LinearLayout) view1.findViewById(R.id.successLayout);
        confirmButton = (Button) view1.findViewById(R.id.confirmBtn);
        locationButton = (ImageButton) view1.findViewById(R.id.locationBtn);
        historyControlLL = (LinearLayout) view1.findViewById(R.id.historyControlLL);
        hisControlStarOrPauseIV = (ImageView) view1.findViewById(R.id.hisControlStarOrPauseIV);
        hisControlStopBtn = (Button) view1.findViewById(R.id.hisControlStopBtn);
        historyRL = (RelativeLayout) view1.findViewById(R.id.historyRL);

        historySeekBAr = (SeekBar) view1.findViewById(R.id.historySeekBar);
        historySeekBAr.setOnSeekBarChangeListener(seekBarChangeListener);

        mMapView = (MapView) view1.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 必须要写
        aMap = mMapView.getMap();

        mSearch = new GeocodeSearch(getActivity());
        mSearch.setOnGeocodeSearchListener(this);
        // getBabyData(); //取数据


        //-----------------------------
        textMoveLayout = (TextMoveLayout) view1.findViewById(R.id.textMoveLayout);
        screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        layoutParams = new ViewGroup.LayoutParams(screenWidth, 10);
        textTime = new TextView(getActivity());
//        textTime.setBackgroundColor(Color.rgb(245, 245, 245));
        textTime.setTextColor(Color.rgb(0, 161, 229));
        textTime.setTextSize(16);
        textMoveLayout.addView(textTime, layoutParams);
        textTime.layout(0, 20, screenWidth, 80);
        moveStep = (float) (((float) screenWidth / (float) 86400) * 0.8);
        //--------------------------

        listLayout.setOnClickListener(clickListener);
        voiceView.setOnClickListener(clickListener);
        confirmButton.setOnClickListener(clickListener);
        locationButton.setOnClickListener(clickListener);
        hisControlStopBtn.setOnClickListener(clickListener);
        hisControlStarOrPauseIV.setOnClickListener(clickListener);

        aMap.setOnMapClickListener(this);
        aMap.setInfoWindowAdapter(this);
        aMap.setOnMapLoadedListener(this);
        provider = LocationProvider.getInstance(getActivity());
        provider.setNotify(Constants.UPDATE_SESSION_LOCATION, new NotifyCallback() {
            @Override
            public void execute(AMapLocation location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Session.getInstance().setLatLng(latLng);

                LatLng deviceLatLng = getReceivedLatLng();
                updateInfo(deviceLatLng);
            }
        });
        provider.startLocation();
        return view1;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showDevice();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMarker.hideInfoWindow();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getPosition() != null) {
            mMarker = marker;
            LatLng ll = marker.getPosition();
            reverseGeoCode(ll);
        }
        return false;
    }

    public void reverseGeoCode(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        LatLonPoint pt = new LatLonPoint(latLng.latitude, latLng.longitude);
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(pt, 5, GeocodeSearch.AMAP);
        mSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int i) {
        if (i == 0) {
            if (mEntity != null) {
                mEntity.setAddress(result.getRegeocodeAddress().getFormatAddress());
            }
            if (addressView != null) {
                addressView.setText(getActivity().getString(R.string.marker_location) + mEntity.getAddress());
            }
        }
//        mMarker.showInfoWindow();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 按钮点击事件的处理
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.listLayout: //左上角的设备列表
                    if (deviceList.size() != 0) {
                        showWindow(v);
                    }
                    break;
                case R.id.confirmBtn: // 确认纠偏成功
                    if (isNeedUpdate) {
                        isNeedUpdate = false;
                        recorrect(mMarker.getPosition());
                        mMarker.setDraggable(false);
                    } else {
                        mMarker.setDraggable(false);
                        reNotifyLayout.setVisibility(View.GONE);
                    }
                    getInfoWindowView(mMarker);
                    break;
                case R.id.voiceBtn:
                    showMenuWindow(v);
                    break;
                case R.id.locationBtn:
                    if (mDevice != null) {
                        sendCommand(MyConstants.LOCATION);
                    }
                    break;
                case R.id.hisControlStopBtn:
                    if (mDevice != null) {
                        //关闭轨迹显示
                        closeDataDialog();
                    }
                    break;
                case R.id.hisControlStarOrPauseIV:
                    if (historySeekBAr.getProgress() == 86400) { //如果已经到最后，就所有变量归位
                        historySeekBAr.setProgress(0);
                        currentPoint = 0;
                        aMap.clear();
                        points.clear();
                        ooPolyline = null;
                        indexPoint = 1;
                    }
                    if (isShowPointPauseIV) {
                        hisControlStarOrPauseIV.setImageResource(R.drawable.location_history_pause);
                        isShowPointPauseIV = !isShowPointPauseIV;
                    } else {
                        hisControlStarOrPauseIV.setImageResource(R.drawable.location_history_start);
                        isShowPointPauseIV = !isShowPointPauseIV;
                    }
                    break;
            }
        }
    };

    private void initView() {
        if (aMap != null && mEntity != null) {
            aMap.clear();
            if (mEntity.getLatitude() == 0 || mEntity.getLongitude() == 0) {
                provider.startLocation(new NotifyCallback() {
                    @Override
                    public void execute(AMapLocation location) {
                        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, Constants.MAP_LEVEL));
                    }
                });
                return;
            }
            LatLng cenpt = new LatLng(mEntity.getLatitude(), mEntity.getLongitude());
            deviceMarkerOptions = new MarkerOptions().title(mEntity.getName()).position(cenpt).zIndex(9).draggable(false);
            if (mDevice.getOnline() != 1) { //0为离线
                deviceMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark_gray));
            } else { //否则为在线
                deviceMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
            }
            mMarker = aMap.addMarker(deviceMarkerOptions);
            LatLng ll = mMarker.getPosition();
            aMap.setOnMarkerClickListener(this);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, Constants.MAP_LEVEL));
            if (CommUtil.isBlank(mEntity.getAddress())) {
                reverseGeoCode(ll);
            }
            mMarker.showInfoWindow();
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return getInfoWindowView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onMapLoaded() {
//        provider.setNotify("MAP_LOADED", new NotifyCallback() {
//            @Override
//            public void execute(AMapLocation location) {
//                provider.removeNotify("MAP_LOADED");
//                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
//                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
//            }
//        }).startLocation();
    }

    /**
     * 显示历史轨迹
     */
    private void showHistoryTrack() {
        if (null != showNextPointThread) {
            showNextPointThread.shutdown();
            showNextPointThread.shutdownNow();
            showNextPointThread = null;
        }
        if (aMap != null) {
            this.points.clear();
            this.ooPolyline = null;
            aMap.clear();
            //System.out.println("历史轨迹的数据长度:" + trackList.size());
            //List<LatLng> points = new ArrayList<LatLng>();
            //points.clear();
            indexPoint = 1;
            isShowPointPauseSeekbar = false;
            isShowPointPauseIV = false;
//            isSetCamera = false;
            hisControlStarOrPauseIV.setImageResource(R.drawable.location_history_pause);

            //这里根据seekBar，确定currentPoint从集合的哪开始取point
            //遍历集合，找出相对应的点

            checkThePoint();
            showNextPointThread = Executors.newSingleThreadScheduledExecutor();
            ShowNextPointTask = new ShowNextPointTask();

            showNextPointThread.scheduleWithFixedDelay(ShowNextPointTask, 0, 300, TimeUnit.MILLISECONDS);


        }
    }

    private void checkThePoint() {
        for (int i = 0; i < trackList.size(); i++) {
            String timeString = getTimeString(trackList.get(i).getDateTime());
            pointHour = CommUtil.totalSeconds("00:00:00", timeString);
            if (pointHour >= historyTime) {
                currentPoint = i;
                Log.d(TAG, "遍历出的角标：" + currentPoint);
                break;
            } else { //如果找不到，说明没有这么多点，则currentPoint为.size
                currentPoint = trackList.size() + 1;
            }
        }
    }

    private String getTimeString(String dateTime) {
        return dateTime.substring(11, 19);
    }

    private class ShowNextPointTask implements Runnable {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNextPoint();
                }
            });

        }
    }


    public void stopPointTimer() {
        if (null != showNextPointThread) {
            showNextPointThread.shutdown();
            showNextPointThread.shutdownNow();
            showNextPointThread = null;
        }
        trackList.clear();
        aMap.clear();
        //关闭时，隐藏停止按钮,隐藏进度条,seekbar归位
        historyRL.setVisibility(View.GONE);
        historySeekBAr.setProgress(0);
        historyTime = 0;
        //切换终端时，取消即时定位的状态
        isTicking = false;
        //清除dialog
        CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
    }

    private void showNextPoint() {
        if (!isShowPointPauseSeekbar && !isShowPointPauseIV) { //只有不暂停的时候才会画
            if (trackList.size() > this.currentPoint) {
                HistoryTrack historyTrack = trackList.get(currentPoint);
                LatLng latLng = new LatLng(historyTrack.getGooglelat(), historyTrack.getGooglelng());
//                if (!isSetCamera) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f);
                aMap.moveCamera(cameraUpdate);
//                    isSetCamera = true;
//                }
                Log.i(TAG, "轨迹时间：" + historyTrack.getDateTime());
                Activity a = getActivity();
                if (a != null) {
                    View view = getActivity().getLayoutInflater().inflate(R.layout.history_track_item, null);
                    TextView index = (TextView) view.findViewById(R.id.indexTV);
                    //currentPoint得用来确定集合的取出位置，setText中的用别的变量
                    index.setText(indexPoint++ + "");
                    BitmapDescriptor bemDescriptor = BitmapDescriptorFactory.fromView(view);
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(bemDescriptor).zIndex(9).draggable(false);
                    aMap.addMarker(markerOptions);
                    this.showPoint(latLng);
//
                    historySeekBAr.setProgress(CommUtil.totalSeconds("00:00:00", getTimeString(historyTrack.getDateTime())));
                    this.currentPoint++;
                }
            } else {
                hisControlStarOrPauseIV.setImageResource(R.drawable.location_history_start);
                isShowPointPauseIV = true;
                historySeekBAr.setProgress(86400);
            }
        }
    }

    private int currentPoint = 0;
    private LinkedList<LatLng> points = new LinkedList<>();
    private PolylineOptions ooPolyline = null;

    public void showPoint(LatLng latLng) {
        points.add(latLng);
        if (ooPolyline == null) {
            if (points.size() < 2) {
                return;
            }
            ooPolyline = new PolylineOptions().width(5).color(0xAAFF0000);
        }
        while(points.size()>2){
            points.poll();
        }
        ooPolyline.addAll(points);
        aMap.addPolyline(ooPolyline);
        aMap.setOnMarkerClickListener(null);
    }

    private void showWindow(View parent) {
        WindowManager windowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.baby_window_list, null);
            termListView = (ListView) view.findViewById(R.id.lvGroup);

            groupAdapter = new BabyAdapter(getActivity(), deviceList);
            termListView.setAdapter(groupAdapter);
            // 创建一个PopuWidow对象
            popupWindow = new PopupWindow(view, windowManager
                    .getDefaultDisplay().getWidth() / 2,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        groupAdapter.setSelectedPosition(selectedPosition);
        groupAdapter.notifyDataSetChanged();
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        int xPos = windowManager.getDefaultDisplay().getWidth() / 2
                - popupWindow.getWidth() / 2;

        popupWindow.showAsDropDown(parent, 0, 0);
        termListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) { //这里是点击的设备列表的条目
                stopPointTimer();
                selectedPosition = position;
                // mEntity = entityList.get(position);
                isFirst = true;
                updateMainDevice();
                getBabyData1();
                handler.removeMessages(4);
                CommUtil.hideProcessing();
                /** -------------------------------------- */
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    private void updateMainDevice() {
        if (CommUtil.isEmpty(deviceList) || selectedPosition < 0 || selectedPosition >= deviceList.size()) {
            return;
        }
        mDevice = deviceList.get(selectedPosition);
        Session.getInstance().setDevice(mDevice);

        if (reNotifyLayout != null && reNotifyLayout.isShown()) { // 如果在位置纠偏就关闭纠偏
            reNotifyLayout.setVisibility(View.GONE);
        }

        if (CommUtil.isNotBlank(mDevice.getPortrait())) {
            ImageLoader.getInstance().displayImage(mDevice.getPortrait(), photoView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (mDevice.getOnline() != 1) {
                        photoView.setImageBitmap(Utils.toGrayscale(loadedImage));
                    }
                }
            });
            nameView.setText(mDevice.getName());
        } else {
            nameView.setText(mDevice.getName());
            photoView.setImageResource(R.drawable.user_no_photo);
            if (mDevice.getOnline() != 1) { //0为设备离线
                Utils.setGrayImageView(photoView);
            }
        }
    }

    /**
     * 右上角菜单选择框
     */
    private PopupWindow menuWindow1;
    private View menuView1;
    private ImageView chatView, notifyView; // 有新消息和警报时显示的红点
    private LinearLayout chatLayout, callLayout, notifyLayout, recorrectLayout,
            historyLayout;

    private void showMenuWindow(View parent) {
        WindowManager windowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        if (menuWindow1 == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            menuView1 = layoutInflater.inflate(R.layout.menu_window_list, null);
            chatView = (ImageView) menuView1.findViewById(R.id.chatNewIV);
            notifyView = (ImageView) menuView1.findViewById(R.id.notifyNewIV);
            chatLayout = (LinearLayout) menuView1.findViewById(R.id.chatLayout);
            callLayout = (LinearLayout) menuView1.findViewById(R.id.callLayout);
            notifyLayout = (LinearLayout) menuView1.findViewById(R.id.notifyLayout);
            recorrectLayout = (LinearLayout) menuView1.findViewById(R.id.recorrectLayout);
            recorrectLayout.setVisibility(View.GONE);
            historyLayout = (LinearLayout) menuView1.findViewById(R.id.historyLayout);
            // 创建一个PopuWidow对象
            menuWindow1 = new PopupWindow(menuView1, (windowManager.getDefaultDisplay().getWidth() * 2) / 5, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 使其聚集
        menuWindow1.setFocusable(true);
        // 设置允许在外点击消失
        menuWindow1.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        menuWindow1.setBackgroundDrawable(new BitmapDrawable());
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        int xPos = windowManager.getDefaultDisplay().getWidth() / 2
                - menuWindow1.getWidth() / 2;
        menuWindow1.showAsDropDown(parent, 0, 0);

        chatLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEntity != null) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("chat", mEntity);
                    getActivity().startActivity(intent);
                }
                if (menuWindow1 != null) {
                    menuWindow1.dismiss();
                }
            }
        });
        callLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEntity != null) { // 紧急呼叫
                    sendCommand(MyConstants.CALL);
                }
                if (menuWindow1 != null) {
                    menuWindow1.dismiss();
                }
            }
        });
        notifyLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WarnActivity.class);
                getActivity().startActivity(intent);
                if (menuWindow1 != null) {
                    menuWindow1.dismiss();
                }
            }
        });
        recorrectLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMarker != null) {
                    initView();
                    mMarker.setDraggable(true);
                    reNotifyLayout.setVisibility(View.VISIBLE);
                }
                if (menuWindow1 != null) {
                    menuWindow1.dismiss();
                }
            }
        });

        /*历史轨迹点击事件处理*/
        //

//        historyLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mDevice != null) {
//                    //显示历史轨迹
//                    showDateDialog();
//                }
//
//                /*关闭弹出的自定义菜单*/
//                if (menuWindow1 != null) {
//                    menuWindow1.dismiss();
//                }
//            }
//        });
    }


    /**
     * 单击地图覆盖物显示详细说明
     */
    private Marker mMarker = null;
    // private TextView popupText;
    // private TextView nameText;
    private TextView addressView, locationView, distanceView, jdView, wdView,
            timeView, dlView;
    private View viewCache;
    private ImageView elecView;
    private boolean isShow = false; // 是否显示分享的对象
    private LinearLayout navLayout, /*shareLayout,*/
            shareDetailLayout, historyTrailLayout,
            weiboLayout, weixinLayout, qzoneLayout, locationLayout;
    private PopupWindow infoWindow;
    private ImageButton closePopWindow;

    /**
     * 显示定位窗体
     *
     * @param marker
     */
    private View getInfoWindowView(final Marker marker) {
        isShow = false;
        LatLng ll = marker.getPosition();
        Projection p1 = aMap.getProjection();
        LatLng llInfo = null;
        if (p1 != null) {
            Point p = aMap.getProjection().toScreenLocation(ll);
            p.y -= 90;
            llInfo = aMap.getProjection().fromScreenLocation(p);
        }

        viewCache = getActivity().getLayoutInflater().inflate(R.layout.customer_map_popwindow, null);
        // popupText = (TextView) viewCache.findViewById(R.id.textcache);
        // nameText = (TextView) viewCache.findViewById(R.id.nameTV);
        elecView = (ImageView) viewCache.findViewById(R.id.popleft1);

        addressView = (TextView) viewCache.findViewById(R.id.nameTV);
        locationView = (TextView) viewCache.findViewById(R.id.locationTV);
        distanceView = (TextView) viewCache.findViewById(R.id.distanceTV);
        jdView = (TextView) viewCache.findViewById(R.id.jdTV);
        wdView = (TextView) viewCache.findViewById(R.id.wdTV);
        timeView = (TextView) viewCache.findViewById(R.id.timeTV);
        dlView = (TextView) viewCache.findViewById(R.id.dlTV);
        navLayout = (LinearLayout) viewCache.findViewById(R.id.navLayout);
        historyTrailLayout = (LinearLayout) viewCache.findViewById(R.id.historyTrailLayout);
//        shareLayout = (LinearLayout) viewCache.findViewById(R.id.shareLayout);
        shareDetailLayout = (LinearLayout) viewCache.findViewById(R.id.shareDetailLayout);
//        weiboLayout = (LinearLayout) viewCache.findViewById(R.id.weiboLayout);
//        weixinLayout = (LinearLayout) viewCache.findViewById(R.id.weixinLayout);
//        qzoneLayout = (LinearLayout) viewCache.findViewById(R.id.qzoneLayout);
        locationLayout = (LinearLayout) viewCache.findViewById(R.id.locationLayout);
        closePopWindow = (ImageButton) viewCache.findViewById(R.id.customer_popwindow_close);

//        weiboLayout.setOnClickListener(this);
//        weixinLayout.setOnClickListener(this);
//        qzoneLayout.setOnClickListener(this);
        closePopWindow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMarker.hideInfoWindow();
            }
        });

        historyTrailLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "点击了历 史轨迹");

                if (mDevice != null) {
                    //显示历史轨迹
                    showDateDialog();
                }

                /*关闭弹出的自定义菜单*/
                if (menuWindow1 != null) {
                    menuWindow1.dismiss();
                }
            }
        });

        navLayout.setOnClickListener(new OnClickListener() { // 导航
            @Override
            public void onClick(View v) {
                // 显示正在定位中，请稍等（定位未成功）
                provider.startLocation(new NotifyCallback() {
                    @Override
                    public void execute(AMapLocation location) {
                        makeText(getActivity(), R.string.enterNav, Toast.LENGTH_LONG).show();
                        launchNavigator2(location.getLatitude(), location.getLongitude(), "", mEntity.getLatitude(), mEntity.getLongitude(), mEntity.getAddress());
                    }
                });
            }
        });

//        provider.startLocation();

        locationLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice.getOnline() != 1) { //设备不在线，就不定位了
                    Toast.makeText(getActivity(), "终端离线", Toast.LENGTH_SHORT).show();
                } else {
                    tracking();
                    realTimeLocation = true;
                }
            }
        });

        addressView.setText(getResources().getString(R.string.marker_location) + (CommUtil.isNotBlank(mEntity.getAddress()) ? mEntity.getAddress() : getString(R.string.querying)));
        locationView.setText(getResources().getString(R.string.marker_location_way) + getLocationWay(mEntity.getPositioning()));
        if(isLatestPosition) {
            distanceView.setText(getResources().getString(R.string.marker_distance) + mEntity.getDistance());
            distanceView.setVisibility(View.VISIBLE);
        }else{
            distanceView.setVisibility(View.INVISIBLE);
        }
        if (String.valueOf(mEntity.getLongitude()).length() > 10) {
            jdView.setText(getResources().getString(R.string.marker_jd) + String.valueOf(mEntity.getLongitude()).substring(0, 10));
        } else {
            jdView.setText(getResources().getString(R.string.marker_jd) + mEntity.getLongitude());
        }
        if (String.valueOf(mEntity.getLatitude()).length() > 10) {
            wdView.setText(getResources().getString(R.string.marker_wd) + String.valueOf(mEntity.getLatitude()).substring(0, 10));
        } else {
            wdView.setText(getResources().getString(R.string.marker_wd) + mEntity.getLatitude());
        }
        timeView.setText(getResources().getString(R.string.marker_time) + Utils.getDayTime(mEntity.getDatetime()));
        showElec(elecView, mEntity.getElectricity());// 展示电量图标
        dlView.setText(Double.valueOf(mEntity.getElectricity()).toString()); // 显示电量

        return viewCache;
    }

    /**
     * 获取设备的定位方式
     *
     * @param position
     * @return
     */
    private String getLocationWay(int position) {
        String temp;
        switch (position) {
            case 1:
                temp = "基站";
                break;
            case 2:
                temp = "卫星";
                break;
            case 3:
                temp = "WIFI";
                break;
            case 4:
                temp = "WIFI和基站";
                break;
            default:
                temp = "未获取到合法定位";
                break;
        }
        return temp;
    }

    /**
     * 显示电量图标
     *
     * @param imageView
     * @param elec
     */
    private void showElec(ImageView imageView, double elec) {
        int percent = Double.valueOf((elec - 3.4) / 0.8).intValue();//最高4.2，最低3.4
        if (percent >= 80) {
            imageView.setImageResource(R.drawable.elec_icon_100);
        } else if (percent >= 60) {
            imageView.setImageResource(R.drawable.elec_icon_80);
        } else if (percent >= 40) {
            imageView.setImageResource(R.drawable.elec_icon_60);
        } else if (percent >= 20) {
            imageView.setImageResource(R.drawable.elec_icon_40);
        } else if (percent >= 0) {
            imageView.setImageResource(R.drawable.elec_icon_20);
        }
    }

    private boolean mIsEngineInitSuccess = false;
//    private BaiduNaviManager.NaviInitListener mNaviEngineInitListener = new BaiduNaviManager.NaviInitListener() {
//        @Override
//        public void onAuthResult(int status, String msg) {
//            String authinfo;
//            if (0 == status) {
//                authinfo = "key校验成功!";
//            } else {
//                authinfo = "key校验失败, " + msg;
//            }
//            Log.e(Constants.TAG,authinfo);
//        }
//
//        @Override
//        public void initStart() {
//
//        }
//
//        @Override
//        public void initSuccess() {
//            mIsEngineInitSuccess=true;
//        }
//
//        @Override
//        public void initFailed() {
//
//        }
//    };

    /**
     * 指定导航起终点启动GPS导航.起终点可为多种类型坐标系的地理坐标。
     * 前置条件：导航引擎初始化成功
     * <p/>
     * 传入的坐标系为gcj02
     */
    private void launchNavigator2(double startLatitude, double startLongitude,
                                  String startName, double endLatitude, double endLongitude,
                                  String endName) {
//        endLatitude=22.6108291806;
//        endLongitude=114.0308176092;

        SharedPreferences preferences = Utils.getPreferences(getActivity());
        boolean useExternalMap = preferences.getBoolean(Constants.SETTING_NAVI_MAP, true);
        if (useExternalMap) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("androidamap://navi?sourceApplication=GpsTracker&poiname=" + endName + "&lat=" + endLatitude + "&lon=" + endLongitude + "&style=0&dev=0"));
                intent.setPackage("com.autonavi.minimap");
                startActivity(intent);
            } catch (Exception e) {
//            CommUtil.showMsgShort("未安装高德地图，正在努力集成高德地图...您可以先下载高德地图再尝试此功能！");
                naviInternal(startLatitude, startLongitude, endLatitude, endLongitude);
            }
        } else {
            naviInternal(startLatitude, startLongitude, endLatitude, endLongitude);
        }


//        if(!mIsEngineInitSuccess){
//            CommUtil.showMsgLong(getString(R.string.navi_not_init));
//            return;
//        }
//        BNRoutePlanNode sNode=new BNRoutePlanNode(startLongitude, startLatitude, startName, null ,BNRoutePlanNode.CoordinateType.GCJ02);
//        BNRoutePlanNode eNode=new BNRoutePlanNode(endLongitude, endLatitude, endName, null ,BNRoutePlanNode.CoordinateType.BD09_MC);
//
//        List<BNRoutePlanNode> nodes=new ArrayList<>(2);
//        nodes.add(sNode);
//        nodes.add(eNode);
//        BaiduNaviManager.getInstance().launchNavigator(getActivity(), nodes,
//                NE_RoutePlan_Mode.ROUTE_PLAN_MOD_RECOMMEND, // 算路方式
//                                                            // static int ROUTE_PLAN_MOD_AVOID_TAFFICJAM躲避拥堵
//                                                            // static int ROUTE_PLAN_MOD_MIN_DIST最短距离
//                                                            // static int ROUTE_PLAN_MOD_MIN_TIME最短时间
//                                                            // static int ROUTE_PLAN_MOD_MIN_TOLL 最少收费
//                                                            // static int ROUTE_PLAN_MOD_RECOMMEND 推荐
//                true, // GPS导航
//                new BaiduNaviManager.RoutePlanListener() {
//                    @Override
//                    public void onJumpToNavigator() {
//                        Intent intent = new Intent(getActivity(), BNavigatorActivity.class);
//                        startActivity(intent);
//                    }
//
//                    @Override
//                    public void onRoutePlanFailed() {
//                        CommUtil.showMsgLong(getString(R.string.navi_failed));
//                    }
//                });
    }

    private void naviInternal(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        Intent intent = new Intent(getActivity(), NavigatorEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("startLatitude", startLatitude);
        intent.putExtra("startLongitude", startLongitude);
        intent.putExtra("endLatitude", endLatitude);
        intent.putExtra("endLongitude", endLongitude);
        startActivity(intent);
        CommUtil.showProcessing(getActivity().getWindow().getDecorView(), true, false);
    }


    /**
     * 获取所有的设备
     */
    private void getDevice() {
        Device.getList(getActivity(), new BctClientCallback() {
            @Override
            public void onStart() {
//                WizardAlertDialog.getInstance().showProgressDialog(R.string.get_device_data, AppContext.getContext());
            }

            @Override
            public void onFinish() {
//                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    deviceList.clear();
                    for (int i = 0; i < obj.getBodyArray().length(); i++) {
                        Device device = new Device(JSONHelper.getJSONObject(obj.getBodyArray(), i));
                        deviceList.add(device);
                    }

                    showDevice();
                } else {
                    makeText(getActivity(), obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                if (CommUtil.isNotBlank(message)) {
                    makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDevice() {
        //如果没有device，则要求去添加
        if (deviceList.size() == 0) {
            AddDeviceDialog.getInstance().setOnclickNegativeButton(new AddDeviceDialog.OnClickNegativeButton() {
                @Override
                public void clickNegativeButton() {
                    //((MainActivity) getActivity()).logout();
//                    Intent intent=new Intent(getActivity(), AccountActivity.class);
//                    startActivity(intent);
                }
            });
            AddDeviceDialog.getInstance().showDialog(getActivity(), false);
        }


        if (deviceList.size() > 0 && mDevice == null) {
            mDevice = deviceList.get(0);
            Session.getInstance().setDevice(mDevice);
            if (CommUtil.isNotBlank(mDevice.getPortrait())) {
                ImageLoader.getInstance().displayImage(mDevice.getPortrait(), photoView, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (mDevice.getOnline() != 1) {
                            photoView.setImageBitmap(Utils.toGrayscale(loadedImage));
                        }
                    }
                });
                nameView.setText(mDevice.getName());
            } else {
                nameView.setText(mDevice.getName());
                photoView.setImageResource(R.drawable.user_no_photo);
                //根据是否离线，显示灰色图片
                if (mDevice.getOnline() != 1) { //0为不在线
                    Utils.setGrayImageView(photoView);
                }
            }
            getBabyData1();
        }
    }

    private static class FragmentHandler extends Handler {
        private BabyFragment fragment;

        public FragmentHandler(WeakReference<BabyFragment> fragment) {
            this.fragment = fragment.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String imei = (String) (msg.obj);
                    //getBabyData(imei);
                    break;
                case 1:
                    fragment.successLayout.setVisibility(View.GONE);
                    break;
                case 2:
//                    RemoteCloseAndClearDialog.getInstance().closeDialog();
                    break;
                case 3:
                    fragment.updateTermInfo();
                    break;
                case 4:
                    if (isTicking) {
                        isTicking = false;
                        CommUtil.hideProcessing();
                        if (retryTimes < 0) {//2015-10-17取消重试功能
                            fragment.tracking();
                            CommUtil.showMsgShort(String.format(fragment.getString(R.string.retrying), ++retryTimes));
                        } else {
                            retryTimes = 0;
                            CommUtil.showMsgShort(AppContext.getContext().getString(R.string.unReceivedLatestLocation));
                        }
                    }
                    break;
            }
        }
    }

    private Handler handler = new FragmentHandler(new WeakReference<>(this));
    private static Boolean isTicking = false;
    private static int retryTimes = 0;

    private void tracking() {
        if (isTicking) {
            CommUtil.sendMsg(CommHandler.TOAST_SHORT, getString(R.string.isTicking));
            return;
        } else {
            isTicking = true;
            isLatestPosition=false;
        }
        sendCommand(MyConstants.LOCATION, false);
        ((MainActivity)getActivity()).sendHeartBeat();
        CommUtil.showProcessing(mMapView, true, false);
        handler.sendEmptyMessageDelayed(4, 60000);
        provider.startLocation(new NotifyCallback() {
            @Override
            public void execute(AMapLocation location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Session.getInstance().setLatLng(latLng);
                isLatestPosition=true;
            }
        });
    }

    /**
     * 请求宝宝的定位数据方法
     */
    private void getBabyData1() {
        if (mDevice == null) return;
        mDevice.getTrack(getActivity(), new BctClientCallback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    JSONObject jsonObj = JSONHelper.getJSONObject(obj.getBodyArray(), 0);
                    mEntity = new MapEntity(jsonObj);
                    if (mEntity.getImei() == null) {
                        mEntity.setImei(mDevice.getImei());
                    }
                    if (mEntity.getName() == null) {
                        mEntity.setName(mDevice.getName());
                    }
                    updateTermInfo();
                } else {
                    String info = obj.getMsg();
                    if (CommUtil.isBlank(info)) {
                        info = getString(R.string.track_failed);
                    }
                    makeText(getActivity(), info + " ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                makeText(getActivity(), R.string.track_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 同步获取接口信息
     * 接收服务器主动通知的消息后去服务器获取最新信息
     */
    @Subscriber(tag = Constants.EVENT_TAG_POSI_NOTIFY, mode = ThreadMode.ASYNC)
    private void getBabySync(String res) {
        if (CommUtil.isBlank(res)) {
            res = mDevice.getTrackSync();
        }
        if (res != null) {
            JSONObject json = null;
            try {
                json = new JSONObject(res);
            } catch (JSONException e) {
                //
            }
            ResponseData respData = new ResponseData(json);
            if (respData.getRetcode() != 1) {
                return;
            }
            JSONObject jsonObj = JSONHelper.getJSONObject(respData.getBodyArray(), 0);
            String targetImei = jsonObj != null ? jsonObj.optString("imei") : null;
            if (CommUtil.isNotBlank(targetImei) && !targetImei.equals(mDevice.getImei())) {
                return;
            }
            if (historyRL.getVisibility() == View.VISIBLE) {
                //历史轨迹在显示时，直接忽略掉最新定位
                return;
            }
            CommUtil.hideProcessing();
            mEntity = new MapEntity(jsonObj);

            handler.sendEmptyMessage(3);
            handler.removeMessages(4);
//            CommUtil.showMsgShort(getString(R.string.receivedLatestLocation));
        }
        isTicking = false;
    }

    private void updateTermInfo() {
        if (Session.getInstance().getLatLng() != null) {
            LatLng latLng = getReceivedLatLng();
            updateInfo(latLng);
        }
        initView();
    }

    /**
     * 获取监护人，并将获取数据存入缓存中
     */
    private void getMoniter() {
        Keeper.getList(getActivity(), new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(getActivity().getString(R.string.drop_down_list_header_loading_text), getActivity());
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                WizardAlertDialog.getInstance().closeProgressDialog();
//                Session.getInstance().getDevices().clear();

                if (obj.getRetcode() == 1) {//如果成功返回数据，那么就重新设置缓存
                    //getMonitor----------
                    Session.getInstance().getMonitors().clear();
                    //getUser-------------------
                    Session.getInstance().getUserList().clear();
                    //getFriends-----------
                    Session.getInstance().getFriendList().clear();
                    TermFriend friend = null;
                    Device deviceMonitor = null;
                    Keeper keeper = null;
                    Device device = null;
                    //getDevice---------------
                    try {
                        //getMonitor----------
                        JSONObject jobj = obj.getBody();
                        if (jobj == null) {
                            return;
                        }
                        JSONArray arrMonitor = obj.getBody().getJSONArray("monitorObjects");
                        if (arrMonitor != null && arrMonitor.length() > 0) {
                            deviceList.clear();
                            for (int i = 0; i < obj.getBody().getJSONArray("monitorObjects").length(); i++) {
                                Log.i(TAG, "monitorObjects:" + obj.getBody().getJSONArray("monitorObjects").length());
                                deviceMonitor = new Device(JSONHelper.getJSONObject(obj.getBody().getJSONArray("monitorObjects"), i));
                                deviceMonitor.setAuthDiscrible(getString(R.string.rel_guardian_obj));
                                deviceMonitor.setSort(Constants.SORT_MONITOR_OBJECT);
                                Session.getInstance().getMonitors().add(deviceMonitor);
                                if(deviceMonitor.isBinded()){
                                    deviceList.add(deviceMonitor);
                                }
                            }
                        }
                        //getUser-------------------
                        JSONArray arrKeeper = obj.getBody().getJSONArray("keeper");
                        if (arrKeeper != null && arrKeeper.length() > 0) {
                            for (int i = 0; i < obj.getBody().getJSONArray("keeper").length(); i++) {
                                Log.i(TAG, "keeper:" + obj.getBody().getJSONArray("keeper").length());
                                keeper = new Keeper(JSONHelper.getJSONObject(obj.getBody().getJSONArray("keeper"), i));
                                if ("1".equals(keeper.getAppUserNum())) {
                                    keeper.setSort(Constants.SORT_MANAGER);
                                    keeper.setAuthDiscrible(getString(R.string.rel_manager));
                                } else {
                                    keeper.setSort(Constants.SORT_KEEPER);
                                    keeper.setAuthDiscrible(getString(R.string.rel_guardian));
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
                                Session.getInstance().getFriendList().add(friend);
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
                        showDevice();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.get_moniter_err, Toast.LENGTH_SHORT).show();
                }

                ContactsUtil.getInstance(getActivity()).insertOrUpdataAllContacts();


            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 发送指令
     *
     * @param type 指令的类型
     */
    private void sendCommand(final String type) {
        sendCommand(type, true);
    }

    private void sendCommand(final String type, final boolean showMsg) {
        String cmd = "";
        String content = "";
        if (type.equals(MyConstants.LOCATION)) {
            cmd = "tk";
            content = "99";//原先值为0，与后台协商，采用socket主动通知结果以后，上传此值以区别是页面端还是APP端，后台会给终端发原值0
        } else if (type.equals(MyConstants.CALL)) {
            cmd = "hj";
            content = "1";
        } else if (type.equals(MyConstants.CLEAR_NOTIFY)) {
            cmd = "ns";
            content = "1";
        }
        CommService.get().sendCommand(getActivity(), mDevice.getImei(), cmd, content, new BctClientCallback() {
            @Override
            public void onStart() {
                if (showMsg) {
                    CommUtil.showProcessing(mMapView, true, false);
                }
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (showMsg) {
                    CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
                    CommUtil.sendMsg(CommHandler.TOAST_SHORT, obj.getMsg());
                }
            }

            @Override
            public void onFailure(String message) {
                if (showMsg) {
                    CommUtil.sendMsg(CommHandler.TOAST_SHORT, message);
                }
            }
        });

    }

    private LatLng getReceivedLatLng() {
        LatLng latLng = null;
        if (mEntity != null && mEntity.getLatitude() != 0.0) {
            latLng = new LatLng(mEntity.getLatitude(), mEntity.getLongitude());
        }
        return latLng;
    }

    private void updateInfo(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        double distance = AMapUtils.calculateLineDistance(latLng, Session.getInstance().getLatLng());
        if (distance > 1000) {
            mEntity.setDistance((int) (distance / 1000) + "KM");
        } else {
            mEntity.setDistance((int) distance + "M");
        }
    }

    /**
     * APP定位信息上传
     */
    private void recorrect(LatLng newLatLng) {
        try {
            LatLng gpsLatLng = newLatLng;
            final JSONObject object = new JSONObject();
            object.put("gpsLng", mEntity.getLongitude());
            object.put("gpsLat", mEntity.getLatitude());
            object.put("rgpsLng", gpsLatLng.longitude);
            object.put("rgpsLat", gpsLatLng.latitude);
            object.put("rbaiduLng", newLatLng.longitude);
            object.put("rbaiduLat", newLatLng.latitude);
            Device.reCorrect(this.getActivity(), object, new BctClientCallback() {
                @Override
                public void onStart() {
                    WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, getActivity());
                }

                @Override
                public void onFinish() {
                    WizardAlertDialog.getInstance().closeProgressDialog();
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        reNotifyLayout.setVisibility(View.GONE);
                        successLayout.setVisibility(View.VISIBLE);
                        handler.sendEmptyMessageDelayed(1, 1500);
                    } else {
                        makeText(getActivity(), obj.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            makeText(getActivity(), "错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取历史轨迹
     */
    private List<HistoryTrack> trackList = new ArrayList<HistoryTrack>();

    /**
     * 从服务器获取当天的历史轨迹
     *
     * @param date
     */
    private void getHistoryTrack(final String date) {
        if (mDevice == null) return;
        mDevice.getHistoryTrackList(this.getActivity(), date, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(mMapView, true, false);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
                if (obj.getRetcode() == 1) {
                    //显示停止按钮
                    historyRL.setVisibility(View.VISIBLE);
                    JSONArray coordinates = JSONHelper.getJSONArray(obj.getBody(), "coordinates");

                    Log.d(TAG, "要取出的时间为：" + date);

                    for (int i = 0, count = coordinates.length(); i < count; i++) {
                        HistoryTrack track = new HistoryTrack(JSONHelper.getJSONObject(coordinates, i));
                        if (date.equals(track.getDateTime().substring(0, 10)))
                            trackList.add(track);
                    }
                    showHistoryTrack();
                } else {
                    makeText(getActivity(), obj.getMsg(), Toast.LENGTH_SHORT).show();
                    //隐藏停止按钮
                    historyRL.setVisibility(View.GONE);
                    //获取不到轨迹时，应该停止之前的画图
                    closeDataDialog();
                }

            }

            @Override
            public void onFailure(String message) {
                CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
            }
        });
    }

    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
    SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();

    /**
     * 关闭历史轨迹
     */
    private void closeDataDialog() {
        stopPointTimer();
        getBabyData1();
    }

    /**
     * 显示历史轨迹
     */
    private void showDateDialog() {
        df.applyPattern("yyyy-MM-dd");
        Date date = new Date();
        mCalendar.setTime(date);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        CommDatePickerDialog datePickerDialog = new CommDatePickerDialog(
                this.getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String str = year + "-";
                        if (monthOfYear + 1 < 10) {
                            str += "0";
                        }
                        str += monthOfYear + 1 + "-";
                        if (dayOfMonth < 10) str += "0";
                        str += dayOfMonth;
                        if (aMap != null) {
                            aMap.clear();
                        }
                        getHistoryTrack(str);
                    }

                }, year, month, day);
        datePickerDialog.show();
    }


    private class CommDatePickerDialog extends DatePickerDialog {

        public CommDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        @Override
        protected void onStop() {
            //注释掉，以避免两次触发日期选择后的动作
            //super.onStop();
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private int seekbarIndex;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

//              historyHourTV.setText(CommUtil.getCheckTimeBySeconds(progress));
            historyTime = seekBar.getProgress();

            textTime.layout((int) (progress * moveStep), 20, screenWidth, 80);
            textTime.setText(CommUtil.getCheckTimeBySeconds(progress));
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isShowPointPauseSeekbar = true;
            seekbarIndex = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//                historyHourTV.setVisibility(View.INVISIBLE);

            checkThePoint();

            if (seekbarIndex != seekBar.getProgress()) {
                aMap.clear(); //地图清空
                points.clear();
                ooPolyline = null;
                indexPoint = 1;
//                    isSetCamera = false;
            }
            isShowPointPauseSeekbar = false;
        }
    };


    @Override
    public boolean onBackPressed() {
        if (realTimeLocation) {
            //清除定位进程
            isTicking = false;
            //清除dialog
            CommUtil.sendMsg(CommHandler.CLOSE_DIALOG);
            realTimeLocation = false;
            return false;
        } else {
            return true;
        }
    }

    /**
     * 更新设备在线状态
     * @param msg
     */
    @Subscriber(tag = Constants.EVENT_TAG_TERM_STATUS)
    private void updateTermStatus(Msg msg) {
        if (msg == null || msg.getFrom() == null) {
            return;
        }
        String imei = msg.getFrom();
        int status = ByteUtil.byteArrayToInt(msg.getData());
        for (Device device : deviceList) {
            if (imei.equals(device.getImei())) {
                device.setOnline(status);
                break;
            }
        }
        if(groupAdapter != null) {
            groupAdapter.notifyDataSetChanged();
        }
        updateMainDevice();
    }

    @Override
    public void update(Observable observable, Object data) {
        try {
            deviceList.clear();
            List<Device> devices=Session.getInstance().getMonitors();
            for(Device dv:devices){
                if(dv.isBinded()){
                    deviceList.add(dv);
                }
            }
            if(groupAdapter != null) {
                groupAdapter.notifyDataSetChanged();
            }
            updateMainDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
