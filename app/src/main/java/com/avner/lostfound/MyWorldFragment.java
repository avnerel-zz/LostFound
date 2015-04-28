package com.avner.lostfound;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class MyWorldFragment extends Fragment implements View.OnClickListener {

    private ImageButton settingsButton;
    private ImageButton messagesButton;
    private ImageButton logOutButton;
    private View rootView;

    private ListView lv_myLoses;

    private ListView lv_myFinds;

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

        lv_myLoses = (ListView) rootView.findViewById(R.id.lv_openLostListings);
        lv_myFinds = (ListView) rootView.findViewById(R.id.lv_openFoundListings);

        List<Item> lostItems = new ArrayList<>();

        lostItems.add(new Item("Ring", "very nice ring", new GregorianCalendar(), new Location("stam"),R.drawable.ring1));

        lostItems.add(new Item("Necklace", "very nice necklace", new GregorianCalendar(), new Location("stam"),R.drawable.necklace1));

        lostItems.add(new Item("Car keys", "my beautiful car keys", new GregorianCalendar(), new Location("stam"),R.drawable.car_keys1));

        List<Item> foundItems = new ArrayList<>();

        foundItems.add(new Item("Earrings", "very nice earrings", new GregorianCalendar(), new Location("stam"), R.drawable.earings1));

        foundItems.add(new Item("Headphones", "lost my beats", new GregorianCalendar(), new Location("stam"),R.drawable.headphones2));

        LostFoundListAdapter myLosesAdapter = new LostFoundListAdapter(lostItems, rootView);

        LostFoundListAdapter myFindsAdapter = new LostFoundListAdapter(foundItems, rootView);

        lv_myLoses.setAdapter(myLosesAdapter);

        lv_myFinds.setAdapter(myFindsAdapter);

        return rootView;
	}


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_settings){

            Intent intent = new Intent(rootView.getContext(),SettingsActivity.class);

            startActivity(intent);

        }else if(v.getId() == R.id.b_messages){

            Intent intent = new Intent(rootView.getContext(),UsersListActivity.class);

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
