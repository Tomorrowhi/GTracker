package com.bct.gpstracker.msg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bct.gpstracker.R;
import com.bct.gpstracker.common.Constants;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;


/**
 * 消息界面
 */
public class MsgMainFragment extends Fragment {
    FragmentManager fragmentManager;
    Fragment msgFragment;
    Fragment contactFragment;
    Fragment currFragment;

    public static Map<String, Uri> mEmoticonsUri = new HashMap<String, Uri>();
    public static List<String> mEmoticonsNewGif = new ArrayList<>();

    @ViewInject(R.id.mainMsg)
    TextView mainMsg;

    @ViewInject(R.id.mainContact)
    TextView mainContact;

    public static MsgMainFragment newInstance() {
        return new MsgMainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg_main, null);
        ViewUtils.inject(this, view);


        fragmentManager = getFragmentManager();
        msgFragment = MsgFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.msg_fragment_place_holder, msgFragment).commit();
        this.currFragment = msgFragment;

        //加载本地表情
        loadLocalEmoticon();
        return view;
    }

    /**
     * 加载表情,加载本地指定文件夹下的表情，如果存在，则加载，不存在则不加载。
     */
    private void loadLocalEmoticon() {
        Log.d(Constants.TAG, "这里要是加载本地表情？");
        File gifFile = new File(getActivity().getFilesDir() + "/emoji/gif/");
        File[] files = gifFile.listFiles();
        if (files != null) {
            mEmoticonsNewGif.clear();
            mEmoticonsUri.clear();
            for (int i = 0; i < files.length; i++) {
                //判断是否为文件夹
                if (!files[i].isDirectory()) {
                    String mFileName = files[i].getName();
                    Pattern p = Pattern.compile("zgif[0-9][0-9].gif");
                    Matcher m = p.matcher(mFileName);
                    if (m.matches()) {
                        //将String类型的路径转换为Uri
                        mFileName = "[" + mFileName + "]";
                        mEmoticonsNewGif.add(mFileName);
                        mEmoticonsUri.put(mFileName, Uri.fromFile(files[i]));
                    }

                }
            }
            Log.d(Constants.TAG, "表情的条数：mEmoticonsNewGif:" + mEmoticonsNewGif.size());
            Log.d(Constants.TAG, "表情的条数：mEmoticonsUri:" + mEmoticonsUri.size());
        }

    }

    @OnClick(R.id.mainMsg)
    private void msgOnClick(View v) {
        mainMsg.setBackgroundResource(R.drawable.shape_circle_rect_msg_focus_left);
        mainMsg.setTextColor(getResources().getColor(R.color.link_blue));
        mainContact.setBackgroundResource(R.drawable.shape_circle_rect_msg_unfocus_right);
        mainContact.setTextColor(getResources().getColor(R.color.text_color_white));
        if (msgFragment == null) {
            msgFragment = MsgFragment.newInstance();
        }
        switchFragment(currFragment, msgFragment);
    }

    @OnClick(R.id.mainContact)
    private void contactOnClick(View v) {
        mainMsg.setBackgroundResource(R.drawable.shape_circle_rect_msg_unfocus_left);
        mainMsg.setTextColor(getResources().getColor(R.color.text_color_white));
        mainContact.setBackgroundResource(R.drawable.shape_circle_rect_msg_focus_right);
        mainContact.setTextColor(getResources().getColor(R.color.link_blue));
        if (contactFragment == null) {
            contactFragment = ContactFragment.newInstance();
        }
        switchFragment(currFragment, contactFragment);
    }

    public void switchFragment(Fragment from, Fragment to) {
        if (from.equals(to)) return;
        if (to == null) return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (!to.isAdded()) {
            transaction.hide(from).add(R.id.msg_fragment_place_holder, to).commit();
        } else {
            transaction.hide(from).show(to).commit();
        }
        currFragment = to;
    }
}
