package com.avner.lostfound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.avner.lostfound.messaging.MessagingActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by avner on 04/05/2015.
 */
public class PushNotificationReceiver extends ParsePushBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(Constants.ParsePush.EXTRA_NAME)) {
            String stringData = intent.getStringExtra(Constants.ParsePush.EXTRA_NAME);

            JSONObject jsonData = null;
            try {
                jsonData = new JSONObject(stringData);
                Log.d("PUSH_RECEIVED", "jsonData: " + jsonData);

                if (!handlePushes(context, jsonData)) return;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onReceive(context, intent);
    }

    /**
     * @return false iff the push should show a notification
     */
    private boolean handlePushes(Context context, JSONObject jsonData) throws JSONException {
        String pushType = (String) jsonData.get(Constants.ParsePush.PUSH_TYPE);
        Log.d("PUSH_RECEIVED", "pushType: " + pushType);
        switch (pushType) {
            case Constants.ParseObject.PARSE_MESSAGE:
                handlePushOfParseMessage(jsonData);

                if (((LostFoundApplication) context.getApplicationContext()).isMessagingActivityActive()) {
                    // no need for popup notification. already in messaging session.
                    return false;
                }
                break;
            case Constants.ParseObject.PARSE_LOST:
                handlePushOfParseLost(jsonData);
                return false;
            case Constants.ParseObject.PARSE_FOUND:
                handlePushOfParseFound(jsonData);
                return false;
        }
        return true;
    }

    private void handlePushOfParseFound(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get(Constants.ParsePush.REPORTED_ITEM);
        Log.d("PUSH_FOUND", reportedItem);
        // TODO: create object from the data, as in handlePushOfParseLost
    }

    private void handlePushOfParseLost(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get(Constants.ParsePush.REPORTED_ITEM);
        Log.d("PUSH_LOST", reportedItem);
        StringBuilder sb = new StringBuilder("KeySet: [");
        for (Iterator iterator = new JSONObject(reportedItem).keys(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(",");
        }
        Log.d("PUSH_LOST", sb.append("]").toString());

        // TODO: create object from the data
    }

    private void handlePushOfParseMessage(JSONObject jsonData) {

        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        try {
            final String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
            final String senderName = jsonData.getString(Constants.ParsePush.SENDER_NAME);
            final String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);

            ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
            itemQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, itemId);

            String[] userIds = {currentUserId, senderId};
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
            query.whereContainedIn(Constants.ParseConversation.MY_USER_ID, Arrays.asList(userIds));
            query.whereContainedIn(Constants.ParseConversation.RECIPIENT_USER_ID, Arrays.asList(userIds));
            query.whereMatchesQuery(Constants.ParseConversation.ITEM,itemQuery);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> conversationList, ParseException e) {
                    // Exception thrown from parse.
                    if(e!=null){
                        return;
                    }
                    // check if conversation exists for this user.
                    boolean foundConversation = false;
                    ParseObject parseItem = null;
                    for(ParseObject conversation : conversationList){
                        if(conversation.get(Constants.ParseConversation.RECIPIENT_USER_ID).equals(senderId)){
                            foundConversation = true;
                            int unreadCount = (int) conversation.get(Constants.ParseConversation.UNREAD_COUNT);
                            conversation.put(Constants.ParseConversation.UNREAD_COUNT, ++unreadCount);
                            conversation.saveInBackground();

                         // found the other user conversation.
                        }else{
                            parseItem = (ParseObject) conversation.get(Constants.ParseConversation.ITEM);
                        }
                    }
                    if(!foundConversation){
                        ParseObject parseConversation = new ParseObject(Constants.ParseObject.PARSE_CONVERSATION);
                        parseConversation.put(Constants.ParseConversation.ITEM,parseItem);
                        parseConversation.put(Constants.ParseConversation.RECIPIENT_USER_ID,senderId);
                        parseConversation.put(Constants.ParseConversation.MY_USER_ID,currentUserId);
                        parseConversation.put(Constants.ParseConversation.RECIPIENT_USER_NAME,senderName);
                        parseConversation.put(Constants.ParseConversation.UNREAD_COUNT,1);
                        parseConversation.saveInBackground();
                    }
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        if (intent.hasExtra(Constants.ParsePush.EXTRA_NAME)) {
            String stringData = intent.getStringExtra(Constants.ParsePush.EXTRA_NAME);

            JSONObject jsonData = null;

            try {
                jsonData = new JSONObject(stringData);
                String pushType = (String) jsonData.get(Constants.ParsePush.PUSH_TYPE);
                switch (pushType) {
                    case Constants.ParseObject.PARSE_MESSAGE:

                        String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
                        String senderName = jsonData.getString(Constants.ParsePush.SENDER_NAME);
                        String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);

                        Intent messagingIntent = new Intent(context, MessagingActivity.class);
                        messagingIntent.putExtra(Constants.Conversation.RECIPIENT_NAME, senderName);
                        messagingIntent.putExtra(Constants.Conversation.ITEM_ID, itemId);
                        messagingIntent.putExtra(Constants.Conversation.RECIPIENT_ID, senderId);
                        context.startActivity(messagingIntent);
                        break;
                    default:
                        super.onPushOpen(context, intent);
                }

                }catch (JSONException e) {
                e.printStackTrace();
            }
        }else{

            super.onPushOpen(context, intent);
        }


    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }
}

