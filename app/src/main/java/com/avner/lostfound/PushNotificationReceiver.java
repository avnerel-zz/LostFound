package com.avner.lostfound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by avner on 04/05/2015.
 */
public class PushNotificationReceiver extends ParsePushBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

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
        // TODO: Avner, when a message is added the json object will hold: recipientId, itemId, messageText. just like you asked
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
        super.onPushOpen(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }
}

