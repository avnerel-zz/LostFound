package com.avner.lostfound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

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
        String pushType = (String) jsonData.get("pushType");
        Log.d("PUSH_RECEIVED", "pushType: " + pushType);
        switch (pushType) {
            case "ParseMessage":
                handlePushOfParseMessage(jsonData);

                if (((LostFoundApplication) context.getApplicationContext()).isMessagingActivityActive()) {
                    // no need for popup notification. already in messaging session.
                    return false;
                }
                break;
            case "ParseLost":
                handlePushOfParseLost(jsonData);
                return false;
            case "ParseFound":
                handlePushOfParseFound(jsonData);
                return false;
        }
        return true;
    }

    private void handlePushOfParseFound(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get("reportedItem");
        Log.d("PUSH_FOUND", reportedItem);
        // TODO: create object from the data, as in handlePushOfParseLost
    }

    private void handlePushOfParseLost(JSONObject jsonData) throws JSONException {
        String reportedItem = (String) jsonData.get("reportedItem");
        Log.d("PUSH_LOST", reportedItem);
        JSONObject report = new JSONObject(reportedItem);

//        StringBuilder sb = new StringBuilder("KeySet: [");
//        String tmp = null;
//        for (Iterator iterator = report.keys(); iterator.hasNext(); ) {
//            tmp = iterator.next().toString();
//            sb.append(tmp + "=\"" + report.get(tmp) + "\"");
//            if (iterator.hasNext())
//                sb.append(",");
//        }
//        Log.d("PUSH_LOST", sb.append("]").toString());

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

                if (e == null) {
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

                } else {
                    Log.d("PUSH_LOST", "Could not get the data from server");
                    // could not retrieve the object from the server
                    removeReportFromLocalDatastoreIfExists(objectId);
                }
            }
        });

//        ParseObject parseReport = new ParseObject(Constants.ParseObject.PARSE_LOST);
//        parseReport.put(Constants.ParseReport.IS_LOST, report.get(Constants.ParseReport.IS_LOST));
//        parseReport.put(Constants.ParseReport.ITEM_DESCRIPTION, report.get(Constants.ParseReport.ITEM_DESCRIPTION));
//        parseReport.put(Constants.ParseReport.ITEM_NAME, report.get(Constants.ParseReport.ITEM_NAME));
//        parseReport.put(Constants.ParseReport.ITEM_IMAGE, report.get(Constants.ParseReport.ITEM_IMAGE));
//        parseReport.put(Constants.ParseReport.LOCATION_STRING, report.get(Constants.ParseReport.LOCATION_STRING));
//        parseReport.put(Constants.ParseReport.TIME, report.get(Constants.ParseReport.TIME));
//        parseReport.put(Constants.ParseReport.USER_ID, report.get(Constants.ParseReport.USER_ID));
//        parseReport.put(Constants.ParseReport.USER_DISPLAY_NAME, report.get(Constants.ParseReport.USER_DISPLAY_NAME));
//        try {
//            parseReport.put(Constants.ParseReport.LOCATION, report.get(Constants.ParseReport.LOCATION));
//        } catch (JSONException e) {
//            // no location was sent
//        }
//        parseReport.put(Constants.ParseReport.USER_DISPLAY_NAME, report.get(Constants.ParseReport.USER_DISPLAY_NAME));
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
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseLost");
        query.fromLocalDatastore();
        query.getInBackground(objectId, new GetCallback<ParseObject>(){

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


    private void handlePushOfParseMessage(JSONObject jsonData) {
        // TODO: Avner, when a message is added the json object will hold: recipientId, itemId, messageText. just like you asked
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        if (intent.hasExtra("com.parse.Data")) {
            String stringData = intent.getStringExtra("com.parse.Data");

            JSONObject jsonData = null;
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
        super.onPushOpen(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }
}

