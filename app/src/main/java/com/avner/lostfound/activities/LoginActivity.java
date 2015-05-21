package com.avner.lostfound.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.ImageUtils;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.avner.lostfound.messaging.MessageService;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.io.File;
import java.util.Arrays;
import java.util.List;



public class LoginActivity extends Activity implements Button.OnClickListener, TextWatcher {

    private Button signUpButton;

    private Button emailLoginButton;

    private LoginButton facebookLoginButton;

    private EditText userName;
    private EditText password;
    private ProgressDialog progressDialog;

    private static final List<String> PERMISSIONS = Arrays.asList(
            "email");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        // already logged in.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {

            finishLogin();

            return;
        }

        setContentView(R.layout.activity_login);

        // hide action bar because it isn't needed.
        getActionBar().hide();

        signUpButton = (Button) findViewById(R.id.b_sign_up);
        signUpButton.setOnClickListener(this);

        emailLoginButton= (Button) findViewById(R.id.b_email_login);
        emailLoginButton.setOnClickListener(this);

        facebookLoginButton = (LoginButton) findViewById(R.id.b_login_facebook);
        facebookLoginButton.setOnClickListener(this);

        userName = (EditText) findViewById(R.id.et_user_name);
        userName.addTextChangedListener(this);
        password = (EditText) findViewById(R.id.et_user_password);
        password.addTextChangedListener(this);


    }

    private void finishLogin() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(intent);

        // start messaging service.
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);

        startService(serviceIntent);

        finish();

    }

    private void fetchUserPhotoFromFacebookProfile() {
        Profile currentProfile = Profile.getCurrentProfile();

        final Uri imageUri = currentProfile != null ? currentProfile.getProfilePictureUri(150, 150): null;

        if(imageUri!= null){

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
        boolean success = (new File( Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME)).mkdir();
        if (!success)
        {
            Log.d("my_tag", "directory already created");
        }

        Bitmap image = ImageUtils.decodeRemoteUrl(imageUri);

        ImageUtils.saveImageToFile(image,Constants.USER_IMAGE_FILE_NAME);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode != RESULT_OK){

            Log.e(Constants.LOST_FOUND_TAG, "result not o.k in login activity. request code: " + requestCode);
        }
        if(requestCode == Constants.REQUEST_CODE_SIGN_UP){

            String userName = data.getStringExtra(Constants.USER_NAME);

            String password = data.getStringExtra(Constants.PASSWORD);

            logInToParseWithAppLogin(userName, password);

        }else if(requestCode == Constants.REQUEST_CODE_FACEBOOK_LOGIN){

            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        }
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.b_sign_up){

            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

            startActivityForResult(intent, Constants.REQUEST_CODE_SIGN_UP);

        } else if(v.getId() == R.id.b_email_login){


            String username = userName.getText().toString().toLowerCase();
            String pass = password.getText().toString();

            logInToParseWithAppLogin(username, pass);
        } else if(v.getId() == R.id.b_login_facebook){

            ParseFacebookUtils.logInWithReadPermissionsInBackground(this, null, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    if (user == null) {
                        Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    } else if (user.isNew()) {
                        Log.d("MyApp", "User signed up and logged in through Facebook!");

                        if(ParseUser.getCurrentUser()==null){
                            Log.d("MyApp", "User logged in through Facebook but no Current Parse user!");
                        }
                        logInToParseWithFacebook();
                    } else {
                        Log.d("MyApp", "User logged in through Facebook!");
                        logInToParseWithFacebook();
                    }
                }
            });
        }

    }

    private void logInToParseWithFacebook() {
        getUserDetails();

        // try to fetch user photo from facebook.
        fetchUserPhotoFromFacebookProfile();
        finishLogin();
    }

    private void getUserDetails() {

        String name = Profile.getCurrentProfile().getName();

        ParseUser user = ParseUser.getCurrentUser();

        user.put(Constants.USER_DISPLAY_NAME, name);

        //TODO get email.

        user.saveInBackground();

    }

    private void logInToParseWithAppLogin(final String userName, String password) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            public void done(ParseUser user, com.parse.ParseException e) {

                progressDialog.dismiss();

                if (user != null) {

                    finishLogin();
                    user.setEmail(userName);
                    user.put(Constants.USER_DISPLAY_NAME, userName.split("@")[0]);
                    user.saveInBackground();
                    ((LostFoundApplication)getApplication()).setUserName(userName);
                } else {
                    Toast.makeText(getApplicationContext(),
                           e.getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(password.getText().toString().isEmpty() || userName.getText().toString().isEmpty() ){
            emailLoginButton.setEnabled(false);
        }else{
            emailLoginButton.setEnabled(true);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
