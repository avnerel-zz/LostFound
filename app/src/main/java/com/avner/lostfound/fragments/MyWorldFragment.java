package com.avner.lostfound.fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.avner.lostfound.structs.Item;
import com.avner.lostfound.activities.LoginActivity;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.SettingsActivity;
import com.avner.lostfound.adapters.OpenItemsAdapter;
import com.avner.lostfound.messaging.ConversationActivity;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class MyWorldFragment extends Fragment implements View.OnClickListener {

    private ImageButton settingsButton;
    private ImageButton messagesButton;
    private ImageButton logOutButton;
    private View rootView;

    private ListView lv_openListings;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_my_world, container, false);

        messagesButton = (ImageButton)rootView.findViewById(R.id.b_messages);
        messagesButton.setOnClickListener(this);

        settingsButton = (ImageButton)rootView.findViewById(R.id.b_settings);
        settingsButton.setOnClickListener(this);

        logOutButton = (ImageButton)rootView.findViewById(R.id.b_log_out);
        logOutButton.setOnClickListener(this);

        initOpenListings();

        return rootView;
	}

    private void initOpenListings() {

        lv_openListings = (ListView) rootView.findViewById(R.id.lv_openListings);

        List<Item> items = new ArrayList<>();

        Location location = new Location("");
        location.setLatitude(32.7734607);
        location.setLongitude(35.0320228);

        String userId = "LKkpD5iTPx";
        String userName = "Avner Elizarov";
        String itemId = "stam";

        items.add(new Item(itemId,"Headphones", "lost my beats", new GregorianCalendar(), location, R.drawable.headphones2,userId,userName));
        items.add(new Item(itemId,"Earrings", "very nice earrings", new GregorianCalendar(), location, R.drawable.earings1,userId,userName));
        items.add(new Item(itemId,"Car keys", "my beautiful car keys", new GregorianCalendar(), location, R.drawable.car_keys1,userId,userName));

        OpenItemsAdapter myOpenListingsAdapter = new OpenItemsAdapter(items, rootView);
        lv_openListings.setAdapter(myOpenListingsAdapter);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_settings){

            Intent intent = new Intent(rootView.getContext(),SettingsActivity.class);
            startActivity(intent);

        }else if(v.getId() == R.id.b_messages){

            Intent intent = new Intent(rootView.getContext(),ConversationActivity.class);
            startActivity(intent);
        }

        if(v.getId() == R.id.b_log_out){

            ParseUser.logOut();

            Intent intent = new Intent(rootView.getContext(),LoginActivity.class);
            startActivity(intent);
            getActivity().finish();

        }
    }


}
