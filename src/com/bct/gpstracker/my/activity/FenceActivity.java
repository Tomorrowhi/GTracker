package com.bct.gpstracker.my.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.*;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.common.LocationProvider;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.inter.NotifyCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.FenceEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.WeekDayTimes;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.Session;

/**
 * 电子围栏
 *
 * @author huangfei
 */
public class FenceActivity extends BaseActivity implements OnClickListener,AMap.OnMapClickListener {

    public static int SEND_TIME_REQUEST = 1;

    private ImageButton backButton;
    private Button fenceSetupButton;
    private MapView mapView;
//    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding_temp1);

    BitmapDescriptor bdA;
    private LinearLayout choiceRadiusLayout, choiceTimeLayout, setupFenceNameArea;//选择半径，选择时间
    private RelativeLayout fenceSeekBarRLayout;
    private TextView notifyView;    //页头提示
    private TextView fenceRadiusTV;    //页头提示
    private TextView setupFenceName;    //页头提示
    private AMap aMap;
    private WeekDayTimes mDayTimes = null;
    private Marker mMarker;
    private List<FenceEntity> entityList = new ArrayList<FenceEntity>();
    private FenceEntity fence;
    private SeekBar fenceSeekBar;
    private LatLng mLatLng;
    private int radius = 300;   //半径
    private boolean fenceList;
    private AlertDialog alertDialog;
    private Button fenceHome, fenceGrandmotherHome, fenceGrandmaHome,
            fenceSchool, fenceCramSchool, fenceTeacherHome, cancelSetupFenceName, okSetupFenceName;
    private EditText fencenNameEt;
    private View radiusTextView;    //自定义半径文字布局
    private TextView radiusTV;   //半径显示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);



        TextView titleTV = (TextView) findViewById(R.id.titleNameTV);
        titleTV.setText(getString(R.string.fence_title) + "-" + Session.getInstance().getSetupDevice().getName());

        fence = Session.getInstance().getSetupfence();

        fenceList = getIntent().getBooleanExtra("fencelist", false);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        mapView = (MapView) findViewById(R.id.map);
        notifyView = (TextView) findViewById(R.id.notifyView);
        choiceRadiusLayout = (LinearLayout) findViewById(R.id.choiceRadiusLayout);
        choiceTimeLayout = (LinearLayout) findViewById(R.id.choiceTimeLayout);
        fenceSeekBarRLayout = (RelativeLayout) findViewById(R.id.fence_seekbar_r_layout);
        fenceSeekBar = (SeekBar) findViewById(R.id.fence_seekbar);
        fenceRadiusTV = (TextView) findViewById(R.id.fence_text_view_radius);
        setupFenceNameArea = (LinearLayout) findViewById(R.id.setup_fence_name_area);
        setupFenceName = (TextView) findViewById(R.id.setup_fence_name);
        fenceSetupButton = (Button) findViewById(R.id.fenceSetupBtn);

        backButton.setOnClickListener(clickListener);
        notifyView.setOnClickListener(clickListener);
        choiceRadiusLayout.setOnClickListener(clickListener);
        choiceTimeLayout.setOnClickListener(clickListener);
        fenceSetupButton.setOnClickListener(clickListener);
        setupFenceNameArea.setOnClickListener(clickListener);

        mLatLng = Session.getInstance().getLatLng();

        mapView.onCreate(savedInstanceState);// 必须要写
        aMap = mapView.getMap();
        aMap.setOnMapClickListener(this);

        fenceSeekBarRLayout.setVisibility(View.VISIBLE);
        //createCircle(mLatLng, radius);

        fenceSeekBar.setProgress(0);
        //滑动条的滑动事件处理
        fenceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    radius = 300;
                } else {
                    radius = 300 + progress * 100;
                }
                createCircle(mLatLng, radius);
//                fenceRadiusTV.setText("半径：" + radius + "米");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        initView();
    }

    public void createCircle(LatLng latLng, int radius) {
        aMap.clear();
        View fenceLocationView = LayoutInflater.from(FenceActivity.this).inflate(R.layout.fence_location, null);
        MarkerOptions ooA = new MarkerOptions().anchor(0.5f, 0.5f).position(latLng).icon(BitmapDescriptorFactory.fromView(fenceLocationView)).zIndex(26).draggable(false);
        mMarker = aMap.addMarker(ooA);
        // 添加圆
        CircleOptions ooCircle = new CircleOptions().center(latLng).strokeColor(0x8000BFFF).strokeWidth(1).fillColor(0x2000BFFF).zIndex(18).radius(radius);
        // 绘制文字
//        OverlayOptions textOption = new TextOptions().bgColor(R.layout.shape_test).fontSize(DensityUtil.dip2px(this, 15)).fontColor(0x992F9BFF).text("直径：" + radius * 2 + "米").position(new LatLng(latLng.latitude + 0.001, latLng.longitude));
//        aMap.addOverlay(textOption);
        //设置显示半径
        mapTextViewBitMap(radius);
        //自定义覆盖物
        MarkerOptions textMarker = new MarkerOptions().anchor(0.5f, 0.5f).position(latLng)
                .icon(BitmapDescriptorFactory.fromView(radiusTextView)).zIndex(27);
        aMap.addCircle(ooCircle);
        aMap.addMarker(textMarker);
    }

    /**
     * 变更视图
     */
    private void initView() {
        if (this.fence != null) {
            aMap.clear();
            FenceEntity.LatLngMix latLngMix = this.fence.getLatLngMixes().get(0);
            mLatLng = latLngMix.getLatLng();
            View fenceLocationView = LayoutInflater.from(FenceActivity.this).inflate(R.layout.fence_location, null);
            MarkerOptions ooA = new MarkerOptions().anchor(0.5f, 0.5f).position(mLatLng).icon(BitmapDescriptorFactory.fromView(fenceLocationView)).zIndex(26).draggable(false);
            mMarker = aMap.addMarker(ooA);
            radius = (int) latLngMix.getRadius();
            //改变界面滑块
            fenceSeekBar.setProgress(radius/100-3);
            // 绘制圆
            CircleOptions ooCircle = new CircleOptions().center(mLatLng).strokeColor(0x8000BFFF).fillColor(0x2000BFFF).strokeWidth(1).zIndex(18).radius(radius);
            // 绘制文字
//            OverlayOptions textOption = new TextOptions().bgColor(R.drawable.shape_fence_location_blue_backound).fontSize(DensityUtil.dip2px(this, 15)).fontColor(0x99FFFFFF).text("直径：" + radius*2 + "米").position(new LatLng(mLatLng.latitude + 0.001, mLatLng.longitude));
            //OverlayOptions textOption = new TextOptions().bgColor(0x00000000).fontSize(DensityUtil.dip2px(this, 15)).fontColor(0x992F9BFF).text("直径：" + radius * 2 + "米").position(new LatLng(mLatLng.latitude + 0.001, mLatLng.longitude));
            //aMap.addOverlay(textOption);
            //设置显示半径
            mapTextViewBitMap(radius);
            //自定义覆盖物
            MarkerOptions textMarker = new MarkerOptions().anchor(0.5f, 0.5f).position(mLatLng)
                    .icon(BitmapDescriptorFactory.fromView(radiusTextView)).zIndex(27);
            aMap.addMarker(textMarker);
            aMap.addCircle(ooCircle);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, Constants.MAP_LEVEL));
            //设置当前安全区域的名字
            setupFenceName.setText(fence.getAreaName());
        }else{
            LocationProvider provider=LocationProvider.getInstance(this);
            provider.startLocation(new NotifyCallback() {
                @Override
                public void execute(AMapLocation location) {
                    LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, Constants.MAP_LEVEL));
                    createCircle(ll,radius);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.setup_fence_name_area:
                    //设置名字区域
                    alertDialog = new AlertDialog.Builder(FenceActivity.this).create();
                    View dialogView = LayoutInflater.from(FenceActivity.this).inflate(R.layout.fence_setup_name, null);
                    //设置围栏名字
                    viewSetName(dialogView);
                    alertDialog.setView(dialogView);
                    alertDialog.show();
                    break;
                case R.id.backBtn:
                    FenceActivity.this.finish();
                    break;
                case R.id.notifyView:
                    break;
                case R.id.choiceRadiusLayout:
                    //System.out.println("选择半径");
                    break;
                case R.id.choiceTimeLayout:
                    //System.out.println("选择时间");
                    Intent intent = new Intent(FenceActivity.this, ChoiceTimeActivity.class);
                    intent.putExtra("time", mDayTimes);
                    FenceActivity.this.startActivityForResult(intent, SEND_TIME_REQUEST);
//				FenceActivity.this.startActivity(intent);
                    break;
                case R.id.fenceSetupBtn:

                    String name = setupFenceName.getText().toString();
                    if ("安全区域名称".equals(name)) {
                        Toast.makeText(FenceActivity.this, R.string.fence_center_name, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (mMarker == null) {
                        Toast.makeText(FenceActivity.this, R.string.fence_center_err, Toast.LENGTH_SHORT).show();
                    } else {
//                        Intent setupIntent = new Intent(FenceActivity.this, FenceSetupActivity.class);
//                        setupIntent.putExtra("longitude", mMarker.getPosition().longitude);
//                        setupIntent.putExtra("latitude", mMarker.getPosition().latitude);
//                        setupIntent.putExtra("radius", radius);
//                        if (fenceList) {
//                            //前一个页面FenceListActivity已经关闭
//                            setupIntent.putExtra("flagFenceList", true);
//                        }
//                        startActivityForResult(setupIntent, Constants.NULL_VALUE);

                        setFenceName(name, mMarker.getPosition().longitude, mMarker.getPosition().latitude, radius);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_TIME_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                mDayTimes = (WeekDayTimes) bundle.getSerializable("time");
            } else if (resultCode == RESULT_CANCELED) {
                Bundle bundle = data.getExtras();
                mDayTimes = (WeekDayTimes) bundle.getSerializable("time");
            }
        } else if (requestCode == Constants.NULL_VALUE && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 在弹出的dialog中设置围栏名字
     *
     * @param dialogView
     */
    private void viewSetName(View dialogView) {
        fenceHome = (Button) dialogView.findViewById(R.id.fence_home);
        fenceGrandmotherHome = (Button) dialogView.findViewById(R.id.fence_grandmother_home);
        fenceGrandmaHome = (Button) dialogView.findViewById(R.id.fence_grandma_home);
        fenceSchool = (Button) dialogView.findViewById(R.id.fence_school);
        fenceCramSchool = (Button) dialogView.findViewById(R.id.fence_cram_school);
        fenceTeacherHome = (Button) dialogView.findViewById(R.id.fence_teacher_home);

        cancelSetupFenceName = (Button) dialogView.findViewById(R.id.cancel_setup_fence_name);
        okSetupFenceName = (Button) dialogView.findViewById(R.id.ok_setup_fence_name);

        fencenNameEt = (EditText) dialogView.findViewById(R.id.fencen_name_et);

        fenceHome.setOnClickListener(this);
        fenceGrandmotherHome.setOnClickListener(this);
        fenceGrandmaHome.setOnClickListener(this);
        fenceSchool.setOnClickListener(this);
        fenceCramSchool.setOnClickListener(this);
        fenceTeacherHome.setOnClickListener(this);
        cancelSetupFenceName.setOnClickListener(this);
        okSetupFenceName.setOnClickListener(this);
        fencenNameEt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_setup_fence_name:
                //确定
                String trim = fencenNameEt.getText().toString();
                if (TextUtils.isEmpty(trim)) {
                    Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show();
                    break;
                }
                setupFenceName.setText(trim);
                alertDialog.dismiss();
                break;
            case R.id.cancel_setup_fence_name:
                //取消
                alertDialog.dismiss();
                break;
            case R.id.fence_school:
                fencenNameEt.setText(R.string.fence_school);
                break;
            case R.id.fence_cram_school:
                fencenNameEt.setText(R.string.fence_cram_school);
                break;
            case R.id.fence_teacher_home:
                fencenNameEt.setText(R.string.fence_teacher_home);
                break;
            case R.id.fence_home:
                fencenNameEt.setText(R.string.fence_home);
                break;
            case R.id.fence_grandmother_home:
                fencenNameEt.setText(R.string.fence_grandmother_home);
                break;
            case R.id.fence_grandma_home:
                fencenNameEt.setText(R.string.fence_grandma_home);
                break;
        }
    }

    /**
     * 设置电子围栏区域
     *
     * @param name
     * @param longitude
     * @param latitude
     * @param radius
     */
    public void setFenceName(String name, double longitude, double latitude, int radius) {
        JSONObject object = new JSONObject();
        try {
            if (fence == null) {
                fence = new FenceEntity();
            }
            object.put("id", fence.getId());
            object.put("areaType", "1");
            object.put("areaName", name);
            object.put("startTime", "");
            object.put("endTime", "");
            object.put("weekDays", "");

            JSONArray array = new JSONArray();
            JSONObject coordinates1 = new JSONObject();
            coordinates1.put("longitude", longitude);
            coordinates1.put("latitude", latitude);
            coordinates1.put("radius", radius);
            array.put(coordinates1);

            object.put("coordinates", array);
            object.put("deviceId", Session.getInstance().getSetupDevice().getId());
            addFence(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 添加设备
     *
     * @param json
     */
    private void addFence(final JSONObject json) {
        Device.addFence(FenceActivity.this, json, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, FenceActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    setResult(RESULT_OK);
                    Toast.makeText(FenceActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FenceActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                if(CommUtil.isNotBlank(message)) {
                    Toast.makeText(FenceActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void mapTextViewBitMap(int radius) {
        radiusTextView = LayoutInflater.from(this).inflate(R.layout.shape_radius_textview, null);
        radiusTV = (TextView) radiusTextView.findViewById(R.id.fence_radius_view_tv);
        radiusTV.setText("半径：" + radius + "米");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mLatLng = latLng;
        createCircle(latLng, radius);
    }
}
