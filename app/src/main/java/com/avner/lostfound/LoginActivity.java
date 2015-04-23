package com.avner.lostfound;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;


public class LoginActivity extends Activity implements Button.OnClickListener{

    Button signUpButton;

    Button emailLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        signUpButton = (Button) findViewById(R.id.b_sign_up);
        signUpButton.setOnClickListener(this);

        emailLoginButton= (Button) findViewById(R.id.b_email_login);
        emailLoginButton.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

        if(v.getId() == R.id.b_sign_up){

            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

            startActivity(intent);
        }

        if(v.getId() == R.id.b_email_login){

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            startActivity(intent);
        }

    }
}
