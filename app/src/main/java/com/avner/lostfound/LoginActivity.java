package com.avner.lostfound;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.parse.LogInCallback;
import com.parse.ParseUser;


public class LoginActivity extends Activity implements Button.OnClickListener{

    private Button signUpButton;

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

            logInToParse(userName,password);

        }
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.b_sign_up){

            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

            startActivityForResult(intent, Constants.SIGN_UP_REQUEST_ID);
        }

        if(v.getId() == R.id.b_email_login){


            String username = userName.getText().toString().toLowerCase();
            String pass = password.getText().toString();

            logInToParse(username,pass);
        }

    }

    private void logInToParse(final String userName, String password) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            public void done(ParseUser user, com.parse.ParseException e) {

                progressDialog.dismiss();

                if (user != null) {

                    finishLogin();
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
