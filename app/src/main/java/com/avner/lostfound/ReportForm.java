package com.avner.lostfound;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class ReportForm extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private TextView et_itemName;
    private ImageButton b_pick_date;
    private TextView tv_date_picker;
    private ImageButton b_pick_time;
    private TextView tv_time_picker;
    private TextView tv_location_picker;
    private ImageButton b_pick_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);


        b_pick_date = (ImageButton) findViewById(R.id.b_pick_date);
        b_pick_date.setOnClickListener(this);
        tv_date_picker = (TextView) findViewById(R.id.tv_date_picker);

        b_pick_time = (ImageButton) findViewById(R.id.b_pick_time);
        b_pick_time.setOnClickListener(this);
        tv_time_picker = (TextView) findViewById(R.id.tv_time_picker);

        b_pick_location = (ImageButton) findViewById(R.id.b_pick_location);
        b_pick_location.setOnClickListener(this);
        tv_location_picker = (TextView) findViewById(R.id.tv_location_picker);

        spinner = (Spinner) findViewById(R.id.s_choose_item);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.default_items, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        et_itemName = (TextView) findViewById(R.id.et_itemName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_form, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_pick_date) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            tv_date_picker.setText(dayOfMonth + "-"
                                    + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            dpd.show();

        } else if (v.getId() == R.id.b_pick_time) {

            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);

            TimePickerDialog tpd = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            tv_time_picker.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                        }
                    }, mHour, mMinute, false);
            tpd.show();

        } else if (v.getId() == R.id.b_pick_location) {
            Dialog placePicker = new Dialog(this);
            placePicker.setContentView(R.layout.location_picker_dialog);
            placePicker.setTitle("Hi");
            placePicker.show();
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
