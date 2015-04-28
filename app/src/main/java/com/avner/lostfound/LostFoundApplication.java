package com.avner.lostfound;

import android.app.Application;
import android.content.Intent;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by avner on 25/04/2015.
 */
public class LostFoundApplication extends Application {

    private static final String PARSE_APPLICATION_ID = "63FMI3OgLxnxl938rnjFf6vnmgOFsBlIop9jC6VQ";

    private static final String PARSE_CLIENT_KEY = "FWxMwbcifgveMVSo9JliPvunXzshxtchnuz1e9ZE";

    private String userName;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParseUser user = ParseUser.getCurrentUser();

        if(user!= null){

            setUserName(user.getUsername());
        }

    }

    //TODO need to get userNAme and password from local DB, if present.
    public void setUserName(String username){

        this.userName = username;

    }

    public String getUserName() {

        return userName.split("@")[0];
    }

    public String getUserEmail() {

        return userName;
    }

}
