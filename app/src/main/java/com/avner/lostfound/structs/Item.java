package com.avner.lostfound.structs;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.avner.lostfound.Constants;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Item implements Parcelable {

    private String locationAsString;
    private String imageUrl;
    private String userId;
    private String userDisplayName;
    private String name;
    private String description;
    private Calendar calender;
    private Location location;
    private String itemId;
    private boolean isLost;
    private boolean isAlive;
    private ArrayList<String> possibleMatches;

    public Item(ParseObject parseItem) {

        name = (String) parseItem.get(Constants.ParseReport.ITEM_NAME);
        description = (String) parseItem.get(Constants.ParseReport.ITEM_DESCRIPTION);

        ParseGeoPoint parseLocation = (ParseGeoPoint) parseItem.get(Constants.ParseReport.LOCATION);
        if(parseLocation!= null){

            location = new Location("");
            location.setLatitude(parseLocation.getLatitude());
            location.setLongitude(parseLocation.getLongitude());
        }

        long timeLost = (long) parseItem.get(Constants.ParseReport.TIME);
        calender = Calendar.getInstance();
        calender.setTimeInMillis(timeLost);

        userId = (String) parseItem.get(Constants.ParseReport.USER_ID);
        itemId = parseItem.getObjectId();
        userDisplayName = (String) parseItem.get(Constants.ParseReport.USER_DISPLAY_NAME);
        locationAsString = (String) parseItem.get(Constants.ParseReport.LOCATION_STRING);
        imageUrl = ((ParseFile) parseItem.get(Constants.ParseReport.ITEM_IMAGE)).getUrl();
        isLost = (boolean) parseItem.get(Constants.ParseReport.IS_LOST);
        isAlive = (boolean) parseItem.get(Constants.ParseReport.IS_ALIVE);
        possibleMatches = (ArrayList<String>) parseItem.get(Constants.ParseReport.POSSIBLE_MATCHES);
    }


    public Calendar getTime() {
        return calender;
    }

    public String getName() {
        return name;
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
        diff /= 1000;
        return (int) (diff / (60 * 60 * 24));
    }

    public Location getLocation() {
        return location;
    }

    public String getLocationString() {
        return locationAsString;
    }

    public String timeAgo() {
        long diff = System.currentTimeMillis() - calender.getTimeInMillis();
        diff /= 1000; // seconds ago

        int days = (int) (diff / (60 * 60 * 24));
        if (days > 0) {
            return days + " days ago";
        }

        int hours = (int) (diff / (60 * 60));
        if (hours > 0) {
            return hours + " hours ago";
        }

        int minutes = (int) (diff / (60));
        return minutes + " minutes ago";


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

    public boolean isLost() {
        return isLost;
    }

    public String getTimeAsString(){

        SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return formatter.format(calender.getTime());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeStringArray(new String[]{locationAsString, imageUrl, userId, userDisplayName, name, description, itemId});
        dest.writeLong(calender.getTimeInMillis());
        dest.writeBooleanArray(new boolean[]{isLost});
        if(location!= null){

            location.writeToParcel(dest, flags);
        }
    }

    public Item(Parcel source) {

        String[] stringFields = new String[7]; // new String[]{locationAsString, imageUrl, userId, userDisplayName, name, description, itemId};
        source.readStringArray(stringFields);
        locationAsString = stringFields[0];
        imageUrl = stringFields[1];
        userId = stringFields[2];
        userDisplayName = stringFields[3];
        name = stringFields[4];
        description = stringFields[5];
        itemId = stringFields[6];

        calender = Calendar.getInstance();
        calender.setTimeInMillis(source.readLong());

        boolean[] booleanField = new boolean[1];
        source.readBooleanArray(booleanField);
        isLost = booleanField[0];

        // location is valid.
        if(source.dataAvail()!=0){

            location = Location.CREATOR.createFromParcel(source);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new Item(source);

        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    };

    public String getShareDescription() {
        StringBuilder sb = new StringBuilder();
        if(isLost()){
            sb.append("Lost ");
        }else{
            sb.append("Found ");
        }
        sb.append(name).append("\n");
        sb.append("Description: ").append(description).append("\n");
        if(location!= null){
            sb.append("Location: ").append(locationAsString).append("\n");
        }
        sb.append("Time: ").append(getTimeAsString()).append("\n");
        sb.append("Shared by Lost & Found app. Soon in google play");

        return sb.toString();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean hasPossibleMatches() {
        return possibleMatches != null && !(possibleMatches.isEmpty());
    }
    public ArrayList<String> getPossibleMatches() {
        return possibleMatches;
    }
}