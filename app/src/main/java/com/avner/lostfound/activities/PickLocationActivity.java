package com.avner.lostfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PickLocationActivity extends FragmentActivity implements GoogleMap.OnMapClickListener{

    private GoogleMap map; // Might be null if Google Play services APK is not available.

    private MarkerOptions location_chosen_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_location, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId()){
            case R.id.mi_send:
                if (null == location_chosen_marker) { // no marker selected - create a dummy one
                    Log.d(Constants.LOST_FOUND_TAG, "location_chosen_marker is NULL!");
                    location_chosen_marker = getMarker(0.0, 0.0);
                }

                Intent intent = new Intent();
                intent.putExtra(Constants.LONGITUDE, location_chosen_marker.getPosition().longitude);
                intent.putExtra(Constants.LATITUDE, location_chosen_marker.getPosition().latitude);
                setResult(RESULT_OK, intent);
                finish();
        }
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                map.setOnMapClickListener(this);
                map.setMyLocationEnabled(true);
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        Intent intent = getIntent();

        double latitude = intent.getExtras().getDouble(Constants.LATITUDE);
        double longitude = intent.getExtras().getDouble(Constants.LONGITUDE);

        location_chosen_marker = getMarker(latitude, longitude);
    }

    private MarkerOptions getMarker(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);

        MarkerOptions marker = new MarkerOptions().position(position).title("My location");

        if (null != map) {
            Log.d(Constants.LOST_FOUND_TAG, "map is NULL! skipping marker update on map");
            map.addMarker(marker);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        }

        // set result for if the user doesn't chose a location.
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.LONGITUDE, longitude);
        resultIntent.putExtra(Constants.LATITUDE, latitude);
        setResult(RESULT_OK, resultIntent);
        return marker;
    }

    @Override
    public void onMapClick(LatLng latLng) {

        map.clear();
        MarkerOptions chosenMarker = new MarkerOptions().position(latLng).title("Chosen");
        map.addMarker(chosenMarker);

        location_chosen_marker = chosenMarker;
    }

}
