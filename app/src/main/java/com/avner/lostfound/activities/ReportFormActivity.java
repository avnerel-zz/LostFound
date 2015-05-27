package com.avner.lostfound.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.utils.ImageUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ReportFormActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CompoundButton.OnCheckedChangeListener, MenuItem.OnMenuItemClickListener {

    private static final String ITEM_IMAGE_NAME = "itemImage.png";

    private Spinner spinner;
    private TextView et_itemName;
    private ImageButton ib_pick_date;
    private TextView tv_date_picker;
    private ImageButton ib_pick_time;
    private TextView tv_time_picker;
    private ImageButton ib_pick_location;
    private ImageButton ib_item_photo;
    private GoogleApiClient googleApiClient;
    private LatLng location_chosen;
    private CheckBox cb_with_location;
    private TextView tv_location_picker;
    private EditText et_description;
    private Calendar timeChosen;

    private Button submitButton;

    private ParseFile parseItemImage;
    /**
     * This field indicates if the report is for a lost item or a found item.
     */
    private boolean lostReport;
    /**
     * This field indicates if the report is new or an edit of a previous report.
     */
    private boolean editReport;
    private Item itemEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);

        initViews();

        lostReport = getIntent().getExtras().getBoolean(Constants.ReportForm.IS_LOST_FORM);
        editReport = getIntent().getExtras().getBoolean(Constants.ReportForm.IS_EDIT_FORM);

        if (editReport){
            itemEdited = (Item) getIntent().getExtras().getParcelable(Constants.ReportForm.ITEM);
            loadFromItem(itemEdited);
        } else {
            setDefaultValues();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    private void setDefaultValues() {
        setTime(Calendar.getInstance());
    }

    private void loadFromItem(Item itemEdited) {

        setTime(itemEdited.getTime());

        //setting picture
        Picasso.with(this).load(itemEdited.getImageUrl()).into(ib_item_photo);

        et_description.setText(itemEdited.getDescription());
        Location location = itemEdited.getLocation();
        if (location != null) {
            location_chosen = new LatLng(location.getLatitude(), location.getLongitude());
            tv_location_picker.setText(itemEdited.getLocationString());
            cb_with_location.setChecked(true);
        }else{
            cb_with_location.setChecked(false);

        }

        boolean found = false;
        // go over all spinner items except for other... and if a match is found than choose it.
        int lastSpinnerItemPosition =  spinner.getCount() - 1;
        for(int i=0; i< lastSpinnerItemPosition; i++) {

            if (itemEdited.getName().equals(spinner.getItemAtPosition(i))) {

                found = true;
                spinner.setSelection(i);
            }
        }
        //set spinner to other...
        if(!found){
            spinner.setSelection(lastSpinnerItemPosition);
            et_itemName.setText(itemEdited.getName());
        }
    }

    private void updateCurrentLocation() {
        if (location_chosen == null){
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (null == location) { // failed to get a location
                tv_location_picker.setText(Constants.Geocoder.NO_LOCATION_AVAILABLE);
                return;
            }

            location_chosen = new LatLng(location.getLatitude(), location.getLongitude());

            String locationAsString = getLocationFromCoordinates(location_chosen);
            tv_location_picker.setText(locationAsString);
        }
    }

    /**
     * initializing views and onClickListeners and setting default values.
     */
    private void initViews() {

        initTimeViews();
        initLocationViews();
        initItemSelector();

        submitButton = (Button) findViewById(R.id.b_submit);
        submitButton.setOnClickListener(this);
        et_description = (EditText) findViewById(R.id.et_description);
        initItemImage();
    }

    private void initItemImage() {
        ib_item_photo = (ImageButton) findViewById(R.id.ib_item_image);
        ib_item_photo.setOnClickListener(this);
    }

    private void initItemSelector() {
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

    private void initLocationViews() {
        ib_pick_location = (ImageButton) findViewById(R.id.b_pick_location);
        ib_pick_location.setOnClickListener(this);

        cb_with_location = (CheckBox) findViewById(R.id.cb_with_location);
        cb_with_location.setOnCheckedChangeListener(this);

        tv_location_picker = (TextView) findViewById(R.id.tv_location_picker);
    }

    private void initTimeViews() {

        ib_pick_date = (ImageButton) findViewById(R.id.b_pick_date);
        ib_pick_date.setOnClickListener(this);
        tv_date_picker = (TextView) findViewById(R.id.tv_date_picker);

        ib_pick_time = (ImageButton) findViewById(R.id.b_pick_time);
        ib_pick_time.setOnClickListener(this);
        tv_time_picker = (TextView) findViewById(R.id.tv_time_picker);
    }

    private void setTime(Calendar calendar) {
        setDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        timeChosen = Calendar.getInstance();
    }

    private void setDate(int day, int month, int year) {
        tv_date_picker.setText(day+ "-" + month+ "-" + year);
    }

    private void setTime(int hour, int minute) {
        tv_time_picker.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_form, menu);
        MenuItem submit = menu.findItem(R.id.action_send);
        submit.setOnMenuItemClickListener(this);
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

        switch(v.getId()){
            case  R.id.b_pick_date:
                getDate();
                break;
            case  R.id.b_pick_time:
                getTime();
                break;
            case  R.id.b_pick_location:
                getLocation();
                break;
            case  R.id.b_submit:
                submitReport();
                break;
            case R.id.ib_item_image:
                ImageUtils.selectItemImage(this);
                break;
            default:
                Log.e(Constants.LOST_FOUND_TAG, "clicked on something weird!!");
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
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tv_date_picker.setText(dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year);
                        timeChosen.set(year,monthOfYear,dayOfMonth);

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
                        timeChosen.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeChosen.set(Calendar.MINUTE, minute);
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
        startActivityForResult(intent, Constants.REQUEST_CODE_PICK_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if( resultCode != RESULT_OK){

            Log.e(Constants.LOST_FOUND_TAG, "bas result for request code: " + requestCode);
            return;
        }

        switch(requestCode){

            case Constants.REQUEST_CODE_PICK_LOCATION:
                setLocationFromMap(data);
                break;

            case Constants.REQUEST_CODE_CAMERA:
                setImageFromCamera(data);
                break;
            case Constants.REQUEST_CODE_SELECT_FILE:
                setImageFromGallery(data);
                break;
            default:
                Log.e(Constants.LOST_FOUND_TAG, "request code for some weird activity. request code: " + requestCode);

        }
    }

    private void setLocationFromMap(Intent data) {
        location_chosen = new LatLng(data.getDoubleExtra(Constants.LATITUDE,0),data.getDoubleExtra(Constants.LONGITUDE,0));
        String location = getLocationFromCoordinates(location_chosen);
        tv_location_picker.setText(location);
    }

    private void setImageFromCamera(Intent data) {
        Bitmap imageFromCamera = (Bitmap) data.getExtras().get("data");
        ib_item_photo.setImageBitmap(imageFromCamera);
    }

    private void setImageFromGallery(Intent data) {
        try{
            Bitmap imageFromGallery = ImageUtils.decodeUri(getContentResolver(), data.getData());
            ib_item_photo.setImageBitmap(imageFromGallery);

        } catch (FileNotFoundException e) {
            Log.e(Constants.LOST_FOUND_TAG, "user image file from gallery not found. WTF???");
            e.printStackTrace();
        }
    }

    private String getLocationFromCoordinates(LatLng location_chosen) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        StringBuilder sb = new StringBuilder();

        try {
            List<Address> addresses = geocoder.getFromLocation(location_chosen.latitude,location_chosen.longitude, 1);

            if (addresses.isEmpty()) {
                Log.d(Constants.LOST_FOUND_TAG, "Got no address from map activity, using unknown");
                sb.append(Constants.Geocoder.DESCRIPTION_NOT_AVAILABLE);
            }
            else {
                Address address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i))
                      .append(", ");
                }

                sb.append(address.getAddressLine(address.getMaxAddressLineIndex()));
            }

        } catch (IOException e) {
            Log.d(Constants.LOST_FOUND_TAG, "Exception when trying to get address description");
            sb.append(Constants.Geocoder.DESCRIPTION_NOT_AVAILABLE);
        }

        return sb.toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // user chose other.
        if (spinner.getSelectedItemPosition() == spinner.getAdapter().getCount() - 1) {
            et_itemName.setVisibility(View.VISIBLE);
        } else {
            et_itemName.setVisibility(View.INVISIBLE);
            //reset item name.
            et_itemName.setText("");
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

        tv_location_picker.setText(Constants.Geocoder.NO_LOCATION_AVAILABLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            ib_pick_location.setEnabled(true);
            tv_location_picker.setEnabled(true);
            updateCurrentLocation();
        }
        else {
            ib_pick_location.setEnabled(false);
            tv_location_picker.setEnabled(false);
            tv_location_picker.setText(Constants.Geocoder.NO_LOCATION_AVAILABLE);
            location_chosen = null;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.action_send){
            submitReport();
        }
        return true;
    }

    private void submitReport() {
        ParseObject parseReport;
//        String parseClass = lostReport ? Constants.ParseObject.PARSE_LOST : Constants.ParseObject.PARSE_FOUND;
        String parseClass = Constants.ParseObject.PARSE_LOST;

        if (editReport) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            ParseQuery query = ParseQuery.getQuery(parseClass);
            query.whereEqualTo(Constants.ParseQuery.OBJECT_ID,itemEdited.getId());
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    progressDialog.dismiss();
                    fillReport(parseObject);
                }
            });

        }
        else {
            parseReport = new ParseObject(parseClass);
            fillReport(parseReport);
        }

    }

    private void fillReport(final ParseObject parseReport) {
        parseReport.put(Constants.ParseReport.IS_LOST,lostReport);
        String itemName = et_itemName.getText().toString();

        // item name was chosen from spinner.
        if(itemName.isEmpty()){
            itemName = spinner.getSelectedItem().toString();
        }

        parseReport.put(Constants.ParseReport.ITEM_NAME, itemName);
        parseReport.put(Constants.ParseReport.ITEM_DESCRIPTION, et_description.getText().toString());
        parseReport.put(Constants.ParseReport.TIME, timeChosen.getTimeInMillis());

        if (cb_with_location.isChecked() && location_chosen != null){
            ParseGeoPoint location = new ParseGeoPoint(location_chosen.latitude, location_chosen.longitude);
            parseReport.put(Constants.ParseReport.LOCATION, location);
            parseReport.put(Constants.ParseReport.LOCATION_STRING, tv_location_picker.getText().toString());
        }
        else {
            parseReport.put(Constants.ParseReport.LOCATION_STRING, Constants.Geocoder.NO_LOCATION_AVAILABLE);
        }

        parseReport.put(Constants.ParseReport.USER_ID, ParseUser.getCurrentUser().getObjectId());

        Bitmap itemImage = ((BitmapDrawable)ib_item_photo.getDrawable()).getBitmap();
        parseItemImage = ImageUtils.getImageAsParseFile(ITEM_IMAGE_NAME,itemImage);
        parseReport.put(Constants.ParseReport.ITEM_IMAGE, parseItemImage);

        String userLoginName = (String) ParseUser.getCurrentUser().get(Constants.ParseUser.USER_DISPLAY_NAME);
        if (null == userLoginName) {
            Log.d(Constants.LOST_FOUND_TAG, "got NULL login name, using unknown");
            userLoginName = "<unknown user>";
        }

        parseReport.put(Constants.ParseReport.USER_DISPLAY_NAME, userLoginName);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        if(editReport){
            progressDialog.setTitle("Updating Report");
        }else{
            progressDialog.setTitle("Creating Report");
        }
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        final ReportFormActivity activity = this;
        parseReport.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressDialog.dismiss();
                if(e!=null){
                    progressDialog.dismiss();
                    Toast.makeText(activity, "No Connection. Can't upload report", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        parseReport.pin();
                        progressDialog.dismiss();
                        setResult(RESULT_OK,null);
                        finish();
                        Toast.makeText(activity, "Report has been shipped", Toast.LENGTH_SHORT).show();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
