package com.bct.gpstracker.my.activity;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bct.gpstracker.R;
import com.bct.gpstracker.baby.activity.ChatActivity;
import com.bct.gpstracker.base.BaseActivity;
import com.bct.gpstracker.common.AudioRecorder;
import com.bct.gpstracker.common.CommonRestPath;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.AlarmEntity;
import com.bct.gpstracker.pojo.Music;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.service.PlayerService;
import com.bct.gpstracker.util.BctClient;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.JsonHttpResponseHelper;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CheckTextView;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 * 生活助手的添加与编辑页面
 *
 * @author huangfei
 */
public class AddAlarmActivity extends BaseActivity implements OnClickListener {

    private ImageButton backButton, addButton, voiceButton;
    private EditText nameText, contentET;
    private Button deleteBtn, saveBtn, playBtn, alarmRingAuditionBtn;

    private String termImei;

    //	WheelView hours;
//	WheelView mins;
//	WheelView ampm;
//	String[] ampmStrings = new String[] { "上午","下午"};
    private CheckTextView checkTV1, checkTV2, checkTV3, checkTV4, checkTV5, checkTV6, checkTV7,checkTVAll;
    private TextView dateTV;
    private AlarmEntity mEntity;
    //private String type = "";

    private static int MAX_TIME = 15; // 最长录制时间，单位秒，0为无时间限制
    private static int MIX_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1
    private static int RECORD_NO = 0; // 不在录音
    private static int RECORD_ING = 1; // 正在录音
    private static int RECODE_ED = 2; // 完成录音
    private static int RECODE_STATE = 0; // 录音的状态
    private static float recodeTime = 0.0f; // 录音的时间
    private static double voiceValue = 0.0; // 麦克风获取的音量值
    private String folderName = "";    //文件夹的名称
    private long amrName;//语音文件的名称
    private AudioRecorder mr;

    private ImageView dialog_img;
    private Dialog dialog;
    private boolean hasVoice = false;
    private ProgressBar dialog_pro;//录音进度条
    private Thread recordThread;
    private Spinner alarmRingSelectSp;
    private List<Music> musicList = new ArrayList<>();
    private ArrayList<String> musicListNames = new ArrayList<>();
    private ArrayAdapter<String> stringArrayAdapter;
    private boolean isPlaying = false;
    private PlayerService playerService;
    private ExecutorService playMusicThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        Intent intent = new Intent(AddAlarmActivity.this, PlayerService.class);
        startService(intent);

        dateTV = (TextView) findViewById(R.id.dateTV);
        dateTV.setOnClickListener(this);
        nameText = (EditText) findViewById(R.id.nameET);
        contentET = (EditText) findViewById(R.id.contentET);
        alarmRingSelectSp = (Spinner) findViewById(R.id.alarmRingSelectSp);

        loadPage();

        stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, musicListNames);
        alarmRingSelectSp.setAdapter(stringArrayAdapter);

        mEntity = (AlarmEntity) getIntent().getSerializableExtra("alarm");


        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        saveBtn = (Button) findViewById(R.id.completeBtn);
        playBtn = (Button) findViewById(R.id.playButton);
        alarmRingAuditionBtn = (Button) findViewById(R.id.alarmRingAuditionBtn);

        backButton = (ImageButton) findViewById(R.id.backBtn);
        addButton = (ImageButton) findViewById(R.id.addBtn);

        checkTV1 = (CheckTextView) findViewById(R.id.checkTV1);
        checkTV2 = (CheckTextView) findViewById(R.id.checkTV2);
        checkTV3 = (CheckTextView) findViewById(R.id.checkTV3);
        checkTV4 = (CheckTextView) findViewById(R.id.checkTV4);
        checkTV5 = (CheckTextView) findViewById(R.id.checkTV5);
        checkTV6 = (CheckTextView) findViewById(R.id.checkTV6);
        checkTV7 = (CheckTextView) findViewById(R.id.checkTV7);
        checkTVAll = (CheckTextView) findViewById(R.id.checkTVAll);

        backButton.setOnClickListener(this);
        checkTV1.setOnClickListener(this);
        checkTV2.setOnClickListener(this);
        checkTV3.setOnClickListener(this);
        checkTV4.setOnClickListener(this);
        checkTV5.setOnClickListener(this);
        checkTV6.setOnClickListener(this);
        checkTV7.setOnClickListener(this);
        checkTVAll.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        alarmRingAuditionBtn.setOnClickListener(this);

        amrName = System.currentTimeMillis();
        dateTV.setText("00:00");
        playBtn.setVisibility(View.GONE);
        if (mEntity == null) {
            mEntity = new AlarmEntity();
            deleteBtn.setVisibility(View.GONE);
            folderName = Utils.Md5(Session.getInstance().getUser().getPhone());
        } else {
            folderName = Utils.Md5(mEntity.getImei() + Session.getInstance().getUser().getPhone());
            if (mEntity.getTime().length() == 4) {
                dateTV.setText(String.format("%s:%s", mEntity.getTime().substring(0, 2), mEntity.getTime().substring(2, 4)));
            } else {
                dateTV.setText(mEntity.getTime());
            }
//			this.downloadUrl(mEntity.getVoiceUrl());;
            nameText.setText(mEntity.getName());
            contentET.setText(mEntity.getContent());
            String[] weeks = mEntity.getWeeks().split(",");
            if (weeks.length == 7) {
                for (int i = 0; i < weeks.length; i++) {
                    String w = weeks[i];
                    switch (i) {
                        case 0:
                            checkTV1.setChecked(!(w.equals("0")));
                            break;
                        case 1:
                            checkTV2.setChecked(!(w.equals("0")));
                            break;
                        case 2:
                            checkTV3.setChecked(!(w.equals("0")));
                            break;
                        case 3:
                            checkTV4.setChecked(!(w.equals("0")));
                            break;
                        case 4:
                            checkTV5.setChecked(!(w.equals("0")));
                            break;
                        case 5:
                            checkTV6.setChecked(!(w.equals("0")));
                            break;
                        case 6:
                            checkTV7.setChecked(!(w.equals("0")));
                            break;
                    }
                }
            }
        }

        //addButton.setOnClickListener(clickListener);

        voiceButton = (ImageButton) findViewById(R.id.voiceButton);
        voiceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (RECODE_STATE != RECORD_ING) {
                            String sdState = Environment.getExternalStorageState();// 获得sd卡的状态
                            if (!sdState.equals(Environment.MEDIA_MOUNTED)) { // 判断SD卡是否存在
                                // 提示sd卡不存在
                                Toast.makeText(AddAlarmActivity.this, R.string.media_mounted_no, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            deleteOldFile();
                            //amrName = System.currentTimeMillis();
                            mr = new AudioRecorder(AddAlarmActivity.this, folderName, amrName + "");
                            RECODE_STATE = RECORD_ING;
                            showVoiceDialog();
                            try {
                                mr.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mythread();
                        }
                    {
//							Resources rs = getResources();
//							Drawable dw = rs
//									.getDrawable(R.drawable.buttonbackground_down);
//							v.setBackgroundDrawable(dw);
                    }
                    break;
                    case MotionEvent.ACTION_UP:
                        if (RECODE_STATE == RECORD_ING) {
                            RECODE_STATE = RECODE_ED;
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            try {
                                mr.stop();
                                voiceValue = 0.0;
                            } catch (IOException e) {
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
                                playBtn.setVisibility(View.VISIBLE);
                                //sendVoice(mEntity.getImei(),"0001");
                            }
                        }
                    {
//							Resources rs = getResources();
//							Drawable dw = rs.getDrawable(R.drawable.buttonbackground_up);
//							v.setBackgroundDrawable(dw);
                    }
                    break;
                }
                return false;
            }
        });



    }

    private void deleteOldFile() {
        File file = new File(Environment.getExternalStorageDirectory(), "gpstracker/voice.amr");
        if (file.exists()) {
            file.delete();
        }
    }

    // 录音时显示麦克风图片的Dialog
    protected void showVoiceDialog() {
        dialog = new Dialog(AddAlarmActivity.this, R.style.DialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.my_dialog);
        dialog_img = (ImageView) dialog.findViewById(R.id.dialog_img);
        dialog_pro = (ProgressBar) dialog.findViewById(R.id.dialog_pro);
        dialog_pro.setMax(MAX_TIME * 10);
        dialog.show();
    }

    // 录音时间太短时Toast显示
    void showWarnToast() {
        deleteOldFile();
        Toast toast = new Toast(AddAlarmActivity.this);
        LinearLayout linearLayout = new LinearLayout(AddAlarmActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(20, 20, 20, 20);

        // 定义一个ImageView
        ImageView imageView = new ImageView(AddAlarmActivity.this);
        imageView.setImageResource(R.drawable.voice_to_short); // 图标

        TextView mTv = new TextView(AddAlarmActivity.this);
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

    // 录音计时线程
    void mythread() {
        recordThread = new Thread(ImgThread);
        recordThread.start();
    }

    // 录音线程
    private Runnable ImgThread = new Runnable() {

        @Override
        public void run() {
            recodeTime = 0.0f;
            while (RECODE_STATE == RECORD_ING) {
                if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
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
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                //更改 录音超时Toast @author zhongjiayuan
                                Toast timeOuToast = Toast.makeText(AddAlarmActivity.this, "录音超时了", Toast.LENGTH_SHORT);
                                timeOuToast.setGravity(Gravity.CENTER, 0, 0);
                                timeOuToast.show();
                            }
                            try {
                                mr.stop();
                                voiceValue = 0.0;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (recodeTime < 1.0) {
                                showWarnToast();
//									record.setText("按住开始录音");
                                RECODE_STATE = RECORD_NO;
                            } else {
//									record.setText("录音完成!点击重新录音");
                                hasVoice = true;
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
     * 新增生活助手
     */
    private void addAlarm(AlarmEntity mEntity) {
        //新的闹钟不需要录音
        try {
            String fileName = "ls," + Session.getInstance().getSetupDevice().getImei() + "," + Session.getInstance().getSetupDevice().getImei() + ",0001";
//			if(mEntity.getId() == 0){
//				File file = new File(fileName);
//				if(!file.exists()){
//					Toast.makeText(AddAlarmActivity.this, "语音不能为空", Toast.LENGTH_SHORT).show();
//					return;
//				}
//			}

            final String filePath = AddAlarmActivity.this.getFilesDir() + "/" + folderName + "/" + amrName + ".amr";

            final JSONObject object = new JSONObject();
            object.put("id", mEntity.getId());
            object.put("name", mEntity.getName());
            object.put("termId", mEntity.getTermId());
            object.put("startTime", mEntity.getTime());
            object.put("endTime", mEntity.getTime());
            object.put("weeks", mEntity.getWeeks());
            object.put("imei", mEntity.getImei());
            object.put("fileName", fileName);

            //把内容加到json中
            object.put("content", mEntity.getContent());
            //添加audioSeq（铃声所在位置）到json中
            object.put("audioSeq", alarmRingSelectSp.getSelectedItemPosition()+1);


            //不需要录音，这里注释掉
//			String voiceFile = Utils.encodeBase64File(filePath);
//			object.put("voiceFile", voiceFile);


            AlarmEntity.add(AddAlarmActivity.this, object, new BctClientCallback() {
                @Override
                public void onStart() {
                    WizardAlertDialog.getInstance().showProgressDialog(R.string.post_data, AddAlarmActivity.this);
                }

                @Override
                public void onFinish() {
                    WizardAlertDialog.getInstance().closeProgressDialog();
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    if (obj.getRetcode() == 1) {
                        Toast.makeText(AddAlarmActivity.this, getString(R.string.add_alarm_save_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddAlarmActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(AddAlarmActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(AddAlarmActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.dateTV:
                showTimeDialog(this.dateTV.getText().toString());
                break;
            case R.id.backBtn:
                AddAlarmActivity.this.finish();
                break;
            case R.id.completeBtn:
//                if (nameText.getText().toString().trim().equals("")) {
//                    Toast.makeText(AddAlarmActivity.this, R.string.alarm_name_null, Toast.LENGTH_SHORT).show();
//                    return;
//                } else if (CommUtil.calcASCIILen(nameText.getText().toString().trim()) > Constants.ALARM_MAX_TITLE_LEN) {
//                    Toast.makeText(AddAlarmActivity.this, getString(R.string.alarm_title_max_text), Toast.LENGTH_SHORT).show();
//                    return;
//                }
                addAlarmEntry();
                break;
            case R.id.deleteBtn:
                mEntity.delete(this, new BctClientCallback() {
                    @Override
                    public void onStart() {
                        WizardAlertDialog.getInstance().showProgressDialog("", AddAlarmActivity.this);
                    }

                    @Override
                    public void onFinish() {
                        WizardAlertDialog.getInstance().closeProgressDialog();
                    }

                    @Override
                    public void onSuccess(ResponseData obj) {
                        if (obj.getRetcode() == 1) {
                            Toast.makeText(AddAlarmActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddAlarmActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                    }
                });
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
            case R.id.checkTVAll:
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
            case R.id.playButton:
                final String filePath = AddAlarmActivity.this.getFilesDir() + "/" + folderName + "/" + amrName + ".amr";
                playVoice(filePath);
                break;
            case R.id.alarmRingAuditionBtn:
                playMusic(alarmRingSelectSp.getSelectedItemPosition());
                break;

        }
    }

    private void addAlarmEntry() {
        String startTime = dateTV.getText().toString();
        String endTime = "";
        StringBuffer buffer = new StringBuffer();
        CheckTextView[] checkTextViews=new CheckTextView[]{checkTV1,checkTV2,checkTV3,checkTV4,checkTV5,checkTV6,checkTV7};
        for(int i=0;i<checkTextViews.length;i++){
            buffer.append(checkTextViews[i].isChecked()?i+1:0).append(',');
        }
        if(buffer.length()>0){
            buffer.deleteCharAt(buffer.length()-1);
        }

        if(buffer.length()==0){
            CommUtil.showMsgShort(getString(R.string.alarm_week_limit));
            return;
        }
        mEntity.setName(nameText.getText().toString());
        mEntity.setWeeks(buffer.toString());
        mEntity.setTime(startTime);

        //添加内容
        //闹钟内容限制在15个汉字，30个英文字符
        String content = contentET.getText().toString().trim();
        if(CommUtil.isBlank(content)){
            CommUtil.showMsgShort(getString(R.string.alarm_content_max_text));
            return;
        }
        if (CommUtil.calcASCIILen(content) > Constants.ALARM_MAX_CONTENT_LEN) {
            Toast.makeText(AddAlarmActivity.this, getString(R.string.alarm_content_max_text), Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.contains("*")) {
            Toast.makeText(AddAlarmActivity.this, "内容包含非法字符：* ", Toast.LENGTH_SHORT).show();
            return;
        }
        mEntity.setContent(content);


        if (Session.getInstance().getSetupDevice() != null) {
            mEntity.setImei(Session.getInstance().getSetupDevice().getImei());
        } else {
            mEntity.setImei("");
        }
        mEntity.setTermId(Session.getInstance().getSetupDevice().getId());

        addAlarm(mEntity);
    }

    /**
     * 播放语音文件
     *
     * @param filePath
     */
    private void playVoice(String filePath) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(filePath);
//			    mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
//			 mediaPlayer.stop();
//			 mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);

    private void showTimeDialog(String time) {
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
                        dateTV.setText(str);
                    }
                }, hours, minute, true);
        pickerDialog.show();
    }

    private void downloadUrl(String url) {
        /*BctClient.getInstance().get(url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                final String filePath = AddAlarmActivity.this.getFilesDir() + "/" + folderName + "/" + amrName + ".amr";
                File file = new File(filePath);
                try {
                    if (file.exists()) file.delete();
                    FileOutputStream oStream = new FileOutputStream(file);
                    oStream.write(bytes);
                    oStream.flush();
                    oStream.close();
                    playBtn.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AddAlarmActivity.this, "语音文件下载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(AddAlarmActivity.this, "语音文件下载失败", Toast.LENGTH_SHORT).show();
                throwable.printStackTrace();
            }
        });
*/
        if (CommUtil.isBlank(url)) {
            return;
        }
        HttpUtils http = new HttpUtils();
        final String filePath = AddAlarmActivity.this.getFilesDir() + "/" + folderName + "/" + amrName + ".amr";
        RequestParams params = new RequestParams();
        params.addHeader("accesskey", Session.getInstance().getAccessKey());
        HttpHandler handler = http.download(url,
                filePath,
                params,
                true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                new RequestCallBack<File>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        playBtn.setVisibility(View.VISIBLE);
                    }


                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.e(Constants.TAG, msg, error);
                        Toast.makeText(AddAlarmActivity.this, "语音文件下载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadPage() {
        try {
//			CommUtil.showProcessing(musicListView,true,false);
            JSONObject json = new JSONObject();
            json.put("imei", ChatActivity.mEntityImei);
            BctClientCallback callback = new BctClientCallback() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                }

                @Override
                public void onSuccess(ResponseData obj) {
                    CommUtil.hideProcessing();
                    if (obj.getRetcode() != 1) {
                        Toast.makeText(AddAlarmActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONArray array = obj.getBodyArray();
                        if (array != null && array.length() > 0) {
                            musicList.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                Music music = new Music();
                                music.setId(jsonObject.getInt("audioId"));
                                music.setUrl(jsonObject.optString("audioUrl"));
                                String name = jsonObject.optString("audioName");
                                int idx;
                                if (CommUtil.isNotBlank(name) && (idx = name.indexOf('_')) != -1) {
                                    name = name.substring(idx + 1, name.length());
                                }
                                music.setName(name);
//                                music.setFileSize(jsonObject.optInt("audioSize"));
                                music.setSerial(i + 1);
                                music.setStatus(jsonObject.optInt("recordState"));
                                musicList.add(music);
                                musicListNames.add(music.getName());
                            }

                            alarmRingSelectSp.setSelection(mEntity.getAudioSeq()-1);
                            stringArrayAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "获取列表失败！", e);
                    }
                }

                @Override
                public void onFailure(String message) {
                    CommUtil.hideProcessing();
                    Toast.makeText(AddAlarmActivity.this, R.string.load_audio_list_failed, Toast.LENGTH_SHORT).show();
                }
            };
            BctClient.getInstance().POST(AddAlarmActivity.this, CommonRestPath.audioDownloadedList(), json, new JsonHttpResponseHelper(callback).getHandler());
        } catch (Exception e) {
            Log.e(Constants.TAG, "获取分页失败！", e);
        }
    }

    private void playMusic(final int position) {

        if (isPlaying) { //为true说明正在播放，则停止播放
            isPlaying = false;
            alarmRingAuditionBtn.setBackgroundResource(R.drawable.location_history_start);
            if (null != playerService) {
                playerService.stop();
            }
            if (null != playMusicThread) {
                playMusicThread.shutdown();
                playMusicThread = null;
            }

        } else { //否则为播放g
            isPlaying = true;
            alarmRingAuditionBtn.setBackgroundResource(R.drawable.location_history_stop);
            playMusicThread = Executors.newSingleThreadExecutor();
            playMusicThread.submit(new PlayMusicTask(position));
        }
    }

    private class PlayMusicTask implements Runnable {
        private int position;

        PlayMusicTask(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            playerService = PlayerService.getPlayer();
            final Music music = musicList.get(position);
            if (CommUtil.isBlank(music.getUrl()) || !music.getUrl().toUpperCase().startsWith("HTTP://")) {
                Log.w(Constants.TAG, "音频播放地址无效:" + music.getUrl());
                return;
            }
            if (playerService != null) {
                playerService.setCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playerService.stop();
                    }
                });
                playerService.setPath(music.getUrl());
                int res = playerService.play();
                if (res == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddAlarmActivity.this, R.string.waiting, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddAlarmActivity.this, R.string.player_err, Toast.LENGTH_SHORT).show();
                        }
                    });
                    playerService.stop();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(AddAlarmActivity.this, PlayerService.class);
        stopService(intent);
    }
}


