package com.avner.lostfound;

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

import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;
import java.util.ArrayList;
import java.util.List;


public class UsersListActivity extends Activity {

    private String currentUserId;
    private ArrayList<String> userNames;
    private ArrayList<String> displayNames;
    private ListView usersListView;
    private ArrayAdapter<String> namesArrayAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        currentUserId = ParseUser.getCurrentUser().getObjectId();
        userNames = new ArrayList<String>();
        displayNames = new ArrayList<String>();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
//don't include yourself
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i=0; i<userList.size(); i++) {
                        String userName = userList.get(i).getUsername().toString();
                        userNames.add(userName);
                        displayNames.add(userName.split("@")[0]);
                    }
                    usersListView = (ListView)findViewById(R.id.lv_user_list);
                    namesArrayAdapter =
                            new ArrayAdapter<String>(getApplicationContext(),
                                    R.layout.list_row_user, displayNames);
                    usersListView.setAdapter(namesArrayAdapter);
                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                            openConversation(userNames, i);
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }

            public void openConversation(ArrayList<String> names, int pos) {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", names.get(pos));
                query.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> user, ParseException e) {
                        if (e == null) {
                            Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                            intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
                            startActivity(intent);

                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error finding that user",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

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
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.avner.lostfound.UsersListActivity"));
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
}
