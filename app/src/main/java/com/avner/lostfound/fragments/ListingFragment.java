package com.avner.lostfound.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.adapters.LostFoundListAdapter;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.structs.ListFilter;
import com.avner.lostfound.utils.ListFilterUtils;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.software.shell.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ListingFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    // WIDGETS
    private ListView lv_itemList;
    private Spinner sp_locationSpinner;
    private Spinner sp_timeSpinner;

    // INSTANCE VARIABLES
    private View rootView;
    private List<Item> allItems;
    private List<Item> itemsToDisplay;
    private LostFoundListAdapter adapter;
    private boolean isLostFragment;
    private int myLayoutId;
    private String parseClassName; // used as parse queries identifier
    private ListFilter filters = new ListFilter();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMembers();
    }

    /**
     * Initialize this fragment's instance variables, based on its configuration - Lost of Found listing.
     */
    private void initMembers() {
        this.isLostFragment = getArguments().getBoolean("isLostFragment");
        this.myLayoutId = isLostFragment ? R.layout.fragment_lost_list : R.layout.fragment_found_list;
        this.parseClassName = this.isLostFragment ? Constants.ParseObject.PARSE_LOST : Constants.ParseObject.PARSE_FOUND;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initWidgets(inflater, container);
        initItemsList();

        return rootView;
    }

    /**
     * Initialize visible widgets in fragment (add report button, filter spinners, listview).
     *
     * @param inflater
     * @param container
     */
    private void initWidgets(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(this.myLayoutId, container, false);
        lv_itemList = (ListView) rootView.findViewById(R.id.lv_myList);

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.b_add_item);
        button.setOnClickListener(this);

        initTimeSpinner();
        initLocationSpinner();
    }

    /**
     * Initialize the location spinner widget, used for filtering the listings by location.
     */
    private void initLocationSpinner() {
        this.sp_locationSpinner = (Spinner) rootView.findViewById(R.id.s_location_filter);
        ArrayAdapter<CharSequence> locationSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_locations, android.R.layout.simple_spinner_item);

        this.sp_locationSpinner.setAdapter(locationSpinnerAdapter);
        this.sp_locationSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Initialize the time spinner widget, used for filtering the listings by time.
     */
    private void initTimeSpinner() {
        this.sp_timeSpinner = (Spinner) rootView.findViewById(R.id.s_time_filter);
        ArrayAdapter<CharSequence> timeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_time, android.R.layout.simple_spinner_item);

        this.sp_timeSpinner.setAdapter(timeSpinnerAdapter);
        this.sp_timeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Initialize the list-view widget holding all listings for display.
     */
    private void initItemsList() {
        this.allItems = new ArrayList<>();
        this.itemsToDisplay = new ArrayList<>();

        this.adapter = new LostFoundListAdapter(this.allItems, rootView);
        Log.d(Constants.LOST_FOUND_TAG, "raw items list contains " + this.allItems.size() + " items. filtered items: " + this.itemsToDisplay.size());

        this.lv_itemList.setClickable(true);
        this.lv_itemList.setAdapter(this.adapter);
        this.lv_itemList.setOnItemClickListener(this.adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getItemsFromParse();
    }

    /**
     * Get all raw listings for display from Parse. List received is un-filtered, sorted by time of
     * listing creation, and must be filtered using the {@link ListFilterUtils#applyListFilters(List, LostFoundListAdapter, ListFilter, android.location.Location)} method
     */
    private void getItemsFromParse() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(parseClassName);
        query.orderByAscending(Constants.ParseQuery.CREATED_AT); // TODO change to order by most recent
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> itemsList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < itemsList.size(); i++) {
                        convertParseListToItemList(itemsList, allItems);
                    }
                    ListFilterUtils.applyListFilters(allItems, adapter, filters, ((MainActivity) getActivity()).getLastKnownLocation());
                    Log.d(Constants.LOST_FOUND_TAG, "Fetched " + allItems.size() + " items from Parse");
                }
            }
        });

    }

    private void convertParseListToItemList(List<ParseObject> itemsList, List<Item> items) {
        //TODO if not loading all allItems and just adding so remove this.
        items.clear();

        for (ParseObject parseItem : itemsList){
            try {
                if (null != parseItem) {
                    items.add(new Item(parseItem));
                }
                else {
                    Log.d(Constants.LOST_FOUND_TAG, "parseItem was null");
                }
            } catch (NullPointerException e) {
                Log.d(Constants.LOST_FOUND_TAG, "caught NullPointerException when adding an item");
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_add_item:
                Intent intent = new Intent(rootView.getContext(), ReportFormActivity.class);
                intent.putExtra(Constants.ReportForm.IS_LOST_FORM, true); // TODO maybe need to adjust for FOUND?
                intent.putExtra(Constants.ReportForm.IS_EDIT_FORM, false);
                startActivity(intent);
                break;

            default:
                Log.d(Constants.LOST_FOUND_TAG, "WTF?! some unknown onClick...");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.s_location_filter:
                if (this.filters.updateDistanceFilter((String) parent.getItemAtPosition(position))) {
                    ListFilterUtils.applyListFilters(allItems, adapter, filters, ((MainActivity) getActivity()).getLastKnownLocation());
                    Log.d(Constants.LOST_FOUND_TAG, "distance filter updated to " + this.filters.getDistFilter() + " meter radius");
                }

                adapter.notifyDataSetInvalidated();
                break;

            case R.id.s_time_filter:
                if (this.filters.updateTimeFilter((String) parent.getItemAtPosition(position))) {
                    ListFilterUtils.applyListFilters(allItems, adapter, filters, ((MainActivity) getActivity()).getLastKnownLocation());
                    Log.d(Constants.LOST_FOUND_TAG, "time filter updated to " + (this.filters.getTimeFilter() / Constants.MILLI_SECONDS_PER_DAY) + " last days");
                }

                adapter.notifyDataSetInvalidated();
                break;

            default:
                Log.d(Constants.LOST_FOUND_TAG, "WTF?! some unknown selection...");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
