package com.avner.lostfound;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;



public class LoginActivity extends Activity implements Button.OnClickListener{

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
        password = (EditText) findViewById(R.id.et_user_password);


    }

    private void finishLogin() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(intent);

        // start messaging service.
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);

        startService(serviceIntent);

        finish();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode == Constants.SIGN_UP_SUCCESSFUL){

            String userName = data.getStringExtra(Constants.USER_NAME);

            String password = data.getStringExtra(Constants.PASSWORD);

            logInToParseWithAppLogin(userName, password);

        }else if(requestCode == Constants.FACEBOOK_LOGIN_REQUEST_ID){

            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.b_sign_up){

            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

            startActivityForResult(intent, Constants.SIGN_UP_REQUEST_ID);

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
        finishLogin();
    }

    private void getUserDetails() {

        String name = Profile.getCurrentProfile().getName();

        ParseUser user = ParseUser.getCurrentUser();

        user.put("name", name);

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
                    user.put("name", userName.split("@")[0]);
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
}
