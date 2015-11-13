package com.bct.gpstracker.my;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.my.activity.*;
import com.bct.gpstracker.view.BadgeView;
import com.bct.gpstracker.vo.Session;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * 设置页面
 *
 * @author huangfei
 */
public class MyFragment extends Fragment implements OnClickListener {

    @ViewInject(R.id.dsp_manager)
    private RelativeLayout DSPManager;
    @ViewInject(R.id.msg_center)
    private RelativeLayout userCenter;
    @ViewInject(R.id.remote_control)
    private RelativeLayout remoteControl;
    @ViewInject(R.id.message_notification)
    private RelativeLayout messageNotification;
    @ViewInject(R.id.about_title)
    private RelativeLayout aboutOurs;
    @ViewInject(R.id.logoutBtn)
    private Button logoutBtn;
    @ViewInject(R.id.aboutTitleTV)
    private TextView aboutTitleTV;
    @ViewInject(R.id.aboutTitlePointTV)
    private TextView aboutTitlePointTV;

    private View view;
    private Context mContext;
    private Intent intentMsg;
    private BadgeView badgeView;

    public static MyFragment newInstance() {
        MyFragment newFragment = new MyFragment();
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_my, container, false);
        ViewUtils.inject(this, view);
        initEvent();

        //非管理员，不显示账户管理界面
        if (!"1".equals(Session.getInstance().getUser().getAppUserNum())) {
            userCenter.setVisibility(View.GONE);
        } else {
            userCenter.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void initEvent() {
        /*账户管理*/
        userCenter.setOnClickListener(this);
        /*远程控制*/
        remoteControl.setOnClickListener(this);
        /* 音频管理 */
        DSPManager.setOnClickListener(this);
        /*消息通知*/
        messageNotification.setOnClickListener(this);
        /*关于我们*/
        aboutOurs.setOnClickListener(this);
        /*退出*/
        logoutBtn.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Constants.hasNewVersion) {
            badgeView = new BadgeView(mContext);
            badgeView.setTargetView(aboutTitlePointTV);
            badgeView.setVisibility(View.VISIBLE);
            badgeView.showAsDot();
        } else {
            if (null != badgeView) {
                badgeView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dsp_manager:
                //音频管理
                intentMsg = new Intent(getActivity(), DSPManagerActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.msg_center:
                //账户管理
                intentMsg = new Intent(getActivity(), AccountActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.remote_control:
                //远程控制
                intentMsg = new Intent(getActivity(), RemoteControlAvtivity.class);
                startActivity(intentMsg);
                break;
            case R.id.message_notification:
                //消息通知
                intentMsg = new Intent(getActivity(), MessageSwitchActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.about_title:
                //关于我们
                intentMsg = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intentMsg);
                break;
            case R.id.logoutBtn:
                //退出
                logoutDialog();
                break;

        }
    }


    /**
     * 应用退出
     */
    protected void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_logout_txt);
        builder.setTitle(R.string.msg_notify);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AppContext.isEntered = false;
                logout();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 退出
     */
    private void logout() {
        getActivity().finish();
        MainActivity.getActivity().logout();
//        User.logout(getActivity(), new BctClientCallback() {
//            @Override
//            public void onStart() {
//                WizardAlertDialog.getInstance().showProgressDialog("正在退出", getActivity());
//            }
//
//            @Override
//            public void onFinish() {
//                WizardAlertDialog.getInstance().closeProgressDialog();
//            }
//
//            @Override
//            public void onSuccess(ResponseData obj) {
//                getActivity().finish();
//                MainActivity.getActivity().logout();
//            }
//
//            @Override
//            public void onFailure(String message) {
//                if (CommUtil.isNotBlank(message)) {
//                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//                }
//                onSuccess(null);
//            }
//        });
    }

}
