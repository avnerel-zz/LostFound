package com.avner.lostfound.fragments;

import android.app.Dialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.adapters.LostFoundListAdapter;
import com.software.shell.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class FoundListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView lv_itemList;

    private View rootView;
    private FloatingActionButton button;

    private Spinner timeSpinner;

    private Spinner locationSpinner;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_found_list, container, false);

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

        List<Item> items = new ArrayList<>();

        Location location = new Location("");
        location.setLatitude(32.7734607);
        location.setLongitude(35.0320228);

        items.add(new Item("Ring", "very nice ring", new GregorianCalendar(), location, R.drawable.ring1));
        items.add(new Item("Necklace", "very nice necklace", new GregorianCalendar(), location, R.drawable.necklace1));
        items.add(new Item("Car keys", "my beautiful car keys", new GregorianCalendar(), location, R.drawable.car_keys1));
        items.add(new Item("Earrings", "very nice earrings", new GregorianCalendar(), location, R.drawable.earings1));
        items.add(new Item("Headphones", "lost my beats", new GregorianCalendar(), location, R.drawable.headphones2));


        //ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        LostFoundListAdapter adapter = new LostFoundListAdapter(items,rootView);

        lv_itemList.setClickable(true);

        lv_itemList.setAdapter(adapter);

        lv_itemList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_item_details_layout);

        Item item = (Item) parent.getItemAtPosition(position);
        if (null == item) {
            Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
            return;
        }

        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);
        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), ViewLocationActivity.class);
                Item item = (Item) parent.getItemAtPosition(position);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                double latitude = item.getLocation().getLatitude();
                double longitude = item.getLocation().getLongitude();
                intent.putExtra(Constants.LATITUDE, latitude);
                intent.putExtra(Constants.LONGITUDE, longitude);
                startActivity(intent);

            }
        });

        setDialogContents(dialog, item);

        dialog.show();
    }

    private void setDialogContents(Dialog dialog, Item item) {
        TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());
        itemLocation.setMaxLines(2);

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.timeAgo());

        ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        itemImage.setImageResource(item.getImage());

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Found Item: " + item.getName());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_add_item){

            Intent intent = new Intent(rootView.getContext(),ReportFormActivity.class);

            startActivity(intent);
        }
    }

}



