package com.avner.lostfound.structs;

import android.graphics.Bitmap;
import android.location.Location;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by avner on 24/04/2015.
 */
public class Item {

    private String locationAsString;

    private String imageUrl;

    private String userId;

    private String userDisplayName;

    private String name;

    private String description;

    private Calendar calender;

    private Location location;

    private int imageId;
    private String itemId;

    private ParseObject parseItem;

    public Item(String itemId, String name, String description, Calendar calender, Location location, String userId, String userDisplayName) {

        this(itemId, name, description, calender, location, R.drawable.image_unavailable, userId, userDisplayName);
    }

    public Item(String itemId, String name, String description, Calendar calender, Location location, int imageId, String userId, String userDisplayName) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.calender = calender;
        this.location = location;
        this.imageId = imageId;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
    }

    public String getName() {
        return name;
    }

    public Item(ParseObject parseItem){

        this.parseItem = parseItem;

        name = (String) parseItem.get(Constants.ParseReport.ITEM_NAME);
        description = (String) parseItem.get(Constants.ParseReport.ITEM_DESCRIPTION);

        ParseGeoPoint parseLocation = (ParseGeoPoint) parseItem.get(Constants.ParseReport.LOCATION);
        location = new Location("location");
        location.setLatitude(parseLocation.getLatitude());
        location.setLongitude(parseLocation.getLongitude());

        long timeLost = (long) parseItem.get(Constants.ParseReport.TIME);
        calender = Calendar.getInstance();
        calender.setTimeInMillis(timeLost);

        userId = (String) parseItem.get(Constants.ParseReport.USER_ID);
        itemId = (String) parseItem.getObjectId();
        userDisplayName = (String) parseItem.get(Constants.ParseReport.USER_DISPLAY_NAME);
        locationAsString = (String) parseItem.get(Constants.ParseReport.LOCATION_STRING);
        imageUrl = ((ParseFile) parseItem.get(Constants.ParseReport.ITEM_IMAGE)).getUrl();

    }

    public Item(String itemId, String name, String description, Calendar calender, Location location, String imageUrl, String userId, String userDisplayName, String locationAsString) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.calender = calender;
        this.location = location;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.locationAsString = locationAsString;
    }

    public String getDescription() {
        return description;
    }

    public int getDiff() {
        long diff = System.currentTimeMillis() - calender.getTimeInMillis();
        return (int)(diff / (1000 * 60 * 60 * 24));
    }

    public Location getLocation() {
        return location;
    }

    public int getImage() {

        return imageId;
    }

    public String getLocationString() {
        return locationAsString;
    }

    public String timeAgo() {
        String returnValue = "";
        long diff = System.currentTimeMillis() - calender.getTimeInMillis();
        diff /= 1000; // seconds ago

        int days = (int)(diff / (60 * 60 * 24));
        returnValue += days + " days ago";

        return returnValue;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getId() {
        return itemId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ParseObject getParseItem() {
        return parseItem;
    }
}
