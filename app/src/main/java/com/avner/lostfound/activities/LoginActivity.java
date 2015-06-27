package com.avner.lostfound.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;


public class LoginActivity extends Activity implements Button.OnClickListener{

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        // already logged in.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            finishLogin(false);
            return;
        }
        setContentView(R.layout.activity_login);

        // hide action bar because it isn't needed.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initViews();
    }


    private void initViews() {

        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.b_login_facebook);
        facebookLoginButton.setOnClickListener(this);
    }

    /**
     * @param updateDB if true, then the local datastore will be updated
     */
    private void finishLogin(boolean updateDB) {

        if (updateDB) {

            final LostFoundApplication application = (LostFoundApplication) getApplication();

            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... v) {
                    application.refreshLocalDataStore();
                    return null;
                }

                @Override
                protected void onPostExecute(Void v) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }.execute();
        }else{
            if(progressDialog != null){

                progressDialog.dismiss();
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {

            Log.e(Constants.LOST_FOUND_TAG, "result not o.k in login activity. request code: " + requestCode);
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_FACEBOOK_LOGIN) {

            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.b_login_facebook) {

            loginWithFacebookButton();
        }
    }

    /**
     * This method is called when the facebook login button is pressed and it tries to login to facebook.
     */
    private void loginWithFacebookButton() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Initializing");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, null, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if(err != null){
                    Toast.makeText(getApplicationContext(), "problem connection to facebook, please check your connection.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }
                if (user == null) {
                    Log.d(Constants.LOST_FOUND_TAG, "Uh oh. The user cancelled the Facebook login.");
                    progressDialog.dismiss();
                } else if (user.isNew()) {
                    Log.d(Constants.LOST_FOUND_TAG, "User signed up and logged in through Facebook!");

                    if (ParseUser.getCurrentUser() == null) {
                        Log.d(Constants.LOST_FOUND_TAG, "User logged in through Facebook but no Current Parse user!");
                    }
                    logInToParseWithFacebook();
                } else {
                    Log.d(Constants.LOST_FOUND_TAG, "User logged in through Facebook!");
                    logInToParseWithFacebook();
                }
            }
        });
    }

    /**
     * This method logs the user in to parse with his facebook credentials.
     */
    private void logInToParseWithFacebook() {
        getUserFacebookProfileDetails();
        finishLogin(true);
    }

    private void getUserFacebookProfileDetails() {

        String name = Profile.getCurrentProfile().getName();
        ParseUser user = ParseUser.getCurrentUser();
        user.put(Constants.ParseUser.USER_DISPLAY_NAME, name);
        user.saveInBackground();

    }

}
