package com.avner.lostfound;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new MyWorldFragment();
            case 1:
                // Games fragment activity
                return new LostListFragment();
            case 2:
                // Movies fragment activity
                return new FoundListFragment();
            case 3:
                // Games fragment activity
                return new StatsFragment();
            case 4:
                // Movies fragment activity
                return new SettingsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 5;
    }

}
