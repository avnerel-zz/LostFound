package com.avner.lostfound;

import android.location.Location;
import android.media.Image;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by avner on 24/04/2015.
 */
public class Item {

    private String name;

    private String description;

    private GregorianCalendar calender;

    private Location location;

    private int imageId;

    public Item(String name, String description, GregorianCalendar calender, Location location) {

        this(name, description, calender, location, 0);
    }

    public Item(String name, String description, GregorianCalendar calender, Location location, int imageId) {
        this.name = name;
        this.description = description;
        this.calender = calender;
        this.location = location;
        this.imageId = imageId;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getDiff() {
        long diff = System.currentTimeMillis() - calender.getTimeInMillis();
        return (diff / (1000 * 60 * 60 * 24));
    }

    public Location getLocation() {
        return location;
    }

    public int getImage() {

        if(imageId == 0){

            return R.drawable.image_unavailable;
        }
        return imageId;
    }
}
