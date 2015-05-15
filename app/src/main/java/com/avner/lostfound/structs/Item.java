package com.avner.lostfound.structs;

import android.location.Location;
import android.media.Image;

import com.avner.lostfound.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by avner on 24/04/2015.
 */
public class Item {

    private String userId;

    private String userDisplayName;

    private String name;

    private String description;

    private GregorianCalendar calender;

    private Location location;

    private int imageId;
    private String itemId;

    public Item(String itemId, String name, String description, GregorianCalendar calender, Location location, String userId, String userDisplayName) {

        this(itemId, name, description, calender, location, R.drawable.image_unavailable, userId, userDisplayName);
    }

    public Item(String itemId, String name, String description, GregorianCalendar calender, Location location, int imageId, String userId, String userDisplayName) {
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
//        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
        return "Rehovot";
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
}
