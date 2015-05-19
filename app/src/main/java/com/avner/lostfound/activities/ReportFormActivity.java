package com.avner.lostfound.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class ReportFormActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CompoundButton.OnCheckedChangeListener {

    private Spinner spinner;
    private TextView et_itemName;
    private ImageButton b_pick_date;
    private TextView tv_date_picker;
    private ImageButton b_pick_time;
    private TextView tv_time_picker;
    private ImageButton b_pick_location;
    private GoogleApiClient googleApiClient;
    private LatLng location_chosen;
    private CheckBox cb_with_location;
    private TextView tv_location_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);

        initViews();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void updateCurrentLocation() {
        if(location_chosen == null){

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            location_chosen = new LatLng(location.getLatitude(), location.getLongitude());

            String locationAsString = getLocationFromCoordinates(location_chosen);
            tv_location_picker.setText(locationAsString);
        }
    }

    /**
     * initializing views and onClickListeners and setting default values.
     */
    private void initViews() {

        Calendar currentDate = new GregorianCalendar();

        b_pick_date = (ImageButton) findViewById(R.id.b_pick_date);
        b_pick_date.setOnClickListener(this);
        tv_date_picker = (TextView) findViewById(R.id.tv_date_picker);
        tv_date_picker.setText((currentDate.get(Calendar.DAY_OF_MONTH) + "-" + (currentDate.get(Calendar.MONTH) + 1) + "-" + currentDate.get(Calendar.YEAR)));

        b_pick_time = (ImageButton) findViewById(R.id.b_pick_time);
        b_pick_time.setOnClickListener(this);
        tv_time_picker = (TextView) findViewById(R.id.tv_time_picker);
        tv_time_picker.setText(String.format("%02d", currentDate.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", currentDate.get(Calendar.MINUTE)));

        b_pick_location = (ImageButton) findViewById(R.id.b_pick_location);
        b_pick_location.setOnClickListener(this);

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

        cb_with_location = (CheckBox) findViewById(R.id.cb_with_location);
        cb_with_location.setOnCheckedChangeListener(this);

        tv_location_picker = (TextView) findViewById(R.id.tv_location_picker);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_form, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_pick_date) {

            getDate();

        } else if (v.getId() == R.id.b_pick_time) {

            getTime();

        } else if (v.getId() == R.id.b_pick_location) {
            getLocation();
        }
    }

    private void getDate() {
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

        // setting max date to be today's date so user can't insert a date in the future.
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
    }

    private void getTime() {
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
    }

    private void getLocation() {
        Intent intent = new Intent(this, PickLocationActivity.class);

        double latitude;
        double longitude;

        if(location_chosen != null){
            latitude = location_chosen.latitude;
            longitude = location_chosen.longitude;

        }else{
            Log.d("my_tag", "last known location is null");
            latitude=32.7734607;
            longitude=35.0320228;
        }
        intent.putExtra(Constants.LATITUDE, latitude);
        intent.putExtra(Constants.LONGITUDE, longitude);
        startActivityForResult(intent, Constants.PICK_LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == Constants.PICK_LOCATION_REQUEST_CODE){

            location_chosen = new LatLng(data.getDoubleExtra(Constants.LATITUDE,0),data.getDoubleExtra(Constants.LONGITUDE,0));

            String location = getLocationFromCoordinates(location_chosen);

            tv_location_picker.setText(location);

        }
    }

    private String getLocationFromCoordinates(LatLng location_chosen) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses  = null;
        String addressLine = "";

        try {
            addresses = geocoder.getFromLocation(location_chosen.latitude,location_chosen.longitude, 1);
            Address address = addresses.get(0);
            for(int i=0; i<address.getMaxAddressLineIndex(); i++){

                addressLine += address.getAddressLine(i) + ", ";
            }
            addressLine += address.getAddressLine(address.getMaxAddressLineIndex());

        } catch (IOException e) {
            addressLine = "Description not Available";
        }

        return addressLine;
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

    @Override
    public void onConnected(Bundle bundle) {

        updateCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        if(isChecked){

            b_pick_location.setEnabled(true);
            tv_location_picker.setEnabled(true);
            updateCurrentLocation();

        }else{

            b_pick_location.setEnabled(false);
            tv_location_picker.setEnabled(false);
            tv_location_picker.setText("No location specified");
            location_chosen = null;
        }
    }
}
