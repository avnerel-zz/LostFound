package com.avner.lostfound.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.regex.Pattern;


public class SignUpActivity extends Activity implements View.OnClickListener, TextWatcher {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private Button signUp;
    private EditText userName;
    private EditText pass;
    private EditText passwordRetyped;

    private Pattern pattern;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getActionBar().hide();

        signUp = (Button) findViewById(R.id.b_sign_up);
        signUp.setOnClickListener(this);

        userName = (EditText) findViewById(R.id.et_user_name);
        userName.addTextChangedListener(this);
        pass = (EditText) findViewById(R.id.et_user_password);
        pass.addTextChangedListener(this);
        passwordRetyped = (EditText) findViewById(R.id.et_user_retype_password);
        passwordRetyped.addTextChangedListener(this);

        pattern = Pattern.compile(EMAIL_PATTERN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.b_sign_up){

            final String username = userName.getText().toString().toLowerCase();
            final String password = this.pass.getText().toString();
            final String reTypedPassword = this.passwordRetyped.getText().toString();

            if (! checkValidityOfUserDetails(username, password, reTypedPassword)) {
                return;
            }

            signInToParse(username, password);
        }
    }

    private boolean checkValidityOfUserDetails(String username, String password, String reTypedPassword) {

        boolean validDetails = true;

        String errorMessage ="";

        if(password.length() < MIN_PASSWORD_LENGTH){
            validDetails = false;
            errorMessage = "Password too short, should be 6 characters or more";
        }
        //passwords don't match.
        if(!password.equals(reTypedPassword)){

            validDetails = false;
            errorMessage = "passwords don't match";
        }

        if(!checkUserName(username)){

            validDetails=false;
            errorMessage = "email isn't in correct form.";
        }
        if(!validDetails){

            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        return validDetails;
    }

    private void signInToParse(final String username, final String pass) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(pass);

        user.signUpInBackground(new SignUpCallback() {
            public void done(com.parse.ParseException e) {

                progressDialog.dismiss();

                if (e == null) {
                    Intent intent = getIntent();

                    intent.putExtra(Constants.USER_NAME, username);
                    intent.putExtra(Constants.PASSWORD, pass);

                    setResult(RESULT_OK, intent);

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            e.getLocalizedMessage()
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkUserName(String username) {

        return pattern.matcher(username).matches();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(passwordRetyped.getText().toString().isEmpty() || pass.getText().toString().isEmpty()
                || userName.getText().toString().isEmpty() ){
            signUp.setEnabled(false);
        }else{
            signUp.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
