package com.bct.gpstracker.my.activity;


import java.io.File;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.CommTitleActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.qrcode.CaptureActivity;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.FileUtils;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CircleImageView;
import com.lidroid.xutils.ViewUtils;
import com.nostra13.universalimageloader.core.ImageLoader;


//import com.bct.gpstracker.zxing.CaptureActivity;

/**
 * 增加监护对象
 *
 * @author huangfei
 */
public class AddDeviceActivity extends CommTitleActivity {

    private ImageButton backButton, selectImageButton, sacnButton;
    private TextView titleView;
    private CircleImageView photoView;
    private Button deleteButton, completeButton;
    private LinearLayout manLayout, womanLayout;
    private EditText nameText, birthdayText, signText, deviceET, phone;
    private CheckBox manBox, womanBox;

    //修改ui后新添加的元素
    private CheckBox boySele, girlSele;//男孩女孩的选择框
    private LinearLayout boyGroup, girlGroup;//选择男女孩的父布局

    private Device mDevice;//接受来自其他界面过来的数据
    private String path = "";
    private String basePath=Utils.getAvailableStoragePath();
    private Uri imageUri;
    private final static int TAKE_PICTURE = 899;
    private final static int OPEN_GALLERY = 799;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_add_device);
        setContentViewAddTop(R.layout.activity_add_device);
        ViewUtils.inject(this);
        mDevice = (Device) getIntent().getSerializableExtra("device");

        selectImageButton = (ImageButton) findViewById(R.id.selectImageBtn);
        sacnButton = (ImageButton) findViewById(R.id.imb_phone);//扫描二维码，这儿对应的是手表ID
        photoView = (CircleImageView) findViewById(R.id.photoIV);//监护对象头像

        nameText = (EditText) findViewById(R.id.nameET);//监护对象昵称
        deviceET = (EditText) findViewById(R.id.deviceET);//IMEI
        phone = (EditText) findViewById(R.id.phone); // 手机号
        birthdayText = (EditText) findViewById(R.id.birthdayET);//生日
        signText = (EditText) findViewById(R.id.qianET);

        boyGroup = (LinearLayout) findViewById(R.id.ll_boy);
        boyGroup.setOnClickListener(this);
        girlGroup = (LinearLayout) findViewById(R.id.ll_girl);
        girlGroup.setOnClickListener(this);
        boySele = (CheckBox) findViewById(R.id.cb_sex_boy);
        girlSele = (CheckBox) findViewById(R.id.cb_sex_girl);
        if (mDevice == null) {//新增监护对象

            title.setText(R.string.add_device_title);
        } else {//修改监护对象

            title.setText(R.string.edit_device_title);
            nameText.setText(mDevice.getName());

            birthdayText.setText(CommUtil.toStr(mDevice.getBirthday()));
            signText.setText(CommUtil.toStr(mDevice.getSign()));
            deviceET.setText(mDevice.getImei());
            boySele.setChecked(!"2".equals(mDevice.getSex()));
            girlSele.setChecked("2".equals(mDevice.getSex()));

            phone.setText(CommUtil.toStr(mDevice.getPhone()));
            mDevice.setOldName(mDevice.getName());
            mDevice.setOldPhone(mDevice.getPhone());
            if (CommUtil.isNotBlank(mDevice.getPortrait())) {
                ImageLoader.getInstance().displayImage(mDevice.getPortrait(), photoView);
            }
        }
        //设置顶部状态栏的相关控件
        moreFunction.setVisibility(View.GONE);
        complet.setVisibility(View.VISIBLE);
        //complet.setOnClickListener(clickListener);

        sacnButton.setOnClickListener(clickListener);

        selectImageButton.setOnClickListener(clickListener);

        birthdayText.setOnClickListener(clickListener);

        File dir=new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.ll_boy:
                boySele.setChecked(true);
                girlSele.setChecked(false);
                break;
            case R.id.ll_girl:
                girlSele.setChecked(true);
                boySele.setChecked(false);
                break;
        }
    }

    /**
     * 点击事件
     */
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.backBtn://弃用
                    AddDeviceActivity.this.finish();
                    break;
                case R.id.imb_phone:
                    Intent openCameraIntent = new Intent(AddDeviceActivity.this, CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 0);
                    break;
                /*case R.id.btn_complet: //添加设备完成的按钮
                    if(setComplet()){

                    }
                    break;*/
                case R.id.deleteBtn://弃用
                    new AlertDialog.Builder(AddDeviceActivity.this)
                            .setTitle(R.string.notice)
                            .setMessage(String.format(getString(R.string.confirm_msg), getString(R.string.delete_monitor_object)))
                            .setPositiveButton(R.string.yes, deleteClickListener)
                            .setNegativeButton(R.string.no, null)
                            .show();
                    break;
                case R.id.selectImageBtn:
                    showWindow(v);
                    break;
                case R.id.manLayout://弃用
                    manBox.setChecked(true);
                    womanBox.setChecked(false);
                    break;
                case R.id.womanLayout://弃用
                    manBox.setChecked(false);
                    womanBox.setChecked(true);
                    break;
                case R.id.birthdayET:
                    if (CommUtil.isNotBlank(birthdayText.getText())) {
                        String[] dateStrings = birthdayText.getText().toString().split("-");
                        WizardAlertDialog.showDateDialog(AddDeviceActivity.this, Integer.parseInt(dateStrings[0].trim()), Integer.parseInt(dateStrings[1].trim()), Integer.parseInt(dateStrings[2].trim()), birthdayText);
                    } else {
                        WizardAlertDialog.showDateDialog(AddDeviceActivity.this, 0, 0, 0, birthdayText);
                    }
                    break;
            }
        }
    };

    @Override
    protected void setComplet() {
        if (mDevice == null) {
            mDevice = new Device();
            mDevice.setId(0);
        }
        mDevice.setImei(deviceET.getText().toString());
        mDevice.setName(CommUtil.toStr(nameText.getText()));
        mDevice.setBirthday(CommUtil.toStr(birthdayText.getText()));
        mDevice.setSign(CommUtil.toStr(signText.getText()));
        mDevice.setSex(boySele.isChecked() ? "1" : "2");
        mDevice.setPhone(CommUtil.toStr(phone.getText()));
        if (CommUtil.isNotBlank(path)) {//不管是否是新增用户，只要修改头像就设置头像
            mDevice.setPortrait(Utils.encodeBase64File(path));
            FileUtils.delete(path);
        }
        editDevice(mDevice);
    }

    @Override
    protected boolean checkIsOk() {
        if (CommUtil.isBlank(nameText.getText())) {
            Toast.makeText(AddDeviceActivity.this, R.string.device_name_err, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (CommUtil.calcASCIILen(nameText.getText().toString()) > 6) {
            Toast.makeText(AddDeviceActivity.this, R.string.name_length_limit, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (CommUtil.isBlank(phone.getText())) {
            Toast.makeText(AddDeviceActivity.this, R.string.device_phone_err, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (CommUtil.isBlank(deviceET.getText())) {
            Toast.makeText(AddDeviceActivity.this, R.string.add_device_scan, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Utils.isMobileNO(phone.getText().toString().trim())) {
            Toast.makeText(AddDeviceActivity.this, getString(R.string.phone_format_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    DialogInterface.OnClickListener deleteClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mDevice != null) {
                mDevice.deleteMonitorObject(AddDeviceActivity.this, new BctClientCallback() {
                    @Override
                    public void onStart() {
                        CommUtil.showProcessing(AddDeviceActivity.this.rootView,true,true);
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onSuccess(ResponseData obj) {
                        CommUtil.hideProcessing();
                        if (obj.getRetcode() == 1) {
                            Toast.makeText(AddDeviceActivity.this, R.string.delete_device_success, Toast.LENGTH_SHORT).show();
                            AddDeviceActivity.this.finish();
                        } else {
                            Toast.makeText(AddDeviceActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        CommUtil.hideProcessing();
                        CommUtil.showMsgShort(message);
                    }
                });
            }
        }
    };

    /**
     * 显示选择照片和拍照对话框
     *
     * @param view
     */
    private void showWindow(View view) {
        LayoutInflater inflater = LayoutInflater.from(AddDeviceActivity.this);
        // 引入窗口配置文件
        View windown = inflater.inflate(R.layout.select_image_item, null);
        // 创建PopupWindow对象
        final PopupWindow pop = new PopupWindow(windown, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
        Button takeButton = (Button) windown.findViewById(R.id.takeBtn);
        Button selectButton = (Button) windown.findViewById(R.id.selectBtn);
        // 需要设置一下此参数，点击外边可消失
        pop.setBackgroundDrawable(new ColorDrawable(0x00ffffff));
        //设置点击窗口外边窗口消失
        pop.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        pop.setFocusable(true);
        pop.showAsDropDown(view);
        takeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pop.isShowing()) {
                    pop.dismiss();
                    Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //下面这句指定调用相机拍照后的照片存储的路径
                    path=basePath+"portrait_tmp.jpg";
                    intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(Constants.FILE_SCHEME+path));
                    startActivityForResult(intent1, TAKE_PICTURE);
                }
            }
        });
        selectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pop.isShowing()) {
                    pop.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, OPEN_GALLERY);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        path=basePath+"portrait_tmp.jpg";
        if (requestCode == 0 && resultCode == RESULT_OK) {//扫描二维码返回的值
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            deviceET.setText(scanResult);
        } else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            startPhotoZoom(Uri.parse(Constants.FILE_SCHEME+path));
        } else if (requestCode == OPEN_GALLERY && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                startPhotoZoom(data.getData());
            }
        } else if (requestCode == 3) {
            if (data != null && data.getData() != null) {
                InputStream in = null;
                try {
                    in = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
//                Bundle extras = data.getExtras();
//                Bitmap bitmap = extras.getParcelable("data");
                    photoView.setImageBitmap(bitmap);
                    Utils.saveImageFile(bitmap, path);
                    getContentResolver().delete(data.getData(), null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }else{
                Bitmap bitmap=BitmapFactory.decodeFile(path);
                if(bitmap!=null){
                    photoView.setImageBitmap(bitmap);
                }else {
                    CommUtil.showMsgShort(getString(R.string.crop_failed));
                }
            }
        }
    }

    private void setBackData(Device device) {
        Intent intent = new Intent();
        intent.putExtra("device", device);
        setResult(RESULT_OK, intent);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(Constants.FILE_SCHEME + path));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, 3);
    }


    /**
     * 添加设备
     */
    private void editDevice(final Device device) {
        Device.editMonitorObject(this, device, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AddDeviceActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
//                    Toast.makeText(AddDeviceActivity.this, R.string.add_device_user_success, Toast.LENGTH_LONG).show();
                    //当添加\修改联系人时，将联系人信息添加到通讯录（昵称作为名字，号码存入，作为标识）
                    if (null != device.getOldName()) {
                        if (!device.getOldName().equals(device.getName()) || !device.getOldName().equals(device.getPhone())) {
                            //添加新联系人,比较耗时,需要异步来执行
                            //addContact(mDevice.getName(), mDevice.getPhone());
                            AppContext.getEventBus().post(device, Constants.EVENT_TAG_UPDATE_TEL_ACCOUNT);
                        }
                    }
                    Device dv=new Device(obj.getBody());
                    if (dv.getId() != 0) {
                        AppContext.getEventBus().post(dv, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    }
                    new AlertDialog.Builder(AddDeviceActivity.this)
                            .setTitle(R.string.msg_notify)
                            .setMessage(R.string.add_device_user_success)
                            .setPositiveButton(R.string.known, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AddDeviceActivity.this.finish();
                                }
                            }).show();
                } else {
                    resetLoading(false);
                    CommUtil.showMsgShort(obj.getMsg());
                }
            }

            @Override
            public void onFailure(String message) {
                CommUtil.hideProcessing();
                resetLoading(false);
                CommUtil.showMsgShort(message);
            }
        });
    }
}
