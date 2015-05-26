package com.avner.lostfound;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
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
    private boolean isMessagingActive;
    private String messagingItemId;

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

        if(user!= null){

            setUserName(user.getUsername());

            installation.put("user",user.getObjectId());

            Log.d("messaging","put installation user id: " + user.getObjectId());
        }

        installation.saveInBackground();

    }

    //TODO need to get userName and password from local DB, if present.
    public void setUserName(String username){

        this.userName = username;

        ParseUser user = ParseUser.getCurrentUser();

        if(user!=null){

            installation.put("user",user.getObjectId());
            installation.saveInBackground();
            Log.d("messaging","put installation user id: " + user.getObjectId());
        }

    }

    public String getUserDisplayName() {

        return (String)ParseUser.getCurrentUser().get(Constants.ParseUser.USER_DISPLAY_NAME);
    }

    public String getUserEmail() {

        return (String)ParseUser.getCurrentUser().getEmail();
    }

    public void updateMessagingStatus(boolean isActive, String itemId) {

        isMessagingActive = isActive;
        messagingItemId = itemId;

    }



    public boolean isMessagingActivityActive() {
        return isMessagingActive;
    }

    public String getMessagingActivityItemId() {
        return messagingItemId;
    }
}
