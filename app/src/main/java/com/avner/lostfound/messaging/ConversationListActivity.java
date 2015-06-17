package com.avner.lostfound.messaging;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.SearchView;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.adapters.ConversationListAdapter;
import com.avner.lostfound.structs.Conversation;
import com.parse.FindCallback;
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
    private List<Conversation> filteredConversations;
    private SearchView sv_search;
    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        myUserId = ParseUser.getCurrentUser().getObjectId();
        conversations = new ArrayList<>();

        filteredConversations = new ArrayList<>();
        initConversationList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation_list, menu);
        initSearchView(menu);
        return true;
    }

    private void initSearchView(Menu menu) {
        sv_search = (SearchView) menu.findItem(R.id.search_conversation).getActionView();
        sv_search.setSubmitButtonEnabled(true);
        sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (null == query || query.length() < 2) {
                    return false;
                }

                applyFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (null == newText) {
                    return false;
                }

                if (newText.isEmpty()) {
                    clearFilter();
                }

                return true;
            }
        });
    }

    private void clearFilter() {
        applyFilter("");
    }

    private void applyFilter(String query) {
        if (null == query) {
            Log.d(Constants.LOST_FOUND_TAG, "can't apply a null filter");
            return;
        }

        currentFilter = query.toLowerCase();
        filteredConversations.clear();

        if (query.isEmpty()) { // for empty filter - add all conversations
            filteredConversations.addAll(conversations);
        } else {
            for (Conversation c : conversations) {
                if (c.getItem().getName().toLowerCase().contains(currentFilter)
                        || c.getUserName().toLowerCase().contains(currentFilter)) {
                    filteredConversations.add(c);
                }
            }
        }

        conversationAdapter.notifyDataSetInvalidated();
    }

    private void reapplyCurrentFilter() {
        applyFilter(currentFilter);
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
        conversationAdapter = new ConversationListAdapter(filteredConversations, this);
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
                if (e != null) {
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
                conversationAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });
    }

    private void updateConversations(List<ParseObject> conversationList) {
        conversations.clear();
        filteredConversations.clear();

        for (ParseObject parseConversation : conversationList) {
            conversations.add(new Conversation(parseConversation));
        }

        filteredConversations.addAll(conversations);
        reapplyCurrentFilter();
    }

    public void openConversation(int pos) {
        updateUnreadCount(pos);
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        Conversation conversation = conversations.get(pos);
        intent.putExtra(Constants.Conversation.RECIPIENT_ID, conversation.getUserId());
        intent.putExtra(Constants.Conversation.RECIPIENT_NAME, conversation.getUserName());
        intent.putExtra(Constants.Conversation.ITEM_ID, conversation.getItem().getId());
        startActivity(intent);
    }

    private void updateUnreadCount(int pos) {
        Conversation conversation = conversations.get(pos);
        conversation.setUnreadCount(0);
        conversationAdapter.notifyDataSetChanged();
    }
}
