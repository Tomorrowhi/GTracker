package com.bct.gpstracker.my.activity;

import java.io.File;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.CommTitleActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.dialog.WizardAlertDialog;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Keeper;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.User;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.FileUtils;
import com.bct.gpstracker.util.Utils;
import com.bct.gpstracker.view.CircleImageView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 增加监护人,修改监护人信息
 *
 * @author huangfei
 */
public class AddUserActivity extends CommTitleActivity {

    private final static int TAKE_PICTURE = 899;
    private final static int OPEN_GALLERY = 799;
    private String path = "";
    private boolean isChangeHead = false;
    private ImageButton backButton, selectPic;
    private EditText relationText, validaCode, userNameText, pwdText, confirmPwdText;//验证码，手机号，与用户名name保持一致，密码，重复密码。
    private TextView titleView;
    private ImageButton relationButton, phoneButton, pwdButton;//关系，电话，和是否显示密码，前两者弃用
    private Button deleteButton, completeButton, sendValid;//发送验证码
    private Keeper mKeeper;
    private ArrayAdapter relationAdapter;
    private Integer relation;
    private CircleImageView userHeading;
    private TimeCount timeCount;

//    private FrameLayout showPass,showRepass;
    /*@ViewInject(R.id.pwdReBtn)
    private ImageButton hidenPass;//控制显示/隐藏密码*/

    @ViewInject(R.id.cellPhoneET)
    private EditText cellPhone;//手机号,被取代

    @ViewInject(R.id.nickName)
    private EditText nickName;//这是名字，对应Keeper中的昵称

    @ViewInject(R.id.relationField)
    private Spinner relationField;

    private boolean isShowingPass = false;//默认是不显示密码的
    private int editType;//为0时表示是新增，为1是表示修改

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_user);
        setContentViewAddTop(R.layout.activity_add_user);
        ViewUtils.inject(this);
        timeCount = new TimeCount(Constants.VALIDCODE_TOTAL_TIME, Constants.VALIDCODE_APART_TIME);

        mKeeper = (Keeper) getIntent().getSerializableExtra("keep");
        relation = getIntent().getIntExtra("relation", 0);
        userNameText = (EditText) findViewById(R.id.userNameET);
        pwdText = (EditText) findViewById(R.id.pwdET);
        confirmPwdText = (EditText) findViewById(R.id.pwdConfirmET);
        completeButton = (Button) findViewById(R.id.completeBtn);
        validaCode = (EditText) findViewById(R.id.et_valid_code);
        sendValid = (Button) findViewById(R.id.btn_send_valid_code);
        sendValid.setOnClickListener(this);

        selectPic = (ImageButton) findViewById(R.id.imb_select_pic);
        selectPic.setOnClickListener(clickListener);
        userHeading = (CircleImageView) findViewById(R.id.cim_heading);

        relationAdapter = ArrayAdapter.createFromResource(this, relation == 1 ? R.array.relation_friend : R.array.relation_family, R.layout.item_spinner_adduser);
        relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationField.setAdapter(relationAdapter);

        completeButton.setOnClickListener(clickListener);

        moreFunction.setVisibility(View.GONE);
        complet.setVisibility(View.VISIBLE);
        if (mKeeper == null) {//表示是添加监护人
            editType = 0;
            title.setText(R.string.add_user_title);
        } else {//表示修改监护人信息
            editType = 1;
            title.setText(getString(R.string.keeper_personal_msg));
            relationField.setSelection(relationAdapter.getPosition(mKeeper.getAppIdentity()));
            userNameText.setText(mKeeper.getCellPhone());//设置手机号
            pwdText.setText(mKeeper.getPassword());

            nickName.setText(mKeeper.getNickName());//设置昵称
            if (CommUtil.isNotBlank(mKeeper.getPortrait())) {
                ImageLoader.getInstance().displayImage(mKeeper.getPortrait(),userHeading);
            }
            mKeeper.setOldCellPhone(mKeeper.getCellPhone());
        }
        String basePath = Utils.getAvailableStoragePath();
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = basePath + "portrait_tmp.jpg";
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_send_valid_code:
                String phone = userNameText.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    if (Utils.isMobileNO(phone)) {
                        getValid(phone);
                        timeCount.start();
                    } else {
                        Toast.makeText(AddUserActivity.this, getString(R.string.phone_err_), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddUserActivity.this, getString(R.string.phone_not_null), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void passShow() {
        if (!isShowingPass) {//表示是不显示密码
            Toast.makeText(this, getString(R.string.pwd_can_see), Toast.LENGTH_SHORT).show();
            confirmPwdText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            pwdText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isShowingPass = true;
        } else {
            Toast.makeText(this, getString(R.string.pwd_cannot_see), Toast.LENGTH_SHORT).show();
            pwdText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmPwdText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isShowingPass = false;
        }
    }

    @Override
    protected void setComplet() {
        if (editType == 0) {
            addUser(mKeeper);
        } else {
            updataKeeper(mKeeper);
        }
        /*if(mKeeper==null){//表示增加用户
            mKeeper =new Keeper();
            if(packageAndCheck(0)) {
                addUser(mKeeper);
                isLoading =true;
            }
        }else{//表示更新用户信息
            if(packageAndCheck(1)) {
                updataKeeper(mKeeper);
                isLoading =true;
            }
        }*/
    }

    @Override
    protected boolean checkIsOk() {
        return packageAndCheck(editType);
    }

    private boolean packageAndCheck(int editType) { //editType==1，表示为更新
        if (relationField.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.add_user_relation_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        String name = nickName.getText().toString().trim();
        if (CommUtil.isBlank(name)) {
            Toast.makeText(this, R.string.add_user_phone_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        int len = CommUtil.calcASCIILen(name);
        if (len > Constants.MAX_WATCH_TITLE_DISPLAY_LEN) {
            Toast.makeText(AddUserActivity.this, getString(R.string.name_length_limit), Toast.LENGTH_LONG).show();
            return false;
        }
        String phoneNum = userNameText.getText().toString().trim();//检测手机号
        if (CommUtil.isBlank(phoneNum)) {
            Toast.makeText(AddUserActivity.this, R.string.add_user_phone_null, Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Utils.isMobileNO(phoneNum)) {
            Toast.makeText(AddUserActivity.this, getString(R.string.phone_format_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        String validCode = null;
        if (editType == 1) { //更新
            if (!phoneNum.equals(mKeeper.getCellPhone())) { //如果手机号和之前的不一样，则要检测验证码
                validCode = validaCode.getText().toString();
                if (CommUtil.isBlank(validCode)) {
                    Toast.makeText(AddUserActivity.this, getString(R.string.valid_hint), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else { //添加，无论如何都需要验证码
            validCode = validaCode.getText().toString();
            if (CommUtil.isBlank(validCode)) {
                Toast.makeText(AddUserActivity.this, getString(R.string.valid_hint), Toast.LENGTH_SHORT).show();
                return false;
            }
        }


        String pwd = pwdText.getText().toString().trim();//修改信息修改密码不是必须的
        if (editType != 1 && CommUtil.isBlank(pwd)) {
            Toast.makeText(AddUserActivity.this, R.string.add_user_pwd_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        String rePwd = confirmPwdText.getText().toString().trim();
        if (editType != 1 && CommUtil.isBlank(rePwd)) {
            Toast.makeText(AddUserActivity.this, R.string.add_user_pwd_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (CommUtil.isNotBlank(pwd) && !pwd.equals(rePwd)) {
            Toast.makeText(AddUserActivity.this, R.string.add_user_pwd_confirm_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editType == 0) {
            mKeeper = new Keeper();
        }
        mKeeper.setAppUserNum("2");//新增用户 他的appUserNum一定不为1
        mKeeper.setAppIdentity(relationField.getSelectedItem().toString());
        mKeeper.setNickName(name);
        mKeeper.setCellPhone(phoneNum);
        mKeeper.setName(phoneNum);
        mKeeper.setPassword(pwd);
        mKeeper.setValidcode(validCode);
        if (isChangeHead) {
            mKeeper.setPortrait(Utils.encodeBase64File(path));
            FileUtils.delete(path);
        }
        return true;
    }

    /**
     * 点击事件
     */

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    AddUserActivity.this.finish();
                    break;
                case R.id.completeBtn:
                    passShow();
                    String name = nickName.getText().toString().trim();
                    int len = CommUtil.calcASCIILen(name);
                    if (len > Constants.MAX_WATCH_TITLE_DISPLAY_LEN) {
                        Toast.makeText(AddUserActivity.this, getString(R.string.name_length_limit), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (mKeeper == null) {//表示是新增监护人
                        if (relationField.getSelectedItemPosition() == 0) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_relation_null, Toast.LENGTH_SHORT).show();
                        } else if (CommUtil.isBlank(userNameText.getText())) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_phone_null, Toast.LENGTH_SHORT).show();
                        } else if (CommUtil.isBlank(pwdText.getText())) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_pwd_null, Toast.LENGTH_SHORT).show();
                        } else if (!pwdText.getText().toString().trim().equals(confirmPwdText.getText().toString().trim())) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_pwd_confirm_null, Toast.LENGTH_SHORT).show();
                        } else {
                            mKeeper = new Keeper();
                            mKeeper.setAppUserNum("2");
                            mKeeper.setAppIdentity(relationField.getSelectedItem().toString());
                            mKeeper.setName(userNameText.getText().toString().trim());
                            mKeeper.setPassword(pwdText.getText().toString().trim());
                            mKeeper.setCellPhone(cellPhone.getText().toString().trim());
                            mKeeper.setNickName(name);
                            if (isChangeHead) {
                                mKeeper.setPortrait(Utils.encodeBase64File(path));
                                FileUtils.delete(path);
                            }
                            addUser(mKeeper);

                        }
                    } else { //表示修改监护人信息
                        if (relationField.getSelectedItemPosition() == 0) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_relation_null, Toast.LENGTH_SHORT).show();
                        } else if (CommUtil.isBlank(userNameText.getText())) {
                            Toast.makeText(AddUserActivity.this, R.string.add_user_phone_null, Toast.LENGTH_SHORT).show();
                        } else {
                            mKeeper.setAppIdentity(relationField.getSelectedItem().toString());
                            mKeeper.setName(userNameText.getText().toString().trim());
                            mKeeper.setPassword(pwdText.getText().toString().trim());
                            mKeeper.setCellPhone(cellPhone.getText().toString().trim());
                            mKeeper.setNickName(name);
                            if (isChangeHead) {
                                mKeeper.setPortrait(Utils.encodeBase64File(path));
                                FileUtils.delete(path);
                            }
                            updataKeeper(mKeeper);

                        }
                    }
                    AppContext.getEventBus().post(mKeeper, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    break;
                case R.id.deleteBtn:
                    if (mKeeper != null) {
                        mKeeper.delete(AddUserActivity.this, new BctClientCallback() {
                            @Override
                            public void onStart() {
                                WizardAlertDialog.getInstance().showProgressDialog("正在删除", AddUserActivity.this);
                            }

                            @Override
                            public void onFinish() {
                                WizardAlertDialog.getInstance().closeProgressDialog();
                            }

                            @Override
                            public void onSuccess(ResponseData obj) {
                                if (obj.getRetcode() == 1) {
                                    Toast.makeText(AddUserActivity.this, R.string.delete_user_success, Toast.LENGTH_SHORT).show();
                                    AddUserActivity.this.finish();
                                } else {
                                    Toast.makeText(AddUserActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(String message) {
                                CommUtil.showMsgShort(message);
                            }
                        });
                    }
                    break;
                case R.id.imb_select_pic://选择照片
                    showWindow(v);
                    break;
            }
        }
    };

    private void getValid(String phoneStr) {
        User.getValdcode(this, phoneStr, new BctClientCallback() {
            @Override
            public void onStart() {
                WizardAlertDialog.getInstance().showProgressDialog(R.string.send_valid_code, AddUserActivity.this);
            }

            @Override
            public void onFinish() {
                WizardAlertDialog.getInstance().closeProgressDialog();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 0) {
                    timeCount.cancel();
                    timeCount.onFinish();
                    Toast.makeText(AddUserActivity.this, getString(R.string.send_valid_msg_to_target), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddUserActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                CommUtil.showMsgShort(message);
                timeCount.cancel();
                timeCount.onFinish();
            }
        });
    }

    /**
     * 添加设备
     */
    private void addUser(final Keeper keeper) {
        Keeper.add(this, keeper, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AddUserActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
                    Toast.makeText(AddUserActivity.this, R.string.add_user_success, Toast.LENGTH_SHORT).show();
                    Keeper kp=new Keeper(obj.getBody());
                    AppContext.getEventBus().post(kp, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    AppContext.getEventBus().post(kp, Constants.EVENT_TAG_UPDATE_TEL_ACCOUNT);
                    AddUserActivity.this.finish();
                } else {
                    mKeeper = null;
                    resetLoading(false);
                    Toast.makeText(AddUserActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
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

    private void updataKeeper(final Keeper keeper) {
        keeper.update(this, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AddUserActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
                    Toast.makeText(AddUserActivity.this, R.string.update_user_success, Toast.LENGTH_SHORT).show();
                    Keeper kp=new Keeper(obj.getBody());
                    kp.setOldCellPhone(keeper.getOldCellPhone());
                    AppContext.getEventBus().post(kp, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    AppContext.getEventBus().post(kp, Constants.EVENT_TAG_UPDATE_TEL_ACCOUNT);
                    AddUserActivity.this.finish();
                } else {
                    resetLoading(false);
                    Toast.makeText(AddUserActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TAKE_PICTURE:
                startPhotoZoom(Uri.parse(Constants.FILE_SCHEME + path));
                break;
            case OPEN_GALLERY:
                if (data.getData() != null) {
                    startPhotoZoom(data.getData());
                }
                break;
            case 3:
                if (data != null && data.getData()!=null) {
                    InputStream in = null;
                    try {
                        in = getContentResolver().openInputStream(data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        //                    Bundle extras = data.getExtras();
                        //                    Bitmap bitmap = extras.getParcelable("data");
                        userHeading.setImageBitmap(bitmap);

                        Utils.saveImageFile(bitmap, path);
                        isChangeHead = true;
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
                        userHeading.setImageBitmap(bitmap);
                        isChangeHead=true;
                    }else {
                        CommUtil.showMsgShort(getString(R.string.crop_failed));
                    }
                }
                break;
        }
    }

    /**
     * 显示选择照片和拍照对话框
     *
     * @param view
     */
    private void showWindow(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View windown = inflater.inflate(R.layout.select_image_item, null);
        // 创建PopupWindow对象
        final PopupWindow pop = new PopupWindow(windown, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
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
                    intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(Constants.FILE_SCHEME + path));
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
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(Constants.FILE_SCHEME + path));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, 3);
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            sendValid.setText(R.string.get_valid_hint);
            sendValid.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            sendValid.setClickable(false);
            sendValid.setText(millisUntilFinished / 1000 + getResources().getString(R.string.time_minus_hint));
        }
    }

}
