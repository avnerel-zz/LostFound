package com.avner.lostfound.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.avner.lostfound.adapters.TabsPagerAdapter;
import com.avner.lostfound.fragments.ListingFragment;
import com.avner.lostfound.fragments.MyWorldFragment;
import com.avner.lostfound.messaging.ConversationListActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MenuItem.OnMenuItemClickListener {

    private Location lastKnownLocation = null;
    private GoogleApiClient googleApiClient;

    private ViewPager viewPager;
    private ActionBar actionBar;
    private SearchView sv_search;
    private MenuItem mi_search_menu_item;

    private int selectedTabIndex = 0;

    // Tab titles
    private String[] tabsStrings = {
            Constants.TabTexts.MY_WORLD,
            Constants.TabTexts.LOST,
            Constants.TabTexts.FOUND,
            Constants.TabTexts.STATS
    };

    // Tab icons
    private int[] tabsIcons = {
            R.drawable.earth,
            R.drawable.question_mark_red1,
            R.drawable.chequered_flags,
            R.drawable.graph,
    };
    private ActionMode actionMode;
    private List<View> itemInfoViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((LostFoundApplication)getApplication()).setMainActivity(this);
        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(false);
        addTabs();

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                if(actionMode != null){
                    actionMode.finish();
                }
                // on changing the page make respected tab selected
                actionBar.setSelectedNavigationItem(position);
                int prevSelectedTabIndex = selectedTabIndex;
                selectedTabIndex = position;

                Log.d(Constants.LOST_FOUND_TAG, String.format("changed from tab %d (%slisting) to %d (%slisting)",
                        prevSelectedTabIndex, isListingFragment(prevSelectedTabIndex) ? "" : "non-",
                        selectedTabIndex, isListingFragment(selectedTabIndex) ? "" : "non-"));

                if (isListingFragment(position)) {
                    // switched to a listing tab - enable search view
                    showSearchView();

                    if (isListingFragment(prevSelectedTabIndex)) {
                        ((ListingFragment)getFragmentAt(prevSelectedTabIndex)).saveSearchViewState();
                    }

                    ((ListingFragment)getCurrentFragment()).restoreSearchViewState();
                }
                else {
                    // switched to a non-listing tab - disable search view
                    hideSearchView();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}

            @Override
            public void onPageScrollStateChanged(int arg0) {}
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void addTabs() {
        // Adding Tabs with icons
        for (int i = 0; i < tabsStrings.length; ++i) {
            Tab tab = actionBar.newTab()
                    .setCustomView(R.layout.action_bar_tab_layout)
                    .setTabListener(this);

            ImageView imageView = (ImageView) tab.getCustomView().findViewById(R.id.iv_tabIcon);
            imageView.setImageResource(tabsIcons[i]);

            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tv_tabText);
            textView.setText(tabsStrings[i]);

            actionBar.addTab(tab);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((LostFoundApplication)getApplication()).setMainActivity(null);
    }

    private void showSearchView() {
        if (null==sv_search){
            return;
        }
        sv_search.setVisibility(View.VISIBLE);
        sv_search.setEnabled(true);
        mi_search_menu_item.setVisible(true);
        mi_search_menu_item.setEnabled(true);
    }

    private void hideSearchView() {
        if (null==sv_search){
            return;
        }
        sv_search.setVisibility(View.INVISIBLE);
        sv_search.setEnabled(false);
        mi_search_menu_item.setVisible(false);
        mi_search_menu_item.setEnabled(false);
        mi_search_menu_item.collapseActionView();
    }

    private boolean isListingFragment(int position) {
        final TextView tv = (TextView) actionBar.getTabAt(position)
                .getCustomView()
                .findViewById(R.id.tv_tabText);

        return (tv.getText().equals(Constants.TabTexts.FOUND) || tv.getText().equals(Constants.TabTexts.LOST));
    }

    public Location getLastKnownLocation() {
        return new Location(this.lastKnownLocation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        initSearchBar(menu);

        if (!isListingFragment(actionBar.getSelectedNavigationIndex())) {
            hideSearchView();
        }

        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setOnMenuItemClickListener(this);

        MenuItem messaging = menu.findItem(R.id.messaging);
        messaging.setOnMenuItemClickListener(this);

        return true;
    }

    private void initSearchBar(Menu menu) {
        this.mi_search_menu_item = menu.findItem(R.id.search);
        this.sv_search = (SearchView) menu.findItem(R.id.search).getActionView();
        this.sv_search.setSubmitButtonEnabled(true);

        initSearchBarExpansionHandling();
        initSearchBarQueryHandling();
    }

    private void initSearchBarQueryHandling() {
        this.sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(Constants.LOST_FOUND_TAG, String.format("searched for '%s'", query));

                if (!isListingFragment(selectedTabIndex)) {
                    Log.d(Constants.LOST_FOUND_TAG, "WTF? somehow submitted search in a non-listing fragment. fragment index: " + selectedTabIndex);
                    return false;
                }

                ListingFragment fragment = (ListingFragment) getCurrentFragment();
                fragment.searchPhrase(getApplicationContext(), query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if ("".equals(newText) && isListingFragment(selectedTabIndex)) {
                    ListingFragment fragment = (ListingFragment) getCurrentFragment();
                    fragment.searchPhrase(getApplicationContext(), newText);
                }
                return true;
            }
        });
    }

    private void initSearchBarExpansionHandling() {
        this.mi_search_menu_item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(Constants.LOST_FOUND_TAG, "search bar expanding...");
                int selectedFragmentIndex = actionBar.getSelectedNavigationIndex();

                if (isListingFragment(selectedFragmentIndex)) {
                    ((ListingFragment) getFragmentAt(selectedFragmentIndex)).searchBarExpanded();
                }

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(Constants.LOST_FOUND_TAG, "search bar collapsing...");
                int selectedFragmentIndex = actionBar.getSelectedNavigationIndex();

                if (isListingFragment(selectedFragmentIndex)) {
                    ((ListingFragment) getFragmentAt(selectedFragmentIndex)).searchBarCollapsed();
                }
                return true;
            }
        });
    }

    public void updateLocalDataInFragments(){

        Log.d(Constants.LOST_FOUND_TAG, "updating local data store in fragments");
        ((MyWorldFragment)getFragmentAt(0)).updateData();
        ((ListingFragment)getFragmentAt(1)).updateData();
        ((ListingFragment)getFragmentAt(2)).updateData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_CODE_REPORT_FORM && resultCode == RESULT_OK){

            updateLocalDataInFragments();
        }
        if(requestCode == Constants.REQUEST_CODE_SETTINGS && resultCode == Constants.RESULT_CODE_LOGOUT){

            ParseUser.logOut();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private Fragment getCurrentFragment() {
        return getFragmentAt(this.selectedTabIndex);
    }

    private Fragment getFragmentAt(int position) {
        return ((TabsPagerAdapter)this.viewPager.getAdapter()).getFragment(position);
    }

    public SearchView getSearchView() {
        return this.sv_search;
    }

    public MenuItem getSearchViewMenuItem() {
        return this.mi_search_menu_item;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onStart() {
        super.onStart();
        this.googleApiClient.connect();
        setInitLocation();

    }

    private void setInitLocation() {
        if (null == this.lastKnownLocation) {
            Location mockLocation = new Location(LocationManager.NETWORK_PROVIDER);
            mockLocation.setLatitude(90.0);
            mockLocation.setLongitude(0.0);
            mockLocation.setAltitude(0.0);
            mockLocation.setAccuracy(50.0f);

            this.lastKnownLocation = mockLocation;
            Log.d(Constants.LOST_FOUND_TAG, "set init mock location: " + mockLocation.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateCurrentLocation();
    }

    private void updateCurrentLocation() {
        Log.d(Constants.LOST_FOUND_TAG, "Updating lastKnownLocation...");
        Location location = LocationServices.FusedLocationApi.getLastLocation(this.googleApiClient);

        if (null != location) { // got a location
            lastKnownLocation = location;
            Log.d(Constants.LOST_FOUND_TAG, "got last location! " + location.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivityForResult(intent,Constants.REQUEST_CODE_SETTINGS);
                break;
            case R.id.messaging:
                Intent conversationIntent = new Intent(this,ConversationListActivity.class);
                startActivity(conversationIntent);
                break;
        }
        return false;
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }
}
