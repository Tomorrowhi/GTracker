package com.bct.gpstracker.my.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.bct.gpstracker.R;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.server.CommService;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JSONHelper;
import com.bct.gpstracker.view.CheckTextView;
import com.bct.gpstracker.vo.CmdType;
import com.bct.gpstracker.vo.Session;

/**
 * 设备休眠主页面
 *
 * @author huangfei
 */
public class DeviceRestActivity extends BaseActivity implements OnClickListener {

    public static final int INDEX = 1;
    private ImageButton backButton;
    private Button confirmButton;
    //	WheelView hours;
//	WheelView mins;
//	WheelView ampm;
//	WheelView hours1;
//	WheelView mins1;
//	WheelView ampm1;
//	String[] ampmStrings = new String[] { "上午","下午"};
    private CheckTextView checkTV1, checkTV2, checkTV3, checkTV4, checkTV5, checkTV6, checkTV7,checkTVAll;
    private CheckTextView checkTVOne1, checkTVOne2, checkTVOne3, checkTVOne4, checkTVOne5, checkTVOne6, checkTVOne7,checkTVOneAll;
    private TextView startTimeTV, endTimeTV, startTimeTV1, endTimeTV1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_rest);

        TextView titleTV = (TextView) findViewById(R.id.titleNameTV);
        titleTV.setText(getString(R.string.setup_rest_time) + "-" + Session.getInstance().getSetupDevice().getName());
        if (Session.getInstance().getDevice().getOnline() != 1) {
            Toast.makeText(DeviceRestActivity.this,R.string.setup_rest_must_online,Toast.LENGTH_SHORT).show();
        }

        backButton = (ImageButton) findViewById(R.id.backBtn);
        confirmButton = (Button) findViewById(R.id.button1);
        checkTV1 = (CheckTextView) findViewById(R.id.checkTV1);
        checkTV2 = (CheckTextView) findViewById(R.id.checkTV2);
        checkTV3 = (CheckTextView) findViewById(R.id.checkTV3);
        checkTV4 = (CheckTextView) findViewById(R.id.checkTV4);
        checkTV5 = (CheckTextView) findViewById(R.id.checkTV5);
        checkTV6 = (CheckTextView) findViewById(R.id.checkTV6);
        checkTV7 = (CheckTextView) findViewById(R.id.checkTV7);
        checkTVAll = (CheckTextView) findViewById(R.id.checkTV_all);

        checkTVOne1 = (CheckTextView) findViewById(R.id.checkTV_one_1);
        checkTVOne2 = (CheckTextView) findViewById(R.id.checkTV_one_2);
        checkTVOne3 = (CheckTextView) findViewById(R.id.checkTV_one_3);
        checkTVOne4 = (CheckTextView) findViewById(R.id.checkTV_one_4);
        checkTVOne5 = (CheckTextView) findViewById(R.id.checkTV_one_5);
        checkTVOne6 = (CheckTextView) findViewById(R.id.checkTV_one_6);
        checkTVOne7 = (CheckTextView) findViewById(R.id.checkTV_one_7);
        checkTVOneAll = (CheckTextView) findViewById(R.id.checkTV_one_all);

        backButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        checkTV1.setOnClickListener(this);
        checkTV2.setOnClickListener(this);
        checkTV3.setOnClickListener(this);
        checkTV4.setOnClickListener(this);
        checkTV5.setOnClickListener(this);
        checkTV6.setOnClickListener(this);
        checkTV7.setOnClickListener(this);
        checkTVAll.setOnClickListener(this);

        checkTVOne1.setOnClickListener(this);
        checkTVOne2.setOnClickListener(this);
        checkTVOne3.setOnClickListener(this);
        checkTVOne4.setOnClickListener(this);
        checkTVOne5.setOnClickListener(this);
        checkTVOne6.setOnClickListener(this);
        checkTVOne7.setOnClickListener(this);
        checkTVOneAll.setOnClickListener(this);

        startTimeTV = (TextView) findViewById(R.id.startTimeTV);
        endTimeTV = (TextView) findViewById(R.id.endTimeTV);
        startTimeTV1 = (TextView) findViewById(R.id.startTimeTV2);
        endTimeTV1 = (TextView) findViewById(R.id.endTimeTV2);

        startTimeTV.setOnClickListener(this);
        endTimeTV.setOnClickListener(this);
        startTimeTV.setText("00:00");
        endTimeTV.setText("00:00");

        startTimeTV1.setOnClickListener(this);
        endTimeTV1.setOnClickListener(this);
        startTimeTV1.setText("00:00");
        endTimeTV1.setText("00:00");

        this.initData();

    }

    private void initData() {
        Session.getInstance().getSetupDevice().getCommandDataByImeiAndType(DeviceRestActivity.this, CmdType.CL.getType(), new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.get_data, DeviceRestActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    //init data
                    String type = JSONHelper.getString(obj.getBody(), "type");
                    if (CmdType.CL.getType().equals(type)) {
//						Toast.makeText(DeviceRestActivity.this, JSONHelper.getString(obj.getBody(), "content"), Toast.LENGTH_SHORT).show();
                        String cmdCont = JSONHelper.getString(obj.getBody(), "content");
                        initView(cmdCont);
                    }
                } else {
                    Toast.makeText(DeviceRestActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(DeviceRestActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView(String cont) {
        if (CommUtil.isBlank(cont)) {
            return;
        }
        String[] conts = cont.split(" *, *");
        if (conts.length != 5) {
            Log.e(Constants.TAG, "指令长度不为10节，长度不对，指令内容：" + cont);
            return;
        }
        String[] time1 = formatTimeStr(conts[1].replaceAll(CmdType.CL.getType() + "=", ""));
        String[] time2 = formatTimeStr(conts[3]);
        startTimeTV.setText(time1[0]);
        endTimeTV.setText(time1[1]);
        startTimeTV1.setText(time2[0]);
        endTimeTV1.setText(time2[1]);

        int j = 2;
        checkTVOne1.setChecked(!"0".equals(conts[j].charAt(0)+""));
        checkTVOne2.setChecked(!"0".equals(conts[j].charAt(1)+""));
        checkTVOne3.setChecked(!"0".equals(conts[j].charAt(2)+""));
        checkTVOne4.setChecked(!"0".equals(conts[j].charAt(3)+""));
        checkTVOne5.setChecked(!"0".equals(conts[j].charAt(4)+""));
        checkTVOne6.setChecked(!"0".equals(conts[j].charAt(5)+""));
        checkTVOne7.setChecked(!"0".equals(conts[j].charAt(6)+""));

        int i = 4;
        checkTV1.setChecked(!"0".equals(conts[i].charAt(0)+""));
        checkTV2.setChecked(!"0".equals(conts[i].charAt(1)+""));
        checkTV3.setChecked(!"0".equals(conts[i].charAt(2)+""));
        checkTV4.setChecked(!"0".equals(conts[i].charAt(3)+""));
        checkTV5.setChecked(!"0".equals(conts[i].charAt(4)+""));
        checkTV6.setChecked(!"0".equals(conts[i].charAt(5)+""));
        checkTV7.setChecked(!"0".equals(conts[i].charAt(6)+""));
    }

    private String[] formatTimeStr(String str) {
        String[] gp = new String[2];
        StringBuffer sb = new StringBuffer(str);
        sb.insert(2, ':');
        sb.insert(8, ':');
        gp[0] = sb.substring(0, 5);
        gp[1] = sb.substring(6);
        return gp;
    }

    /**
     * 发送指令
     *
     * @param content 终端IMEI号
     */
    private void sendCommand(final String content) {
        CommService.get().sendCommand(DeviceRestActivity.this, Session.getInstance().getSetupDevice().getImei(), CmdType.CL.getType(), content, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog("", DeviceRestActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
                //CustomProgressDialog.getInstance(getActivity()).closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 1) {
                    Toast.makeText(DeviceRestActivity.this, R.string.setup_rest_time_success_later, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DeviceRestActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(DeviceRestActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBtn:
                DeviceRestActivity.this.finish();
                break;
            case R.id.button1:
                try {
                    String startTime = startTimeTV.getText().toString();
                    String endTime = endTimeTV.getText().toString();
                    StringBuffer bufferOne = new StringBuffer();
                    StringBuffer bufferTwo = new StringBuffer();
                    if (checkTVOne1.isChecked()) {
                        bufferOne.append("1");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne2.isChecked()) {
                        bufferOne.append("2");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne3.isChecked()) {
                        bufferOne.append("3");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne4.isChecked()) {
                        bufferOne.append("4");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne5.isChecked()) {
                        bufferOne.append("5");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne6.isChecked()) {
                        bufferOne.append("6");
                    } else {
                        bufferOne.append("0");
                    }
                    if (checkTVOne7.isChecked()) {
                        bufferOne.append("7");
                    } else {
                        bufferOne.append("0");
                    }

                    if (checkTV1.isChecked()) {
                        bufferTwo.append("1");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV2.isChecked()) {
                        bufferTwo.append("2");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV3.isChecked()) {
                        bufferTwo.append("3");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV4.isChecked()) {
                        bufferTwo.append("4");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV5.isChecked()) {
                        bufferTwo.append("5");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV6.isChecked()) {
                        bufferTwo.append("6");
                    } else {
                        bufferTwo.append("0");
                    }
                    if (checkTV7.isChecked()) {
                        bufferTwo.append("7");
                    } else {
                        bufferTwo.append("0");
                    }
                    String content = String.format("%s-%s,%s,%s-%s,%s", startTimeTV.getText().toString(), endTimeTV.getText().toString(), bufferOne, startTimeTV1.getText().toString(), endTimeTV1.getText().toString(), bufferTwo);
                    //String content = startTime+"-"+endTime+","+startTime+"-"+endTime+","+bufferTwo.toString();
                    content = content.replace(":", "");
                    if (Session.getInstance().getDevice() != null) {
                        sendCommand(content);
                    }
//					addFence(object);
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
            case R.id.checkTV_all:
                if (checkTVAll.isChecked()) {
                    checkTV1.setChecked(false);
                    checkTV2.setChecked(false);
                    checkTV3.setChecked(false);
                    checkTV4.setChecked(false);
                    checkTV5.setChecked(false);
                    checkTV6.setChecked(false);
                    checkTV7.setChecked(false);
                    checkTVAll.setChecked(false);
                } else {
                    checkTV1.setChecked(true);
                    checkTV2.setChecked(true);
                    checkTV3.setChecked(true);
                    checkTV4.setChecked(true);
                    checkTV5.setChecked(true);
                    checkTV6.setChecked(true);
                    checkTV7.setChecked(true);
                    checkTVAll.setChecked(true);
                }
                break;

            case R.id.checkTV_one_1:
                if (checkTVOne1.isChecked()) {
                    checkTVOne1.setChecked(false);
                } else {
                    checkTVOne1.setChecked(true);
                }
                break;
            case R.id.checkTV_one_2:
                if (checkTVOne2.isChecked()) {
                    checkTVOne2.setChecked(false);
                } else {
                    checkTVOne2.setChecked(true);
                }
                break;
            case R.id.checkTV_one_3:
                if (checkTVOne3.isChecked()) {
                    checkTVOne3.setChecked(false);
                } else {
                    checkTVOne3.setChecked(true);
                }
                break;
            case R.id.checkTV_one_4:
                if (checkTVOne4.isChecked()) {
                    checkTVOne4.setChecked(false);
                } else {
                    checkTVOne4.setChecked(true);
                }
                break;
            case R.id.checkTV_one_5:
                if (checkTVOne5.isChecked()) {
                    checkTVOne5.setChecked(false);
                } else {
                    checkTVOne5.setChecked(true);
                }
                break;
            case R.id.checkTV_one_6:
                if (checkTVOne6.isChecked()) {
                    checkTVOne6.setChecked(false);
                } else {
                    checkTVOne6.setChecked(true);
                }
                break;
            case R.id.checkTV_one_7:
                if (checkTVOne7.isChecked()) {
                    checkTVOne7.setChecked(false);
                } else {
                    checkTVOne7.setChecked(true);
                }
                break;
            case R.id.checkTV_one_all:
                if (checkTVOneAll.isChecked()) {
                    checkTVOne1.setChecked(false);
                    checkTVOne2.setChecked(false);
                    checkTVOne3.setChecked(false);
                    checkTVOne4.setChecked(false);
                    checkTVOne5.setChecked(false);
                    checkTVOne6.setChecked(false);
                    checkTVOne7.setChecked(false);
                    checkTVOneAll.setChecked(false);
                } else {
                    checkTVOne1.setChecked(true);
                    checkTVOne2.setChecked(true);
                    checkTVOne3.setChecked(true);
                    checkTVOne4.setChecked(true);
                    checkTVOne5.setChecked(true);
                    checkTVOne6.setChecked(true);
                    checkTVOne7.setChecked(true);
                    checkTVOneAll.setChecked(true);
                }
                break;

            case R.id.startTimeTV:
                showTimeDialog(startTimeTV.getText().toString(), (TextView) view);
                break;
            case R.id.endTimeTV:
                showTimeDialog(endTimeTV.getText().toString(), (TextView) view);
                break;
            case R.id.startTimeTV2:
                showTimeDialog(startTimeTV1.getText().toString(), (TextView) view);
                break;
            case R.id.endTimeTV2:
                showTimeDialog(endTimeTV1.getText().toString(), (TextView) view);
                break;
        }
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
