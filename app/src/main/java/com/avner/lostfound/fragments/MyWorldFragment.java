package com.avner.lostfound.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.messaging.ConversationListActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.activities.LoginActivity;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.SettingsActivity;
import com.avner.lostfound.adapters.OpenItemsAdapter;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyWorldFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_my_world, container, false);

        ImageButton messagesButton = (ImageButton) rootView.findViewById(R.id.b_messages);
        messagesButton.setOnClickListener(this);

        ImageButton settingsButton = (ImageButton) rootView.findViewById(R.id.b_settings);
        settingsButton.setOnClickListener(this);

        ImageButton logOutButton = (ImageButton) rootView.findViewById(R.id.b_log_out);
        logOutButton.setOnClickListener(this);

        initOpenListings();

        return rootView;
	}

    private void initOpenListings() {

        ListView lv_openListings = (ListView) rootView.findViewById(R.id.lv_openListings);

        final List<Item> items = new ArrayList<>();
        final OpenItemsAdapter myOpenListingsAdapter = new OpenItemsAdapter(items, rootView);

        // get all my lost items
        ParseQuery<ParseObject> lostQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        lostQuery.whereEqualTo(Constants.ParseReport.USER_ID, ParseUser.getCurrentUser().getObjectId());

        // get all my found items
        ParseQuery<ParseObject> foundQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        foundQuery.whereEqualTo(Constants.ParseReport.USER_ID, ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(lostQuery);
        queries.add(foundQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByAscending(Constants.ParseQuery.CREATED_AT);

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> itemsList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < itemsList.size(); i++) {
                        convertParseListToItemList(itemsList, items);
                    }
                    myOpenListingsAdapter.notifyDataSetChanged();
                }
            }
        });
        lv_openListings.setAdapter(myOpenListingsAdapter);
        lv_openListings.setOnItemClickListener(myOpenListingsAdapter);
    }

    private void convertParseListToItemList(List<ParseObject> itemsList, List<Item> items) {

        //TODO if not loading all items and just adding so remove this.
        items.clear();
        Item item = null;
        for (ParseObject parseItem : itemsList){
            try {
                if (null != parseItem) {
                    item = new Item(parseItem);
                    if (null == item) {
                        Log.d(Constants.LOST_FOUND_TAG, "item created as NULL");
                    }
                    items.add(item);
                }
            } catch (NullPointerException e) {
                Log.d(Constants.LOST_FOUND_TAG, "caught NullPointerException when adding an item (item == null?" + (item == null) + ")");
                Log.d(Constants.LOST_FOUND_TAG, "based on parseItem: " + parseItem.toString());
            }
        }
    }

    @Override
    public void onClick(View v) {

        Intent intent;
        switch(v.getId()){

            case R.id.b_settings:
                intent = new Intent(rootView.getContext(),SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.b_messages:
                intent = new Intent(rootView.getContext(),ConversationListActivity.class);
                startActivity(intent);
                break;
            case R.id.b_log_out:
                ParseUser.logOut();
                intent = new Intent(rootView.getContext(),LoginActivity.class);
                startActivity(intent);
                // return to login screen.
                getActivity().finish();
                break;
        }
    }


}
