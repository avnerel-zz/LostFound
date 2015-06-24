package com.avner.lostfound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avner.lostfound.activities.MessagingActivity;
import com.avner.lostfound.utils.SignalSystem;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import static com.avner.lostfound.Constants.UIActions.uiaCompleteConversationSent;
import static com.avner.lostfound.Constants.UIActions.uiaConversationSaved;
import static com.avner.lostfound.Constants.UIActions.uiaItemSaved;
import static com.avner.lostfound.Constants.UIActions.uiaMessageSaved;

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
                return false;
            case Constants.ParsePush.COMPLETE_CONVERSATION_REQUEST:
                return handlePushOfCompleteConversationRequest(jsonData, context);
            case Constants.ParsePush.COMPLETE_CONVERSATION_REPLY:
                return true;
            case Constants.ParsePush.TYPE_CONVERSATION:
                handlePushOfParseConversation(jsonData);
                return false;
            case Constants.ParsePush.TYPE_MY_MESSAGE:
                saveMyMessage(jsonData);
                return false;
        }
        return true;
    }

    private void saveMyMessage(JSONObject jsonData) throws JSONException {

        String messageId = (String) jsonData.get(Constants.ParseQuery.OBJECT_ID);
        pinMessage(messageId);
    }

    private void handlePushOfParseConversation(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get(Constants.ParsePush.REPORTED_ITEM);
        Log.d("PUSH_CONVERSATION", reportedItem);
        JSONObject report = new JSONObject(reportedItem);

        final String objectId = report.get(Constants.ParseQuery.OBJECT_ID).toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseConversation");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseConversation, ParseException e) {

                if (e != null) {
                    Log.e("PUSH_CONVERSATION", "Could not get the data from server");
                }else{

                    Log.d("PUSH_CONVERSATION", "Got the data from server");
//                    parseConversation.put(Constants.ParseConversation.UNREAD_COUNT, 1);
                    parseConversation.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            SignalSystem.getInstance().fireUpdateChange(uiaConversationSaved);
                        }
                    });
                }
            }
        });
    }

    private boolean handlePushOfCompleteConversationRequest(JSONObject jsonData, Context context) throws JSONException {

        final String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
        final String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);

        LostFoundApplication applicationContext = (LostFoundApplication) context.getApplicationContext();
        if (applicationContext.getMessagingActivity()!=null &&
                itemId.equals(applicationContext.getMessagingActivityItemId())&&
                senderId.equals(applicationContext.getMessagingRecipientId())){

            Intent intent = new Intent();
            intent.putExtra(Constants.ParseMessage.RECIPIENT_ID, senderId);
            intent.putExtra(Constants.ParseMessage.ITEM_ID, itemId);
            SignalSystem.getInstance().fireUpdateChange(uiaCompleteConversationSent, true, intent);
            updateUnreadCount(senderId, itemId);
            return false;
        }
        return true;
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

                if (e != null) {
                    Log.d("PUSH_LOST", "Could not get the data from server");
                    // could not retrieve the object from the server
                }else{

                    Log.d("PUSH_LOST", "Got the data from server");
                    insertReportInLocalDataStore(parseObject);
                }
            }
        });
    }

    private void insertReportInLocalDataStore(ParseObject parseObject) {
        parseObject.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                SignalSystem.getInstance().fireUpdateChange(uiaItemSaved);
            }
        });
    }

    private boolean handlePushOfParseMessage(JSONObject jsonData, Context context) throws JSONException {

//        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        final String senderId = jsonData.getString(Constants.ParsePush.SENDER_ID);
//        final String senderName = jsonData.getString(Constants.ParsePush.SENDER_NAME);
        final String itemId = jsonData.getString(Constants.ParsePush.ITEM_ID);
        final String messageId = jsonData.getString(Constants.ParseQuery.OBJECT_ID);


        LostFoundApplication applicationContext = (LostFoundApplication) context.getApplicationContext();

        pinMessage(messageId);
        if (applicationContext.getMessagingActivity()!=null &&
                itemId.equals(applicationContext.getMessagingActivityItemId())&&
                senderId.equals(applicationContext.getMessagingRecipientId())){
            // no need for popup notification. already in messaging session.
            return false;
        }

        // find local conversation and update unread count. do this only if conversation isn't opened.
        updateUnreadCount(senderId, itemId);

        return true;
    }

    private void updateUnreadCount(String senderId, String itemId) {
        ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        itemQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, itemId);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
        query.whereEqualTo(Constants.ParseConversation.MY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Constants.ParseConversation.RECIPIENT_USER_ID, senderId);
        query.whereMatchesQuery(Constants.ParseConversation.ITEM, itemQuery);
        query.fromLocalDatastore();
        try {
            ParseObject parseConversation = query.getFirst();
            int unreadCount = (int) parseConversation.get(Constants.ParseConversation.UNREAD_COUNT);
            parseConversation.put(Constants.ParseConversation.UNREAD_COUNT, ++unreadCount);
            parseConversation.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e!= null){
                        Log.e(Constants.LOST_FOUND_TAG, "exception while trying to pin conversation: " + e.getLocalizedMessage());
                        return;
                    }
                    SignalSystem.getInstance().fireUpdateChange(uiaConversationSaved);
                }
            });
            Log.d("PARSE_MESSAGE", "updated unread Count. now is: " + unreadCount );

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("PARSE_MESSAGE", "couldn't retrieve conversation from local data store.");
        }


    }

    private void pinMessage(String messageId) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_MESSAGE);
        query.whereEqualTo(Constants.ParseQuery.OBJECT_ID, messageId);
        try {
            ParseObject parseMessage = query.getFirst();
            if(parseMessage == null ){
                Log.e("PARSE_MESSAGE", "couldn't retrieve message from server.");
                return;
            }
            final String senderId = parseMessage.getString(Constants.ParsePush.SENDER_ID);
            final String itemId = parseMessage.getString(Constants.ParsePush.ITEM_ID);

            parseMessage.pin();
            Intent intent = new Intent();
            intent.putExtra(Constants.ParseMessage.RECIPIENT_ID, senderId);
            intent.putExtra(Constants.ParseMessage.ITEM_ID, itemId);
            SignalSystem.getInstance().fireUpdateChange(uiaMessageSaved, true, intent);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("PARSE_MESSAGE", e.getLocalizedMessage());

        }

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        if (intent.hasExtra(Constants.ParsePush.EXTRA_NAME)) {
            String stringData = intent.getStringExtra(Constants.ParsePush.EXTRA_NAME);

            JSONObject jsonData;
            try {
                jsonData = new JSONObject(stringData);
                Log.d("PUSH_RECEIVED", "jsonData: " + jsonData);

                if (!handlePushes(context, jsonData) ||
                        !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.enable_push_messages), true))
                return;

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
                case Constants.ParsePush.COMPLETE_CONVERSATION_REQUEST:
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
        messagingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(messagingIntent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }
}

