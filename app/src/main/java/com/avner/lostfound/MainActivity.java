package com.avner.lostfound;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabsStrings = { "My World", "Lost", "Found", "Stats" };

    // Tab icons
    private int[] tabsIcons = {
            R.drawable.earth,
            R.drawable.question_mark_red1,
            R.drawable.chequered_flags,
            R.drawable.graph,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        actionBar.setHomeButtonEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayOptions(0);



        // Adding Tabs with icons
        for (int i = 0; i < tabsStrings.length; ++i) {
            Tab tab = actionBar.newTab()
                    .setCustomView(R.layout.action_bar_tab_layout)
                    .setTabListener(this);

            ImageView imageView = (ImageView) tab.getCustomView().findViewById(R.id.iv_tabIcon);
            imageView.setImageResource(tabsIcons[i]);
//            imageView.setPadding(0, 0, 0, 0);
//            imageView.setMaxWidth(20);

            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tv_tabText);
            textView.setText(tabsStrings[i]);
//            textView.setPadding(0, 0, 0, 0);
//            textView.setMaxWidth(20);

//            tab.getCustomView().setPadding(0, 0, 0, 0);


            actionBar.addTab(tab);

//            actionBar.addTab(actionBar.newTab()
//                    .setCustomView(R.layout.action_bar_tab_layout)
//                    .setText(tabsStrings[i])
//                    .setIcon(tabsIcons[i])
//                    .setTabListener(this));
        }

        setTabsWidth();

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void setTabsWidth() {
//        final Tab tab = actionBar.getTabAt(0);
//        final View text1 = tab.
//        final View tabView = tab.getCustomView();
//        tabView.setPadding(0, 0, 0, 0);
//        final View tabContainerView = (View) tabView.getParent();
//        tabContainerView.setPadding(0, 0, 0, 0);

    }


    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

}
