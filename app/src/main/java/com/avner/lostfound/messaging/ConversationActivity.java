package com.avner.lostfound.messaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.adapters.ConversationListAdapter;
import com.avner.lostfound.structs.Item;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;


public class ConversationActivity extends Activity implements FindCallback<ParseObject> {

    private String currentUserId;
    private List<String> userDisplayNames;
    private ListView usersListView;
    private ConversationListAdapter conversationAdapter;
    private ProgressDialog progressDialog;
//    private List<ParseUser> userList;
    private List<String> userIds;
    private List<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        currentUserId = ParseUser.getCurrentUser().getObjectId();
        userDisplayNames = new ArrayList<String>();
        userIds = new ArrayList<String>();

        initItems();
        initUserList();
    }

    /**
     * this is only for debug. items should be from server.
     */
    private void initItems() {
        items = new ArrayList<>();

        Location location = new Location("");
        location.setLatitude(32.7734607);
        location.setLongitude(35.0320228);

        String userId = "LKkpD5iTPx";
        String userName = "Avner Elizarov";
        String itemId = "stam";
        items.add(new Item(itemId,"Ring", "very nice ring", new GregorianCalendar(), location, R.drawable.ring1,userId,userName));
        items.add(new Item(itemId,"Necklace", "very nice necklace", new GregorianCalendar(), location, R.drawable.necklace1,userId,userName));
        items.add(new Item(itemId,"Car keys", "my beautiful car keys", new GregorianCalendar(), location, R.drawable.car_keys1,userId,userName));
        items.add(new Item(itemId,"Earrings", "very nice earrings", new GregorianCalendar(), location, R.drawable.earings1,userId,userName));
        items.add(new Item(itemId,"Headphones", "lost my beats", new GregorianCalendar(), location, R.drawable.headphones2,userId,userName));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users_list, menu);
        return true;
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

    private void initUserList() {

        usersListView = (ListView)findViewById(R.id.lv_user_list);

        //TODO change to another list view adapter cause we need to add buttons
        conversationAdapter = new ConversationListAdapter(items, userDisplayNames, this);

        usersListView.setAdapter(conversationAdapter);

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                openConversation(i);
            }
        });
        //TODO should be removed because we won't need the list from the server, it will be added inside the app.

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseConversations");
        //don't include yourself
        query.whereEqualTo("userId", currentUserId);
        query.findInBackground(this);
    }

    private void updateUsers(List<ParseObject> userList) {

        userDisplayNames.clear();
        userIds.clear();
        //TODO remove comment when there are items in the parse object.
//        items.clear();

        for (int i=0; i<userList.size(); i++) {

            ParseObject conversation = userList.get(i);
            userDisplayNames.add((String) conversation.get("conversationUserName"));
            userIds.add((String) conversation.get("conversationUserId"));
            //TODO parse item and add it.
//            items.add(conversation.get("conversationItemId"))
        }
        conversationAdapter.notifyDataSetChanged();
    }

    public void openConversation(int pos) {

        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, userIds.get(pos));
        startActivity(intent);

//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.whereEqualTo("username", names.get(pos));
//        query.findInBackground(new FindCallback<ParseUser>() {
//            public void done(List<ParseUser> user, ParseException e) {
//                if (e == null) {
//                    Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
//                    intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
//                    startActivity(intent);
//
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Error finding that user",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public void done(List<ParseObject> userList, ParseException e) {

        if(e!=null){
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        updateUsers(userList);


    }
}
