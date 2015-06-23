package com.avner.lostfound;

import android.app.Application;
import android.util.Log;

import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.activities.MessagingActivity;
import com.avner.lostfound.utils.SignalSystem;
import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by avner on 25/04/2015.
 */
public class LostFoundApplication extends Application {

    private static final String PARSE_APPLICATION_ID = "63FMI3OgLxnxl938rnjFf6vnmgOFsBlIop9jC6VQ";

    private static final String PARSE_CLIENT_KEY = "FWxMwbcifgveMVSo9JliPvunXzshxtchnuz1e9ZE";

    private String userName;

    /**
     * used to get push notifications
     */
    private ParseInstallation installation;
    private String messagingItemId;
    private MainActivity mainActivity;
    private String messagingRecipientId;
    private MessagingActivity messagingActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Data store to save things locally.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        ParseFacebookUtils.initialize(this, Constants.REQUEST_CODE_FACEBOOK_LOGIN);

        FacebookSdk.sdkInitialize(this);
        // installation used for sending push notifications.
        installation = ParseInstallation.getCurrentInstallation();

        ParseUser user = ParseUser.getCurrentUser();

        if (user != null && !user.getObjectId().equals(installation.get("user"))) {

            installation.put("user", user.getObjectId());
            installation.saveInBackground();

            Log.d("messaging", "put installation user id: " + user.getObjectId());
        }

        SignalSystem.initialize(this);


    }

    public void updateMessagingStatus(MessagingActivity messagingActivity, String itemId, String recipientId) {

        this.messagingActivity = messagingActivity;
        messagingItemId = itemId;
        messagingRecipientId = recipientId;

    }

    public MessagingActivity getMessagingActivity() {
        return messagingActivity;
    }

    public String getMessagingActivityItemId() {
        return messagingItemId;
    }

    public void refreshLocalDataStore() {
        try {
            ParseObject.unpinAll();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String userId = ParseUser.getCurrentUser().getObjectId();

        getInstancesOfParseObjectFromRemoteDB(Constants.ParseObject.PARSE_LOST);
        getInstancesOfParseObjectFromRemoteDB(Constants.ParseObject.PARSE_CONVERSATION, true, Constants.ParseConversation.MY_USER_ID, userId);
        getInstancesOfParseObjectFromRemoteDB(Constants.ParseObject.PARSE_MESSAGE, true, Constants.ParseMessage.RECIPIENT_ID, userId);
        getInstancesOfParseObjectFromRemoteDB(Constants.ParseObject.PARSE_MESSAGE, true, Constants.ParseMessage.SENDER_ID, userId);
    }

    private void getInstancesOfParseObjectFromRemoteDB(String objectName) {
        getInstancesOfParseObjectFromRemoteDB(objectName, false, null, null);
    }

    private void getInstancesOfParseObjectFromRemoteDB(String objectName, boolean takeUserObjectsOnly, String usernameFieldName, String myUsername) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(objectName);

        if (takeUserObjectsOnly) {
            query.whereEqualTo(usernameFieldName, myUsername);
        }

        try {
            ParseObject.pinAllInBackground(query.find());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getMessagingRecipientId() {
        return messagingRecipientId;
    }
}
