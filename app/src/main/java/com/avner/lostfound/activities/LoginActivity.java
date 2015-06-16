package com.avner.lostfound.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.avner.lostfound.messaging.MessageService;
import com.avner.lostfound.utils.ImageUtils;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.io.File;


public class LoginActivity extends Activity implements Button.OnClickListener{

    private Button emailLoginButton;

    private EditText userName;
    private EditText password;
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

//        Button signUpButton = (Button) findViewById(R.id.b_sign_up);
//        signUpButton.setOnClickListener(this);
//
//        emailLoginButton = (Button) findViewById(R.id.b_email_login);
//        emailLoginButton.setOnClickListener(this);

        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.b_login_facebook);
        facebookLoginButton.setOnClickListener(this);

//        userName = (EditText) findViewById(R.id.et_user_name);
//        userName.addTextChangedListener(this);
//        password = (EditText) findViewById(R.id.et_user_password);
//        password.addTextChangedListener(this);
    }

    /**
     * @param updateDB if true, then the local datastore will be updated
     */
    private void finishLogin(boolean updateDB) {

        // start messaging service.
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);

        startService(serviceIntent);

        if (updateDB) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Initializing");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void fetchUserPhotoFromFacebookProfile() {
        Profile currentProfile = Profile.getCurrentProfile();
        final Uri imageUri = currentProfile != null ? currentProfile.getProfilePictureUri(150, 150) : null;
        if (imageUri != null) {

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    saveFacebookProfilePictureToFile(imageUri.toString());
                    return null;
                }
            };
            task.execute();
        }
    }

    public static void saveFacebookProfilePictureToFile(String imageUri) {

        // make dir for the app if it isn't already created.
        boolean success = (new File(Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME)).mkdir();
        if (!success) {
            Log.d(Constants.LOST_FOUND_TAG, "directory already created");
        }
        Bitmap image = ImageUtils.decodeRemoteUrl(imageUri);
        ImageUtils.saveImageToFile(image, Constants.USER_IMAGE_FILE_NAME);

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
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, null, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if(err != null){
                    Toast.makeText(getApplicationContext(), "problem connection to facebook, please check your connection.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user == null) {
                    Log.d(Constants.LOST_FOUND_TAG, "Uh oh. The user cancelled the Facebook login.");
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
        // try to fetch user photo from facebook.
        fetchUserPhotoFromFacebookProfile();
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Loading");
//        progressDialog.setMessage("Please wait...");
//        progressDialog.show();
        finishLogin(true);
//        progressDialog.dismiss();
    }

    private void getUserFacebookProfileDetails() {

        String name = Profile.getCurrentProfile().getName();
        ParseUser user = ParseUser.getCurrentUser();
        user.put(Constants.ParseUser.USER_DISPLAY_NAME, name);

        //TODO get email.

        user.saveInBackground();

    }

//    /**
//     * This method logs the user into parse with the app user name and password.
//     *
//     * @param userName user name entered.
//     * @param password password entered.
//     */
//    private void logInToParseWithAppLogin(final String userName, String password) {
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Loading");
//        progressDialog.setMessage("Please wait...");
//        progressDialog.show();
//
//        ParseUser.logInInBackground(userName, password, new LogInCallback() {
//            public void done(ParseUser user, com.parse.ParseException e) {
//                if (user != null) {
//
//                    finishLogin(true);
//                    user.setEmail(userName);
//                    if(user.get(Constants.ParseUser.USER_DISPLAY_NAME)==null){
//
//                        user.put(Constants.ParseUser.USER_DISPLAY_NAME, userName.split("@")[0]);
//                    }
//                    user.saveInBackground();
//                    ((LostFoundApplication) getApplication()).setUserName(userName);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            e.getLocalizedMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//                progressDialog.dismiss();
//            }
//        });
//    }

}
