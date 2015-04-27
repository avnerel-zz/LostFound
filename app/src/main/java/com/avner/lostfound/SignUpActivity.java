package com.avner.lostfound;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpActivity extends Activity implements View.OnClickListener {

    private Button signUp;
    private EditText userName;
    private EditText password;
    private EditText passwordRetyped;

    private Pattern pattern;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getActionBar().hide();

        signUp = (Button) findViewById(R.id.b_sign_up);
        signUp.setOnClickListener(this);

        userName = (EditText) findViewById(R.id.et_user_name);
        password = (EditText) findViewById(R.id.et_user_password);
        passwordRetyped = (EditText) findViewById(R.id.et_user_retype_password);

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

            //passwords don't match.
            if(!password.getText().toString().equals(passwordRetyped.getText().toString())){

                Toast.makeText(getApplicationContext(),
                        "passwords don't match"
                        , Toast.LENGTH_LONG).show();
                return;
            }

            //sign up to Parse - maybe move this to app class TODO
            final String username = userName.getText().toString().toLowerCase();
            if(!checkuserName(username)){
                Toast.makeText(getApplicationContext(),
                        "email isn't in correct form."
                        , Toast.LENGTH_SHORT).show();
                return;
            }
            final String pass = password.getText().toString();

            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(pass);

            user.signUpInBackground(new SignUpCallback() {
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Intent intent = getIntent();

                        intent.putExtra(Constants.USER_NAME, username);
                        intent.putExtra(Constants.PASSWORD, pass);

                        setResult(Constants.SIGN_UP_SUCCESSFUL, intent);

                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "There was an error signing up."
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean checkuserName(String username) {

        return pattern.matcher(username).matches();
    }
}
