package com.bct.gpstracker.my.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.R;
import com.bct.gpstracker.base.CommTitleActivity;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.inter.BctClientCallback;
import com.bct.gpstracker.pojo.Keeper;
import com.bct.gpstracker.pojo.ResponseData;
import com.bct.gpstracker.pojo.TermFriend;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Admin on 2015/9/9 0009.
 */
public class AddGoodFriendsActivity extends CommTitleActivity {

    @ViewInject(R.id.friends_name_et)
    private EditText friendsName;
    @ViewInject(R.id.friends_phone_et)
    private EditText friendsPhone;
//    @ViewInject(R.id.friends_relationship_et)
//    private EditText friendsRelationship;
    @ViewInject(R.id.imb_select_pic)
    private ImageButton imbSelectPic;
    @ViewInject(R.id.friends_relationship_spi)
    private Spinner friendsRelationshipSpi;

    private TermFriend mFriends;
    private Keeper mKeeper;
    private ArrayAdapter relationAdapter;

    private String name;
    private String phone;
    private String relationship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载内容布局
        setContentViewAddTop(R.layout.activity_add_goog_friends);
        complet.setVisibility(View.VISIBLE);
        moreFunction.setVisibility(View.GONE);
        ViewUtils.inject(this);
        mFriends = new TermFriend();

        //下拉列表数据处理
        relationAdapter = ArrayAdapter.createFromResource(this, R.array.relation_friend, R.layout.item_spinner_friend);
        relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendsRelationshipSpi.setAdapter(relationAdapter);

        initData();
        initEvent();
    }

    private void initData() {
        mKeeper = (Keeper) getIntent().getSerializableExtra("keep");
        mFriends = (TermFriend) getIntent().getSerializableExtra("friend");
        if (mKeeper != null) {
            /*friendsName.setText(CommUtil.isBlank(mKeeper.getNickName())?"未设置昵称":mKeeper.getNickName());
            friendsPhone.setText(CommUtil.isBlank(mKeeper.getCellPhone())?CommUtil.isBlank(mKeeper.getName())?"未设置电话号码":mKeeper.getName().substring(0,mKeeper.getName().length()-3):mKeeper.getCellPhone());
            friendsRelationship.setText(CommUtil.isBlank(mKeeper.getAppIdentity())?"未选定关系":mKeeper.getAppIdentity());*/
            Log.i(Constants.TAG,mKeeper.toString());
            title.setText(getString(R.string.add_friends_message));
            friendsRelationshipSpi.setSelection(relationAdapter.getPosition(mKeeper.getAppIdentity()));
            friendsName.setText(mKeeper.getNickName());
            friendsPhone.setText(mKeeper.getCellPhone());
            //friendsRelationship.setText(mKeeper.getAppIdentity());
        } else if(mFriends!=null){
            Log.i(Constants.TAG,mFriends.toString());
            title.setText(getString(R.string.add_friends_message));
            friendsRelationshipSpi.setSelection(relationAdapter.getPosition(mFriends.getRelationship()));
            friendsName.setText(mFriends.getName());
            friendsPhone.setText(mFriends.getCellPhone());
            mFriends.setOldCellPhone(mFriends.getCellPhone());
        }else{
            title.setText(getString(R.string.add_friend));
        }
    }

    private void initEvent() {
        //complet.setOnClickListener(this);
        imbSelectPic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.imb_select_pic:
                break;
            /*case R.id.btn_complet:
                //完成

                break;*/
            default:

                break;
        }

    }

    @Override
    protected void setComplet() {
        if(mFriends==null){
            mFriends =new TermFriend();
            mFriends.setName(name);
            mFriends.setCellPhone(phone);
            mFriends.setRelationship(relationship);
            mFriends.setId(0);
            addFriendsMessage(0);
        }else{
            mFriends.setName(name);
            mFriends.setCellPhone(phone);
            mFriends.setRelationship(relationship);
            addFriendsMessage(mFriends.getId());
        }
        AppContext.getEventBus().post(mFriends, Constants.EVENT_TAG_UPDATE_ACCOUNT);
    }

    @Override
    protected boolean checkIsOk() {
        name = friendsName.getText().toString();
        phone = friendsPhone.getText().toString().trim();
        relationship = friendsRelationshipSpi.getSelectedItem().toString();

        if(relationship.equals(getString(R.string.pl_select))){
            Toast.makeText(this,getString(R.string.rel_relation_not_null),Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.please_input_name, Toast.LENGTH_SHORT).show();
            return false;
        }else if(CommUtil.calcASCIILen(name)>Constants.MAX_WATCH_TITLE_DISPLAY_LEN){
            Toast.makeText(this,getString(R.string.name_length_limit),Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.please_input_phone, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(relationship)) {
            Toast.makeText(this, R.string.please_input_relationship, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!Utils.isMobileNO(phone)){
            Toast.makeText(this,getString(R.string.phone_format_error),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 添加好友信息 id为0表示增加用户  否则为修改
     @param id
     */
    private void addFriendsMessage(final int id) {


        Log.i(Constants.TAG,mFriends.toString());
        mFriends.friendsAddAndEdit(this, new BctClientCallback() {
            @Override
            public void onStart() {
                CommUtil.showProcessing(AddGoodFriendsActivity.this.rootView,true,true);
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(ResponseData obj) {
                CommUtil.hideProcessing();
                if (obj.getRetcode() == 1) {
                    Toast.makeText(AddGoodFriendsActivity.this, id==0?getString(R.string.add_friend_succ):getString(R.string.updata_freind_msg_succ), Toast.LENGTH_SHORT).show();
                    TermFriend fd=new TermFriend(obj.getBody());
                    fd.setOldCellPhone(mFriends.getOldCellPhone());
                    AppContext.getEventBus().post(fd, Constants.EVENT_TAG_UPDATE_ACCOUNT);
                    AppContext.getEventBus().post(fd, Constants.EVENT_TAG_UPDATE_TEL_ACCOUNT);
                    AddGoodFriendsActivity.this.finish();
                } else {
                    Toast.makeText(AddGoodFriendsActivity.this, obj.getMsg(), Toast.LENGTH_SHORT).show();
                    resetLoading(false);
                }
            }

            @Override
            public void onFailure(String message) {
                resetLoading(false);
                CommUtil.hideProcessing();
                CommUtil.showMsgShort(message);
            }
        });

    }
    private void setBackData(TermFriend friends){
        Intent intent =new Intent();
        intent.putExtra("friend",friends);
        setResult(RESULT_OK,intent);
    }

}
