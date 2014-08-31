package com.hanzeli.karlftp;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.hanzeli.fragments.LocalFragment;
import com.hanzeli.fragments.RemoteFragment;
import com.hanzeli.fragments.TransferFragment;

public class TabPagerAdapter extends FragmentPagerAdapter{
    FragmentManager fm;

    public TabPagerAdapter(FragmentManager fm){
        super(fm);
        this.fm=fm;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new LocalFragment();
            case 1:
                return new RemoteFragment();
            case 2:
                return new TransferFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
