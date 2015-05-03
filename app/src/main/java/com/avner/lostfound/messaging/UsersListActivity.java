package com.avner.lostfound.messaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.avner.lostfound.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;
import java.util.ArrayList;
import java.util.List;


public class UsersListActivity extends Activity implements FindCallback<ParseUser> {

    private String currentUserId;
    private List<String> userDisplayNames;
    private ListView usersListView;
    private ArrayAdapter<String> namesArrayAdapter;
    private ProgressDialog progressDialog;
//    private List<ParseUser> userList;
    private List<String> userIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        currentUserId = ParseUser.getCurrentUser().getObjectId();
        userDisplayNames = new ArrayList<String>();
        userIds = new ArrayList<String>();

        initUserList();
        waitForSinchService();
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

    private void waitForSinchService() {


//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Loading");
//        progressDialog.setMessage("Please wait...");
//        progressDialog.show();

        //broadcast receiver to listen for the broadcast
        //from MessageService
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("LostFound", "received success/failure on connecting to user");
                Boolean success = intent.getBooleanExtra("success", false);
//                progressDialog.dismiss();

                //show a toast message if the Sinch
                //service failed to start
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_SHORT).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.avner.lostfound.messaging.UsersListActivity"));
    }

    private void initUserList() {

        usersListView = (ListView)findViewById(R.id.lv_user_list);


        // try to retrieve from local data.
        ParseUser user = ParseUser.getCurrentUser();
        List<String> localUserIds = (List<String>) user.get("userIds");
        List<String> localUserDisplayNames = (List<String>) user.get("userDisplayNames");

        if(localUserIds!= null && localUserDisplayNames != null){
            userIds = localUserIds;
            userDisplayNames = localUserDisplayNames;
        }

        //TODO change to another list view adapter cause we need to add buttons
        namesArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),  R.layout.list_row_user, userDisplayNames);

        usersListView.setAdapter(namesArrayAdapter);

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                openConversation(i);
            }
        });
        //TODO should be removed because we won't need the list from the server, it will be added inside the app.
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //don't include yourself
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(this);


    }

    private void updateUsers(List<ParseUser> userList) {

        userDisplayNames.clear();
        userIds.clear();

        for (int i=0; i<userList.size(); i++) {

            ParseUser user = userList.get(i);
            userDisplayNames.add((String) user.get("name"));
            userIds.add(user.getObjectId());
        }
        namesArrayAdapter.notifyDataSetChanged();
    }


    @Override
    public void done(final List<ParseUser> userList, com.parse.ParseException e) {

        if(e!=null){
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        updateUsers(userList);

        final ParseUser user = ParseUser.getCurrentUser();
        user.put("userDisplayNames", userDisplayNames);
        user.put("userIds", userIds);
        user.saveInBackground();
        user.unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                user.pinInBackground();
            }
        });
    }

    public void openConversation(int pos) {

        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra("RECIPIENT_ID", userIds.get(pos));
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

}
