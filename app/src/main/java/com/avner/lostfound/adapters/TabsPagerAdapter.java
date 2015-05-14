package com.avner.lostfound.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.avner.lostfound.fragments.FoundListFragment;
import com.avner.lostfound.fragments.LostListFragment;
import com.avner.lostfound.fragments.MyWorldFragment;
import com.avner.lostfound.fragments.StatsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {
            new MyWorldFragment(),
            new LostListFragment(),
            new FoundListFragment(),
            new StatsFragment(),
//            new SettingsFragment()
    };

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int index) {

        return fragments[index];
//        switch (index) {
//            case 0:
//                // Top Rated fragment activity
//                return new MyWorldFragment();
//            case 1:
//                // Games fragment activity
//                return new LostListFragment();
//            case 2:
//                // Movies fragment activity
//                return new FoundListFragment();
//            case 3:
//                // Games fragment activity
//                return new StatsFragment();
//            case 4:
//                // Movies fragment activity
//                return new SettingsFragment();
//        }
//
//        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return fragments.length;
    }

}
