package com.bct.gpstracker.baby.activity;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

import com.bct.gpstracker.util.FragmentStatePagerAdapter;

/**
 * Created by Admin on 2015/8/28 0028.
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragmentList;

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public FragmentAdapter(FragmentManager manager, List<Fragment> mFragmentList) {
        super(manager);
        this.mFragmentList = mFragmentList;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
