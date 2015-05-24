package com.avner.lostfound.fragments;

import android.content.Intent;
import android.location.Location;
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
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.software.shell.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LostListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ListView lv_itemList;
    private View rootView;
    private List<Item> allItems;
    private List<Item> itemsToDisplay;
    private LostFoundListAdapter adapter;
    private Spinner sp_locationSpinner;
    private Spinner sp_timeSpinner;

    private long filter_dist = Constants.NO_DISTANCE_FILTER;
    private long filter_time = Constants.NO_TIME_FILTER;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_lost_list, container, false);

        lv_itemList = (ListView) rootView.findViewById(R.id.lv_myList);

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.b_add_item);
        button.setOnClickListener(this);

        initTimeSpinner();
        initLocationSpinner();
        initItemsList();

        return rootView;
	}

    private void initLocationSpinner() {
        this.sp_locationSpinner = (Spinner) rootView.findViewById(R.id.s_location_filter);
        ArrayAdapter<CharSequence> locationSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_locations, android.R.layout.simple_spinner_item);
        this.sp_locationSpinner.setAdapter(locationSpinnerAdapter);
        this.sp_locationSpinner.setOnItemSelectedListener(this);
    }

    private void initTimeSpinner() {
        this.sp_timeSpinner = (Spinner) rootView.findViewById(R.id.s_time_filter);
        ArrayAdapter<CharSequence> timeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_time, android.R.layout.simple_spinner_item);
        this.sp_timeSpinner.setAdapter(timeSpinnerAdapter);
        this.sp_timeSpinner.setOnItemSelectedListener(this);
    }

    private void initItemsList() {
        this.allItems = new ArrayList<>();
        this.itemsToDisplay = new ArrayList<>();

        this.adapter = new LostFoundListAdapter(this.allItems, rootView);
        Log.d(Constants.LOST_FOUND_TAG, "raw items list contains " + this.allItems.size() + " items. filtered items: " + this.itemsToDisplay.size());

        lv_itemList.setClickable(true);
        lv_itemList.setAdapter(adapter);
        lv_itemList.setOnItemClickListener(adapter);
    }

    private void applyListFilters() {
        List<Item> filteredList = new ArrayList(this.allItems);

        applyDistanceFilter(filteredList);
        applyTimeFilter(filteredList);

        this.itemsToDisplay = filteredList;
        Log.d(Constants.LOST_FOUND_TAG, "filtering re-applied, notifying adapter. raw list size: " + this.allItems.size() + " filtered size: " + filteredList.size());
        adapter.setList(this.itemsToDisplay);
        adapter.notifyDataSetChanged();
    }

    private void applyDistanceFilter(List<Item> filteredList) {
        if (this.filter_dist == Constants.NO_DISTANCE_FILTER) { // no distance filter defined - skip
            Log.d(Constants.LOST_FOUND_TAG, "distance unfiltered - not applying distance filter");
            return;
        }

        Location lastKnownLocation = ((MainActivity)getActivity()).getLastKnownLocation();
        if (null == lastKnownLocation) { // unknown location - skip distance filtering
            Log.d(Constants.LOST_FOUND_TAG, "lastKnownLocation is null - not applying distance filter");
            return;
        }

        Iterator<Item> it = filteredList.iterator();
        while (it.hasNext()) {
            Item item = it.next(); // advance iterator
            Location itemLocation = item.getLocation();
            if (null == itemLocation || lastKnownLocation.distanceTo(itemLocation) > this.filter_dist) {
                it.remove();
            }
        }
    }

    private void applyTimeFilter(List<Item> filteredList) {
        if (this.filter_time == Constants.NO_TIME_FILTER) return; // no time filter defined - skip

        long lowerTimeBound = System.currentTimeMillis() - this.filter_time;

        Iterator<Item> it = filteredList.iterator();
        while (it.hasNext()) {
            Item item = it.next(); // advance iterator
            if (item.getTime().getTimeInMillis() < lowerTimeBound) {
                it.remove();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getItemsFromParse();
        applyListFilters();
        adapter.notifyDataSetChanged();
    }

    private void getItemsFromParse() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        query.orderByAscending(Constants.ParseQuery.CREATED_AT); // TODO change to order by most recent
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> itemsList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < itemsList.size(); i++) {
                        convertParseListToItemList(itemsList, allItems);
                    }
                }
            }
        });

        this.itemsToDisplay = this.allItems; // TODO remove
        adapter.notifyDataSetChanged(); // TODO remove

        Log.d(Constants.LOST_FOUND_TAG, "Fetched " + this.allItems.size() + " items from Parse");
    }

    private void convertParseListToItemList(List<ParseObject> itemsList, List<Item> items) {
        //TODO if not loading all allItems and just adding so remove this.
        items.clear();

        Item item = null;
        for (ParseObject parseItem : itemsList){
            try {
                if (null != parseItem) {
                    item = new Item(parseItem);
                    items.add(item);
                }
                else {
                    Log.d(Constants.LOST_FOUND_TAG, "parseItem was null");
                }
            } catch (NullPointerException e) {
                Log.d(Constants.LOST_FOUND_TAG, "caught NullPointerException when adding an item (item == null? " + (item == null) + ")");
            }
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.b_add_item:
                Intent intent = new Intent(rootView.getContext(),ReportFormActivity.class);
                intent.putExtra(Constants.ReportForm.IS_LOST_FORM, true);
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
                if (updateDistanceFilter((String) parent.getItemAtPosition(position))) {
                    applyListFilters();
                    Log.d(Constants.LOST_FOUND_TAG, "distance filter updated to " + this.filter_dist + " meter radius");
                }
                adapter.notifyDataSetInvalidated();
                break;

            case R.id.s_time_filter:
                if (updateTimeFilter((String) parent.getItemAtPosition(position))) {
                    applyListFilters();
                    Log.d(Constants.LOST_FOUND_TAG, "time filter updated to " + (this.filter_time / Constants.MILLI_SECONDS_PER_DAY) + " last days");
                }
                adapter.notifyDataSetInvalidated();
                break;

            default:
                Log.d(Constants.LOST_FOUND_TAG, "WTF?! some unknown selection...");
        }
    }

    private boolean updateTimeFilter(String itemAtPosition) {
        long prevFilter = this.filter_time;

        if (itemAtPosition.startsWith("All")) {
            this.filter_time = Constants.NO_TIME_FILTER;
            return (this.filter_time != prevFilter);
        }

        for (String time : Arrays.asList("today", "week", "month", "year")) {
            if (itemAtPosition.toLowerCase().contains(time)) {
                long factor = Constants.daysFactor.get(time);
                this.filter_time = Constants.MILLI_SECONDS_PER_DAY * factor;
                break;
            }
        }

        return (this.filter_time != prevFilter);
    }

    private boolean updateDistanceFilter(String itemAtPosition) {
        long prevFilter = this.filter_dist;

        if (itemAtPosition.startsWith("All")) {
            this.filter_dist = Constants.NO_DISTANCE_FILTER;
            return (this.filter_dist != prevFilter);
        }

        String pattern = "\\D";
        this.filter_dist = Long.parseLong(itemAtPosition.replaceAll(pattern, ""));
        if (itemAtPosition.contains("km")) {
            this.filter_dist *= 1000;
        }

        return (this.filter_dist != prevFilter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
