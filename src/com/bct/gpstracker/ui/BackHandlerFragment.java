package com.bct.gpstracker.ui;

import android.app.Activity;
import android.app.Fragment;

import com.bct.gpstracker.inter.BackHandledInterface;

/**
 * Created by Admin on 2015/8/28 0028.
 */
public abstract class BackHandlerFragment extends Fragment {

    protected BackHandledInterface mBackHandledInterface;

    /**
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
     */
    public abstract boolean onBackPressed();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof BackHandledInterface)) {
            throw new IllegalStateException("BaseHandlerFragment所在的Activity必须实现BackHandledInterface接口");
        }
        mBackHandledInterface = (BackHandledInterface) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        //告诉FragmentActivity，当前Fragment在栈顶
        mBackHandledInterface.setSelectedFragment(this);
    }

}

