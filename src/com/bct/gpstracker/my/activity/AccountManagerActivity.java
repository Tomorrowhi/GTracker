package com.bct.gpstracker.my.activity;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.CommTitleActivity;
import com.bct.gpstracker.common.Constants;
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
 * Created by Administrator on 15-9-9.
 * 管理员的个人信息界面
 */
public class AccountManagerActivity extends CommTitleActivity {

    @ViewInject(R.id.cim_mana_heading)
    private CircleImageView uHead;//头像
    @ViewInject(R.id.et_mana_name)
    private EditText name;  //名字
    @ViewInject(R.id.et_mana_phone)
    private EditText phone; //电话号码
    @ViewInject(R.id.imb_mana_pic)
    private ImageButton picSele; //选择图片
    @ViewInject(R.id.sp_mana_rela)
    private Spinner relation;//关系
    @ViewInject(R.id.et_mana_code)
    private EditText etManaCode;//验证码
    @ViewInject(R.id.btn_mana_send_valid_code)
    private Button getValidCode;//获取验证码
    @ViewInject(R.id.et_mana_pwdET)
    private EditText pwdET;//密码
    @ViewInject(R.id.et_mana_pwdConfirmET)
    private EditText pwdConfirmET;//再次输入密码
    @ViewInject(R.id.bt_mana_change_phone)
    private Button btChangePhone;//更改手机号
    @ViewInject(R.id.bt_mana_cancel_change_phone)
    private Button btCancelChangePhone;//取消更改手机号
    @ViewInject(R.id.ll_mana_valid_code)
    private LinearLayout llValidCode;

    private Keeper manaKeeper;//管理员

    private ArrayAdapter mAdapter;
    private Context mContext = AccountManagerActivity.this;
    private TimeCount timeCount;

    private final static int TAKE_PICTURE = 899;
    private final static int OPEN_GALLERY = 799;
    private final static int CUT_PICTURE = 900;

    protected String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAddTop(R.layout.activity_manager_msg);
        ViewUtils.inject(this);
        timeCount = new TimeCount(Constants.VALIDCODE_TOTAL_TIME, Constants.VALIDCODE_APART_TIME);
        getData();
        initView();
        initEvent();
        initFillData();

        String basePath = Utils.getAvailableStoragePath();
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = basePath + "portrait_tmp.jpg";
    }

    /**
     * 获取从其他界面传递过来的数据
     */
    private void getData() {
        Intent intent = getIntent();
        manaKeeper = (Keeper) intent.getSerializableExtra("keep");
        mAdapter = ArrayAdapter.createFromResource(this, R.array.relation_family, R.layout.item_spinner_adduser);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    /**
     * 初始化空间
     */
    private void initView() {
        relation.setAdapter(mAdapter);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        phone.setFocusable(false);
        complet.setOnClickListener(this);
        picSele.setOnClickListener(this);
        getValidCode.setOnClickListener(this);
        btChangePhone.setOnClickListener(this);
        btCancelChangePhone.setOnClickListener(this);
    }

    /**
     * 根据需要显示隐藏控件，并完成数据的设置,以及完成设置回调接口
     */
    private void initFillData() {
        complet.setVisibility(View.VISIBLE);
        moreFunction.setVisibility(View.GONE);
        title.setText(getString(R.string.rel_manager_msg));
        if (CommUtil.isNotBlank(manaKeeper.getPortrait())) {
            ImageLoader.getInstance().displayImage(manaKeeper.getPortrait(),uHead);
        }
        //relation.setText(manaKeeper.getAppIdentity());
        relation.setSelection(mAdapter.getPosition(manaKeeper.getAppIdentity()));
        name.setText(manaKeeper.getNickName());
        phone.setText(TextUtils.isEmpty(manaKeeper.getCellPhone()) ? manaKeeper.getName() : manaKeeper.getCellPhone());
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.bt_mana_cancel_change_phone:
                btCancelChangePhone.setVisibility(View.GONE);
                llValidCode.setVisibility(View.GONE);
                btChangePhone.setVisibility(View.VISIBLE);
                phone.setText(manaKeeper.getCellPhone());
                phone.setFocusable(false);
                phone.setFocusableInTouchMode(false);
                break;
            case R.id.bt_mana_change_phone:
                btChangePhone.setVisibility(View.GONE);
                btCancelChangePhone.setVisibility(View.VISIBLE);
                llValidCode.setVisibility(View.VISIBLE);
                phone.setFocusable(true);
                phone.setFocusableInTouchMode(true);
                break;

            case R.id.btn_mana_send_valid_code:
                String mPhone = phone.getText().toString();
                if (!TextUtils.isEmpty(mPhone)) {
                    if (Utils.isMobileNO(mPhone)) {
                        getValid(mPhone);
                        timeCount.start();
                    } else {
                        Toast.makeText(mContext, getString(R.string.phone_err_), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.phone_not_null), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imb_mana_pic://选择照片
                /*setIsChangeHead(true);
                super.onClick(v);*/
                showWindow(v);
                break;
            default:
                /*setIsChangeHead(false);
                super.onClick(v);*/
                break;
        }
        super.onClick(v);
    }

    private boolean changedHead = false;

    @Override
    protected void setComplet() {
        manaKeeper.update(this, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AccountManagerActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
                    Toast.makeText(AccountManagerActivity.this, R.string.update_user_admin_success, Toast.LENGTH_SHORT).show();
                    Keeper keeper = new Keeper(obj.getBody());
                    AppContext.getEventBus().post(keeper, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    AccountManagerActivity.this.finish();
                } else {
                    resetLoading(false);
                    Toast.makeText(AccountManagerActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                resetLoading(false);
                CommUtil.hideProcessing();
                if(CommUtil.isNotBlank(message)) {
                    Toast.makeText(AccountManagerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected boolean checkIsOk() {
        return packageData();
    }

    private boolean packageData() {

        String identity = relation.getSelectedItem().toString();
        if (relation.getSelectedItemPosition()<1) {
            Toast.makeText(this, getString(R.string.rel_relation_not_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        String nickName = name.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(this, getString(R.string.add_user_phone_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        String phoneNum = phone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            Toast.makeText(this, getString(R.string.phone_not_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Utils.isMobileNO(phoneNum)) {
            Toast.makeText(this, getString(R.string.phone_format_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        String pwd = pwdET.getText().toString();
        String pwdConfirm = pwdConfirmET.getText().toString();
        if (!phoneNum.equals(manaKeeper.getCellPhone())) { //如果手机号和之前的不一样，则要检测验证码
            String validCode = etManaCode.getText().toString();
            if (CommUtil.isBlank(validCode)) {
                Toast.makeText(mContext, getString(R.string.valid_hint), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (CommUtil.isBlank(pwd)) {
                Toast.makeText(mContext, R.string.add_user_pwd_null, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (CommUtil.isBlank(pwdConfirm) || !pwdConfirm.equals(pwd)) {
                Toast.makeText(mContext, R.string.add_user_pwd_confirm_null, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (!"".equals(pwd) || !"".equals(pwdConfirm)) {
                //修改密码,进行如下校验
                if (CommUtil.isBlank(pwd)) {
                    Toast.makeText(mContext, R.string.add_user_pwd_null, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (CommUtil.isBlank(pwdConfirm) || !pwdConfirm.equals(pwd)) {
                    Toast.makeText(mContext, R.string.add_user_pwd_confirm_null, Toast.LENGTH_SHORT).show();
                    return false;
                }
                manaKeeper.setPassword(pwd);
            }
        }


        manaKeeper.setNickName(name.getText().toString());
        manaKeeper.setCellPhone(phoneNum);
        manaKeeper.setName(phoneNum);
        manaKeeper.setAppIdentity(identity);
        if (changedHead) {
            manaKeeper.setPortrait(Utils.encodeBase64File(path));
            FileUtils.delete(path);
        }
        return true;
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
        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
//					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
//					imageUri = Uri.fromFile(photo);
//					startActivityForResult(intent, TAKE_PICTURE);
                    Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //下面这句指定调用相机拍照后的照片存储的路径
                    intent1.putExtra(MediaStore.EXTRA_OUTPUT, Constants.FILE_SCHEME+path);
                    startActivityForResult(intent1, TAKE_PICTURE);
                }
            }
        });
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, OPEN_GALLERY);
//					Intent intent = new Intent();
//					intent.setType("image/*");
//					intent.setAction(Intent.ACTION_GET_CONTENT);
//					startActivityForResult(Intent.createChooser(intent, "Choose Picture"),OPEN_GALLERY);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {//扫描二维码返回的值
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            phone.setText(scanResult);
        } else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            startPhotoZoom(Uri.parse(Constants.FILE_SCHEME + path));
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
                    uHead.setImageBitmap(bitmap);
                    changedHead = true;
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
                    uHead.setImageBitmap(bitmap);
                    changedHead=true;
                }else {
                    CommUtil.showMsgShort(getString(R.string.crop_failed));
                }
            }
        }
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


    private void getValid(String phoneStr) {
        User.getValdcode(this, phoneStr, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AccountManagerActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {
                CommUtil.hideProcessing();
            }

            @Override
            public void onSuccess(ResponseData obj) {
                if (obj.getRetcode() == 0) {
                    timeCount.cancel();
                    timeCount.onFinish();
                    Toast.makeText(mContext, getString(R.string.send_valid_msg_to_target), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, obj.getMsg(), Toast.LENGTH_SHORT).show();
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

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            getValidCode.setText(R.string.get_valid_hint);
            getValidCode.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            getValidCode.setClickable(false);
            getValidCode.setText(millisUntilFinished / 1000 + getResources().getString(R.string.time_minus_hint));
        }
    }
}
