package com.avner.lostfound.utils;

import android.location.Location;
import android.util.Log;

import com.avner.lostfound.Constants;
import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.adapters.LostFoundListAdapter;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.structs.ListFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Oded on 24/05/2015.
 */
public class ListFilterUtils {

    public static void applyDistanceFilter(List<Item> items, ListFilter filter, Location lastKnownLocation) {
        if (filter.getDistFilter() == Constants.NO_DISTANCE_FILTER) { // no distance filter defined - skip
            Log.d(Constants.LOST_FOUND_TAG, "distance unfiltered - not applying distance filter");
            return;
        }

        if (null == lastKnownLocation) { // unknown location - skip distance filtering
            Log.d(Constants.LOST_FOUND_TAG, "lastKnownLocation is null - not applying distance filter");
            return;
        }

        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next(); // advance iterator
            Location itemLocation = item.getLocation();
            if (null == itemLocation || lastKnownLocation.distanceTo(itemLocation) > filter.getDistFilter()) {
                it.remove();
            }
        }
    }


    public static void applyTimeFilter(List<Item> items, ListFilter filter) {
        if (filter.getTimeFilter() == Constants.NO_TIME_FILTER) return; // no time filter defined - skip

        long lowerTimeBound = System.currentTimeMillis() - filter.getTimeFilter();

        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next(); // advance iterator
            if (item.getTime().getTimeInMillis() < lowerTimeBound) {
                it.remove();
            }
        }
    }


    /**
     * Apply both location and time filters that are currently defined on the listings being displayed.
     *
     * @param rawList List containing all potential items, to be filtered.
     * @param adapter Adapter used for displaying the list's contents
     * @param filters Filters to be applied on the list.
     * @param lastKnownLocation Last known location, upon which to filter by location.
     */
    public static void applyListFilters(List<Item> rawList, LostFoundListAdapter adapter,
                                        ListFilter filters, Location lastKnownLocation) {
        List<Item> filteredList = new ArrayList<>(rawList);

        ListFilterUtils.applyDistanceFilter(filteredList, filters, lastKnownLocation);
        ListFilterUtils.applyTimeFilter(filteredList, filters);

        Log.d(Constants.LOST_FOUND_TAG, "filtering re-applied, notifying adapter. raw list size: " + rawList.size() + " filtered size: " + filteredList.size());
        adapter.setList(filteredList);
        adapter.notifyDataSetChanged();
    }
}


