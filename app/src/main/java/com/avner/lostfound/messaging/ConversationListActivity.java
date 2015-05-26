package com.avner.lostfound.messaging;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.adapters.ConversationListAdapter;
import com.avner.lostfound.structs.Conversation;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ConversationListActivity extends Activity {

    private String myUserId;
    private ConversationListAdapter conversationAdapter;
    private List<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        myUserId = ParseUser.getCurrentUser().getObjectId();
        conversations = new ArrayList<>();

        initConversationList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation_list, menu);
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

    private void initConversationList() {

        ListView usersListView = (ListView) findViewById(R.id.lv_user_list);

        setContextualBar(usersListView);
        conversationAdapter = new ConversationListAdapter(conversations, this);
        usersListView.setAdapter(conversationAdapter);
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                openConversation(i);
            }
        });

        // get all conversations from parse.
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
        query.whereEqualTo(Constants.ParseConversation.MY_USER_ID, myUserId);
        query.include(Constants.ParseConversation.ITEM);
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> conversationList, ParseException e) {
                // Exception thrown from parse.
                if(e!=null){
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Success in retrieving user list.
                updateConversations(conversationList);
            }
        });
    }

    private void setContextualBar(final ListView usersListView) {
        usersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Capture ListView item click
        usersListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = usersListView.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                conversationAdapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = conversationAdapter.getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Conversation selectedConversation = (Conversation) conversationAdapter.getItem(selected.keyAt(i));
                                // Remove selected items following the ids
                                conversationAdapter.remove(selectedConversation);
                            }
                        }
                        // Close CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_conversation_contextual, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                conversationAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    private void updateConversations(List<ParseObject> conversationList) {

        conversations.clear();

        for (int i=0; i<conversationList.size(); i++) {

            ParseObject parseConversation = conversationList.get(i);
            Conversation conversation = new Conversation(parseConversation);
            conversations.add(conversation);
        }
        conversationAdapter.notifyDataSetChanged();
    }

    public void openConversation(int pos) {

        updateUnreadCount(pos);
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra(Constants.Conversation.RECIPIENT_ID, conversations.get(pos).getUserId());
        intent.putExtra(Constants.Conversation.RECIPIENT_NAME, conversations.get(pos).getUserName());
        intent.putExtra(Constants.Conversation.ITEM_ID, conversations.get(pos).getItem().getId());
        startActivity(intent);
    }

    private void updateUnreadCount(int pos) {
        Conversation conversation = conversations.get(pos);
        conversation.setUnreadCount(0);
        conversationAdapter.notifyDataSetChanged();
    }
}
