package com.avner.lostfound.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.avner.lostfound.fragments.ListingFragment;
import com.avner.lostfound.fragments.MyWorldFragment;
import com.avner.lostfound.fragments.StatsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {
            new MyWorldFragment(),
            new ListingFragment(),  // lost, must update argument "isLostFragment" to 'true'
            new ListingFragment(), // found, must update argument "isLostFragment" to 'false'
            new StatsFragment(),
    };

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);

        Bundle lostFragmentArgs = new Bundle();
        lostFragmentArgs.putBoolean("isLostFragment", true);
        fragments[1].setArguments(lostFragmentArgs);

        Bundle foundFragmentArgs = new Bundle();
        foundFragmentArgs.putBoolean("isLostFragment", false);
        fragments[2].setArguments(foundFragmentArgs);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int index) {
        return fragments[index];
    }

    public Fragment getFragment(int position) {
        if (position < 0 || position > fragments.length) {
            throw new IllegalArgumentException("invalid fragment index " + position);
        }

        return this.fragments[position];
    }


    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return fragments.length;
    }

}
