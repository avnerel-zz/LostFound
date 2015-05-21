package com.avner.lostfound.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.ImageUtils;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.messaging.MessagingActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.adapters.LostFoundListAdapter;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.software.shell.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LostListFragment extends Fragment implements View.OnClickListener {

    private ListView lv_itemList;

    private FloatingActionButton button;

    private View rootView;

    private Spinner timeSpinner;

    private Spinner locationSpinner;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_lost_list, container, false);

        lv_itemList = (ListView) rootView.findViewById(R.id.lv_myList);

        button = (FloatingActionButton) rootView.findViewById(R.id.b_add_item);
        button.setOnClickListener(this);

        initItemsList();

        timeSpinner = (Spinner) rootView.findViewById(R.id.s_time_filter);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> timeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_time, android.R.layout.simple_spinner_item);
// Apply the adapter to the spinner
        timeSpinner.setAdapter(timeSpinnerAdapter);

        locationSpinner = (Spinner) rootView.findViewById(R.id.s_location_filter);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> locationSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_locations, android.R.layout.simple_spinner_item);
// Apply the adapter to the spinner
        locationSpinner.setAdapter(locationSpinnerAdapter);

        return rootView;
	}

    private void initItemsList() {
        final List<Item> items = new ArrayList<>();

        final LostFoundListAdapter adapter;
        adapter = new LostFoundListAdapter(items,rootView);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> itemsList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < itemsList.size(); i++) {
                        convertParseListToItemList(itemsList,items);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
//        Location location = new Location("");
//        location.setLatitude(32.7734607);
//        location.setLongitude(35.0320228);
//
//        String userId = "LKkpD5iTPx";
//        String userName = "Avner Elizarov";
//        String itemId = "stam";
//        items.add(new Item(itemId,"Ring", "very nice ring", new GregorianCalendar(), location, R.drawable.ring1,userId,userName));
//        items.add(new Item(itemId,"Necklace", "very nice necklace", new GregorianCalendar(), location, R.drawable.necklace1,userId,userName));
//        items.add(new Item(itemId,"Car keys", "my beautiful car keys", new GregorianCalendar(), location, R.drawable.car_keys1,userId,userName));
//        items.add(new Item(itemId,"Earrings", "very nice earrings", new GregorianCalendar(), location, R.drawable.earings1,userId,userName));
//        items.add(new Item(itemId,"Headphones", "lost my beats", new GregorianCalendar(), location, R.drawable.headphones2,userId,userName));


        lv_itemList.setClickable(true);
        lv_itemList.setAdapter(adapter);
        lv_itemList.setOnItemClickListener(adapter);
    }

    private void convertParseListToItemList(List<ParseObject> itemsList, List<Item> items) {

        //TODO if not loading all items and just adding so remove this.
        items.clear();
        for(ParseObject parseItem: itemsList){

            Item item = new Item(parseItem);
            items.add(item);
        }

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_add_item){

            Intent intent = new Intent(rootView.getContext(),ReportFormActivity.class);

            intent.putExtra(Constants.IS_LOST_FORM, true);
            startActivity(intent);
        }
    }

}



