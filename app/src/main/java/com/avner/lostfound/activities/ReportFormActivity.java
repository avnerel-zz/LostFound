package com.avner.lostfound.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.avner.lostfound.ImageUtils;
import com.avner.lostfound.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
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

        lostReport = getIntent().getExtras().getBoolean(Constants.IS_LOST_FORM);
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

        Bitmap defaultImage = ((BitmapDrawable)ib_item_photo.getDrawable()).getBitmap();
        parseItemImage = ImageUtils.getImageAsParseFile(ITEM_IMAGE_NAME,defaultImage);
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
        Calendar currentDate = new GregorianCalendar();

        ib_pick_date = (ImageButton) findViewById(R.id.b_pick_date);
        ib_pick_date.setOnClickListener(this);
        tv_date_picker = (TextView) findViewById(R.id.tv_date_picker);
        tv_date_picker.setText((currentDate.get(Calendar.DAY_OF_MONTH) + "-" + (currentDate.get(Calendar.MONTH) + 1) + "-" + currentDate.get(Calendar.YEAR)));

        ib_pick_time = (ImageButton) findViewById(R.id.b_pick_time);
        ib_pick_time.setOnClickListener(this);
        tv_time_picker = (TextView) findViewById(R.id.tv_time_picker);
        tv_time_picker.setText(String.format("%02d", currentDate.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", currentDate.get(Calendar.MINUTE)));

        timeChosen = Calendar.getInstance();
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
                selectItemImage();
                break;
            default:
                Log.e("my_tag", "clicked on something weird!!");
        }
    }

    private void selectItemImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.REQUEST_CODE_SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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
        parseItemImage = ImageUtils.getImageAsParseFile(ITEM_IMAGE_NAME,imageFromCamera);
        ib_item_photo.setImageBitmap(imageFromCamera);
    }

    private void setImageFromGallery(Intent data) {
        try{
            Bitmap imageFromGallery = ImageUtils.decodeUri(getContentResolver(), data.getData());
            parseItemImage = ImageUtils.getImageAsParseFile(ITEM_IMAGE_NAME,imageFromGallery);
            ib_item_photo.setImageBitmap(imageFromGallery);

        } catch (FileNotFoundException e) {
            Log.e(Constants.LOST_FOUND_TAG, "user image file from gallery not found. WTF???");
            e.printStackTrace();
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

        // chose other.
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

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        if(isChecked){

            ib_pick_location.setEnabled(true);
            tv_location_picker.setEnabled(true);
            updateCurrentLocation();

        }else{

            ib_pick_location.setEnabled(false);
            tv_location_picker.setEnabled(false);
            tv_location_picker.setText("No location specified");
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
        if(lostReport){

            parseReport = new ParseObject("ParseLost");
        }else{

            parseReport = new ParseObject("ParseFound");
        }

        String itemName = et_itemName.getText().toString();

        // item name was chosen from spinner.
        if(itemName.isEmpty()){
            itemName = spinner.getSelectedItem().toString();
        }

        parseReport.put("itemName", itemName);
        parseReport.put("itemDescription", et_description.getText().toString());
        parseReport.put("time", timeChosen.getTimeInMillis());

        if(cb_with_location.isChecked()){

            ParseGeoPoint location = new ParseGeoPoint(location_chosen.latitude, location_chosen.longitude);
            parseReport.put("location", location);
        }
        parseReport.put("userId", ParseUser.getCurrentUser().getObjectId());
        parseReport.put("itemImage", parseItemImage);
        parseReport.saveInBackground();

        Toast.makeText(this,"report has been shipped" ,Toast.LENGTH_SHORT).show();

        finish();
    }
}
