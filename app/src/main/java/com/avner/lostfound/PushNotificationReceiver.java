package com.avner.lostfound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.messaging.MessagingActivity;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by avner on 04/05/2015.
 */
public class PushNotificationReceiver extends ParsePushBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
    }

    /**
     * @return false iff the push should show a notification
     */
    private boolean handlePushes(Context context, JSONObject jsonData) throws JSONException {
        String pushType = (String) jsonData.get(Constants.ParsePush.PUSH_TYPE);
        Log.d("PUSH_RECEIVED", "pushType: " + pushType);
        switch (pushType) {
            case Constants.ParsePush.TYPE_MESSAGE:
                return handlePushOfParseMessage(jsonData,context);
            case Constants.ParsePush.TYPE_LOST:
                handlePushOfParseLost(jsonData);
                LostFoundApplication applicationContext = (LostFoundApplication) context.getApplicationContext();
                MainActivity mainActivity = applicationContext.getMainActivity();
                if(mainActivity!= null){
                    mainActivity.updateLocalDataInFragments();
                }
                return false;
            case Constants.ParsePush.TYPE_FOUND:
                handlePushOfParseFound(jsonData);
                return false;
        }
        return true;
    }

    private void handlePushOfParseFound(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get(Constants.ParsePush.REPORTED_ITEM);
        Log.d("PUSH_FOUND", reportedItem);
        // TODO: create object from the data, as in handlePushOfParseLost. shouldn't happen cause there is no more found and lost.
    }

    private void handlePushOfParseLost(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get(Constants.ParsePush.REPORTED_ITEM);
        Log.d("PUSH_LOST", reportedItem);
        JSONObject report = new JSONObject(reportedItem);

        final String objectId = report.get(Constants.ParseQuery.OBJECT_ID).toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseLost");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                /*
                 if e!=null, there was a problem retrieving the object from the server, so we delete
                 it from the local datastore. Otherwise, we need to update it, i.e. remove the existing
                 object from the local datastore and insert the new (or updated) version.
                 Either way - we remove the object from the local datastore.
                */

                if (e != null) {
                    Log.d("PUSH_LOST", "Could not get the data from server");
                    // could not retrieve the object from the server
                    removeReportFromLocalDatastoreIfExists(objectId);
                    return;
                }

                // we first remove the existing object from the local datastore (if it exists)
                // and insert a new one
                Log.d("PUSH_LOST", "Got the data from server");
                removeReportFromLocalDatastoreIfExists(objectId, new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("PUSH_LOST", "about to insert into local datastore");
                        insertReportInLocalDatastore(parseObject);
                    }
                });
            }
        });

        // TODO: refresh screen if we are in the lost/found list tab
    }

    private void insertReportInLocalDatastore(ParseObject parseObject) {
        parseObject.pinInBackground();
    }

    private void removeReportFromLocalDatastoreIfExists(String objectId) {
        removeReportFromLocalDatastoreIfExists(objectId, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                // NOP
            }
        });

    }

    private void removeReportFromLocalDatastoreIfExists(String objectId, final DeleteCallback callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        query.fromLocalDatastore();
        query.getInBackground(objectId, new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d("PUSH_LOST", "Removing existing object from local datastore");
                    parseObject.unpinInBackground(callback);
                    return;
                } else {
                    Log.d("PUSH_LOST", "Trying to remove, but the item was not in the local datastore");
                }
                callback.done(null);
            }
        });
    }


    private boolean handlePushOfParseMessage(JSONObject jsonData, Context context) throws JSONException {

        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        final String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
        final String senderName = jsonData.getString(Constants.ParsePush.SENDER_NAME);
        final String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);

        LostFoundApplication applicationContext = (LostFoundApplication) context.getApplicationContext();
        if (applicationContext.isMessagingActivityActive() && applicationContext.getMessagingActivityItemId().equals(senderId)) {
            // no need for popup notification. already in messaging session.
            return false;
        }

        ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        itemQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, itemId);

        String[] userIds = {currentUserId, senderId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
        query.whereContainedIn(Constants.ParseConversation.MY_USER_ID, Arrays.asList(userIds));
        query.whereContainedIn(Constants.ParseConversation.RECIPIENT_USER_ID, Arrays.asList(userIds));
        query.whereMatchesQuery(Constants.ParseConversation.ITEM, itemQuery);
        query.include(Constants.ParseConversation.ITEM);

        addConversationIfNeeded(currentUserId, senderId, senderName, query);

        return true;
    }

    private void addConversationIfNeeded(final String currentUserId, final String senderId, final String senderName, ParseQuery<ParseObject> query) {
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> conversationList, ParseException e) {
                // Exception thrown from parse.
                if (e != null) {
                    return;
                }
                // check if conversation exists for this user.
                boolean foundConversation = false;
                ParseObject parseItem = null;
                for (ParseObject conversation : conversationList) {
                    if (conversation.get(Constants.ParseConversation.RECIPIENT_USER_ID).equals(senderId)) {
                        foundConversation = true;
                        int unreadCount = (int) conversation.get(Constants.ParseConversation.UNREAD_COUNT);
                        conversation.put(Constants.ParseConversation.UNREAD_COUNT, ++unreadCount);
                        conversation.saveInBackground();

                        // found the other user conversation.
                    } else {
                        parseItem = (ParseObject) conversation.get(Constants.ParseConversation.ITEM);
                    }
                }
                if (!foundConversation) {
                    ParseObject parseConversation = new ParseObject(Constants.ParseObject.PARSE_CONVERSATION);
                    parseConversation.put(Constants.ParseConversation.ITEM, parseItem);
                    parseConversation.put(Constants.ParseConversation.RECIPIENT_USER_ID, senderId);
                    parseConversation.put(Constants.ParseConversation.MY_USER_ID, currentUserId);
                    parseConversation.put(Constants.ParseConversation.RECIPIENT_USER_NAME, senderName);
                    parseConversation.put(Constants.ParseConversation.UNREAD_COUNT, 1);
                    parseConversation.pinInBackground();
                    parseConversation.saveInBackground();
                }
            }
        });
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        if (intent.hasExtra(Constants.ParsePush.EXTRA_NAME)) {
            String stringData = intent.getStringExtra(Constants.ParsePush.EXTRA_NAME);

            JSONObject jsonData;
            try {
                jsonData = new JSONObject(stringData);
                Log.d("PUSH_RECEIVED", "jsonData: " + jsonData);

                if (!handlePushes(context, jsonData)) return;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        if (!intent.hasExtra(Constants.ParsePush.EXTRA_NAME)) {
            super.onPushOpen(context, intent);
        }
        String stringData = intent.getStringExtra(Constants.ParsePush.EXTRA_NAME);
        JSONObject jsonData;

        try {
            jsonData = new JSONObject(stringData);
            String pushType = (String) jsonData.get(Constants.ParsePush.PUSH_TYPE);
            switch (pushType) {
                case Constants.ParsePush.TYPE_MESSAGE:

                    openMessagingActivity(context, jsonData);
                    break;
                default:
                    super.onPushOpen(context, intent);
        }

        }catch (JSONException e) {
        e.printStackTrace();
        }

    }

    private void openMessagingActivity(Context context, JSONObject jsonData) throws JSONException {
        String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
        String senderName = jsonData.getString(Constants.ParsePush.SENDER_NAME);
        String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);

        Intent messagingIntent = new Intent(context, MessagingActivity.class);
        messagingIntent.putExtra(Constants.Conversation.RECIPIENT_NAME, senderName);
        messagingIntent.putExtra(Constants.Conversation.ITEM_ID, itemId);
        messagingIntent.putExtra(Constants.Conversation.RECIPIENT_ID, senderId);
        messagingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(messagingIntent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }
}

