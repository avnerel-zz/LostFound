package com.avner.lostfound;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by avner on 25/04/2015.
 */
public class LostFoundApplication extends Application {

    private static final String PARSE_APPLICATION_ID = "63FMI3OgLxnxl938rnjFf6vnmgOFsBlIop9jC6VQ";

    private static final String PARSE_CLIENT_KEY = "FWxMwbcifgveMVSo9JliPvunXzshxtchnuz1e9ZE";

    private String userName;

    private ParseInstallation installation;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Data store to save things locally.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        ParseFacebookUtils.initialize(this, Constants.FACEBOOK_LOGIN_REQUEST_ID);

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

    public String getUserName() {

        return (String)ParseUser.getCurrentUser().get("name");
    }

    public String getUserEmail() {

        return (String)ParseUser.getCurrentUser().getEmail();
    }

}
