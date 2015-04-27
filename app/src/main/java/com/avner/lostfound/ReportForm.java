package com.avner.lostfound;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;


public class ReportForm extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private EditText et_itemName;
    private ImageButton b_pick_time_and_date;
    private ImageButton b_pick_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);


        b_pick_time_and_date = (ImageButton) findViewById(R.id.b_pick_time_date);
        b_pick_time_and_date.setOnClickListener(this);

        b_pick_location = (ImageButton) findViewById(R.id.b_pick_location);
        b_pick_time_and_date.setOnClickListener(this);

        spinner = (Spinner) findViewById(R.id.s_choose_item);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_items, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        et_itemName = (EditText) findViewById(R.id.et_itemName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_form, menu);
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
        if (v.getId() == R.id.b_pick_time_date) {

            Dialog dialog = new Dialog(getApplicationContext());
            dialog.setContentView(R.layout.time_date_picker);
            dialog.setTitle("Custom Dialog");

        } else if (v.getId() == R.id.b_pick_location) {

//            Dialog dialog = new Dialog(getApplicationContext());
//            dialog.setContentView(R.layout.location_picker);
//            dialog.setTitle("Custom Dialog");

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if ("Other..".equals(spinner.getSelectedItem().toString())) {
            et_itemName.setVisibility(View.VISIBLE);
        } else {
            et_itemName.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
