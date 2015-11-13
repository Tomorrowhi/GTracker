package com.bct.gpstracker.my.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.FenceEntity;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CheckTextView;
import com.bct.gpstracker.vo.Session;

/**
 * 设置电子围栏详情页
 *
 * @author huangfei
 */
public class FenceSetupActivity extends BaseActivity implements OnClickListener {

    private ImageButton backButton;
    private Button confirmButton;
    String[] radiusStrings = new String[]{"300", "400", "500", "600", "700", "800", "900", "1000"};
    private CheckTextView checkTV1, checkTV2, checkTV3, checkTV4, checkTV5, checkTV6, checkTV7;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private TextView disTV, startTimeTV, endTimeTV;
    private int radius;
    private FenceEntity fence;
    private EditText nameET;
    private LinearLayout fenceWaitSelect;
    private Button fenceHome;
    private Button fenceGrandmotherHome;
    private Button fenceGrandmaHome;
    private Button fenceSchool;
    private Button fenceCramSchool;
    private Button fenceTeacherHome;
    private boolean flagFenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_setup);

        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        radius = getIntent().getIntExtra("radius", 300);
        flagFenceList = getIntent().getBooleanExtra("flagFenceList", false);
        //System.out.println("------"+latitude+";"+longitude);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        confirmButton = (Button) findViewById(R.id.button1);
        checkTV1 = (CheckTextView) findViewById(R.id.checkTV1);
        checkTV2 = (CheckTextView) findViewById(R.id.checkTV2);
        checkTV3 = (CheckTextView) findViewById(R.id.checkTV3);
        checkTV4 = (CheckTextView) findViewById(R.id.checkTV4);
        checkTV5 = (CheckTextView) findViewById(R.id.checkTV5);
        checkTV6 = (CheckTextView) findViewById(R.id.checkTV6);
        checkTV7 = (CheckTextView) findViewById(R.id.checkTV7);
        backButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        checkTV1.setOnClickListener(this);
        checkTV2.setOnClickListener(this);
        checkTV3.setOnClickListener(this);
        checkTV4.setOnClickListener(this);
        checkTV5.setOnClickListener(this);
        checkTV6.setOnClickListener(this);
        checkTV7.setOnClickListener(this);

        disTV = (TextView) findViewById(R.id.disTV);
        startTimeTV = (TextView) findViewById(R.id.startTimeTV);
        endTimeTV = (TextView) findViewById(R.id.endTimeTV);
        nameET = (EditText) findViewById(R.id.nameET);

        fenceWaitSelect = (LinearLayout) findViewById(R.id.fence_wait_select);
        fenceHome = (Button) findViewById(R.id.fence_home);
        fenceGrandmotherHome = (Button) findViewById(R.id.fence_grandmother_home);
        fenceGrandmaHome = (Button) findViewById(R.id.fence_grandma_home);
        fenceSchool = (Button) findViewById(R.id.fence_school);
        fenceCramSchool = (Button) findViewById(R.id.fence_cram_school);
        fenceTeacherHome = (Button) findViewById(R.id.fence_teacher_home);

        disTV.setText(radius + "");


        fenceSchool.setOnClickListener(this);
        fenceCramSchool.setOnClickListener(this);
        fenceTeacherHome.setOnClickListener(this);
        fenceHome.setOnClickListener(this);
        fenceGrandmotherHome.setOnClickListener(this);
        fenceGrandmaHome.setOnClickListener(this);
        disTV.setOnClickListener(this);
        startTimeTV.setOnClickListener(this);
        endTimeTV.setOnClickListener(this);

        if (Session.getInstance().getSetupfence() != null) {
            fence = Session.getInstance().getSetupfence();
        } else {
            fence = new FenceEntity();
        }
        startTimeTV.setText(fence.getStartTime());
        endTimeTV.setText(fence.getEndTime());
        nameET.setText(fence.getAreaName());
        //日期
        String[] str = fence.getWeekDays().split(",");
        for (String s : str) {
            if (s.equals("1")) {
                checkTV1.setChecked(true);
            } else if (s.equals("2")) {
                checkTV2.setChecked(true);
            } else if (s.equals("3")) {
                checkTV3.setChecked(true);
            } else if (s.equals("4")) {
                checkTV4.setChecked(true);
            } else if (s.equals("5")) {
                checkTV5.setChecked(true);
            } else if (s.equals("6")) {
                checkTV6.setChecked(true);
            } else if (s.equals("7")) {
                checkTV7.setChecked(true);
            }
        }
        initEvevt();

    }

    private void initEvevt() {

        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (nameET.getText().length() > 0) {
                    fenceWaitSelect.setVisibility(View.GONE);
                } else {
                    fenceWaitSelect.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 添加设备
     *
     * @param json
     */
    private void addFence(final JSONObject json) {
        Device.addFence(FenceSetupActivity.this, json, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, FenceSetupActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    setResult(RESULT_OK);
                    Toast.makeText(FenceSetupActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FenceSetupActivity.this, FenceListActivity.class);
                    if (!flagFenceList) {
                        //如果FenceActivity页面已经关闭，那么不传递参数
                        intent.putExtra("flag", "setup");
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(FenceSetupActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(FenceSetupActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fence_school:
                nameET.setText(R.string.fence_school);
                break;
            case R.id.fence_cram_school:
                nameET.setText(R.string.fence_cram_school);
                break;
            case R.id.fence_teacher_home:
                nameET.setText(R.string.fence_teacher_home);
                break;
            case R.id.fence_home:
                nameET.setText(R.string.fence_home);
                break;
            case R.id.fence_grandmother_home:
                nameET.setText(R.string.fence_grandmother_home);
                break;
            case R.id.fence_grandma_home:
                nameET.setText(R.string.fence_grandma_home);
                break;
            case R.id.backBtn:
                FenceSetupActivity.this.finish();
                break;
            case R.id.button1:
                double mLatitude = (Utils.getAround(latitude, longitude, radius))[0];
                double mLongitude = (Utils.getAround(latitude, longitude, radius))[1];
                try {
                    JSONObject object = new JSONObject();
                    object.put("id", fence.getId());
                    object.put("areaType", "1");
                    object.put("areaName", nameET.getText().toString());
                    object.put("startTime", startTimeTV.getText().toString());
                    object.put("endTime", endTimeTV.getText().toString());
                    List<String> days = new ArrayList<String>();
                    //JSONArray weekDays = new JSONArray();
                    if (checkTV1.isChecked()) {
                        days.add("1");
                        //weekDays.put("1");
//						weekDays.put()
//						weekDays.put("mondayStartTime", startTimeTV.getText().toString());
//						weekDays.put("mondayEndTime",endTimeTV.getText().toString());
                    }
//					else {
//						weekDays.put("2");
////						weekDays.put("mondayStartTime", "");
////						weekDays.put("mondayEndTime", "");
//					}
                    if (checkTV2.isChecked()) {
                        days.add("2");
                        //weekDays.put("2");
//						weekDays.put("tuesdayStartTime", startTimeTV.getText());
//						weekDays.put("tuesdayEndTime", endTimeTV.getText());
                    }

//					else {
//						weekDays.put("tuesdayStartTime", "");
//						weekDays.put("tuesdayEndTime", "");
//					}
                    if (checkTV3.isChecked()) {
                        days.add("3");
                        //weekDays.put("3");
//						weekDays.put("wednesdayStartTime",startTimeTV.getText());
//						weekDays.put("wednesdayEndTime",endTimeTV.getText());
                    }
//					else {
//						weekDays.put("wednesdayStartTime", "");
//						weekDays.put("wednesdayEndTime", "");
//					}
                    if (checkTV4.isChecked()) {
                        days.add("4");
                        //weekDays.put("4");
//						weekDays.put("thursdayStartTime", startTimeTV.getText());
//						weekDays.put("thursdayEndTime", endTimeTV.getText());
                    }
//					else {
//						weekDays.put("thursdayStartTime", "");
//						weekDays.put("thursdayEndTime", "");
//					}
                    if (checkTV5.isChecked()) {
                        days.add("5");
                        //weekDays.put("5");
//						weekDays.put("fridayStartTime", startTimeTV.getText());
//						weekDays.put("fridayEndTime", endTimeTV.getText());
                    }
//					else {
//						weekDays.put("fridayStartTime", "");
//						weekDays.put("fridayEndTime", "");
//					}
                    if (checkTV6.isChecked()) {
                        days.add("6");
//						weekDays.put("saturdayStartTime", startTimeTV.getText());
//						weekDays.put("saturdayEndTime", endTimeTV.getText());
                    }
//					else {
//						weekDays.put("saturdayStartTime", "");
//						weekDays.put("saturdayEndTime", "");
//					}
                    if (checkTV7.isChecked()) {
                        days.add("7");
                        //weekDays.put("sundayStartTime", startTimeTV.getText());
                        //weekDays.put("sundayEndTime", endTimeTV.getText());
                    }
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < days.size(); i++) {
                        if (i == (days.size() - 1)) {
                            sb.append(days.get(i));
                        } else {
                            sb.append(days.get(i)).append(",");
                        }
                    }
                    object.put("weekDays", sb);
                    fence.setWeekDays(sb.toString());

                    JSONArray array = new JSONArray();
                    JSONObject coordinates1 = new JSONObject();
                    coordinates1.put("longitude", longitude);
                    coordinates1.put("latitude", latitude);
                    coordinates1.put("radius", radius);
                    array.put(coordinates1);
                    /*
                    JSONObject coordinates2 = new JSONObject();
                    coordinates2.put("longitude",mLongitude);
					coordinates2.put("latitude",mLatitude);
					coordinates2.put("radius",radius);
					array.put(coordinates2);
					*/
                    object.put("coordinates", array);
                    object.put("deviceId", Session.getInstance().getSetupDevice().getId());
                    addFence(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.checkTV1:
                if (checkTV1.isChecked()) {
                    checkTV1.setChecked(false);
                } else {
                    checkTV1.setChecked(true);
                }
                break;
            case R.id.checkTV2:
                if (checkTV2.isChecked()) {
                    checkTV2.setChecked(false);
                } else {
                    checkTV2.setChecked(true);
                }
                break;
            case R.id.checkTV3:
                if (checkTV3.isChecked()) {
                    checkTV3.setChecked(false);
                } else {
                    checkTV3.setChecked(true);
                }
                break;
            case R.id.checkTV4:
                if (checkTV4.isChecked()) {
                    checkTV4.setChecked(false);
                } else {
                    checkTV4.setChecked(true);
                }
                break;
            case R.id.checkTV5:
                if (checkTV5.isChecked()) {
                    checkTV5.setChecked(false);
                } else {
                    checkTV5.setChecked(true);
                }
                break;
            case R.id.checkTV6:
                if (checkTV6.isChecked()) {
                    checkTV6.setChecked(false);
                } else {
                    checkTV6.setChecked(true);
                }
                break;
            case R.id.checkTV7:
                if (checkTV7.isChecked()) {
                    checkTV7.setChecked(false);
                } else {
                    checkTV7.setChecked(true);
                }
                break;
            case R.id.disTV:
                //showDisDialog();

                break;
            case R.id.startTimeTV:
                showTimeDialog(startTimeTV.getText().toString(), (TextView) view);
                break;
            case R.id.endTimeTV:
                showTimeDialog(endTimeTV.getText().toString(), (TextView) view);
                break;
        }
    }

    public void showDisDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择");
        builder.setItems(this.radiusStrings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                disTV.setText(radiusStrings[i]);
                radius = Integer.parseInt(radiusStrings[i]);
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);

    private void showTimeDialog(String time, final TextView tv) {
        df.applyPattern("kk:mm");
        Date date = new Date();
        mCalendar.setTime(date);
        int hours = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        try {
            date = df.parse(time);
            mCalendar.setTime(date);
            hours = mCalendar.get(Calendar.HOUR_OF_DAY);
            minute = mCalendar.get(Calendar.MINUTE);
        } catch (Exception ex) {

        }
        TimePickerDialog pickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker arg0, int house, int min) {
                        String str = "";
                        if (house < 10) {
                            str += "0";
                        }
                        str += house + ":";
                        if (min < 10) str += "0";
                        str += min;
                        tv.setText(str);
                    }
                }, hours, minute, true);
        pickerDialog.show();
    }

}
