package com.avner.lostfound.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;

public class SettingsActivity extends Activity implements AdapterView.OnItemSelectedListener{

    private static final String INFINITY = "\u221E";
    Spinner messageHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        messageHistory = (Spinner)findViewById(R.id.sp_history_length);

        String[] items = new String[]{"30", "50", "100", INFINITY};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.message_history_spinner_item, items);

        messageHistory.setAdapter(adapter);

        messageHistory.setOnItemSelectedListener(this);

        TextView userName = (TextView) findViewById(R.id.tv_userName);

        LostFoundApplication app = (LostFoundApplication) getApplication();

        userName.setText(app.getUserEmail());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
