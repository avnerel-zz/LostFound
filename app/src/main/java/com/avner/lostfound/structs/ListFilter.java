package com.avner.lostfound.structs;

import com.avner.lostfound.Constants;

import java.util.Arrays;

/**
 * Created by Oded on 24/05/2015.
 */
public class ListFilter {
    private long filter_dist = Constants.NO_DISTANCE_FILTER;
    private long filter_time = Constants.NO_TIME_FILTER;
    private String filter_content = Constants.NO_CONTENT_FILTER;

    public long getDistFilter() {
        return filter_dist;
    }

    public long getTimeFilter() {
        return filter_time;
    }

    public String getContentFilter() {
        return new String(this.filter_content); // defensive copy
    }

    public boolean updateTimeFilter(String strTimeFilter) {
        long prevFilter = this.filter_time;

        if (strTimeFilter.startsWith("All")) {
            this.filter_time = Constants.NO_TIME_FILTER;
            return (this.filter_time != prevFilter);
        }

        for (String time : Arrays.asList("today", "week", "month", "year")) {
            if (strTimeFilter.toLowerCase().contains(time)) {
                long factor = Constants.daysFactor.get(time);
                this.filter_time = Constants.MILLI_SECONDS_PER_DAY * factor;
                break;
            }
        }

        return (this.filter_time != prevFilter);
    }

    public boolean updateContentFilter(String phrase) {
        if (null == phrase || "".equals(phrase) || phrase.length() < Constants.MIN_CONTENT_FILTER_SIZE) { // invalid filter
            return false;
        }

        if (this.filter_content.equals(phrase)) { // same as previous filter
            return false;
        }

        this.filter_content = new String(phrase);
        return true;
    }


    public boolean updateDistanceFilter(String strDistFilter) {
        long prevFilter = this.filter_dist;

        if (strDistFilter.startsWith("All")) {
            this.filter_dist = Constants.NO_DISTANCE_FILTER;
            return (this.filter_dist != prevFilter);
        }

        String pattern = "\\D";
        this.filter_dist = Long.parseLong(strDistFilter.replaceAll(pattern, ""));
        if (strDistFilter.contains("km")) {
            this.filter_dist *= 1000;
        }

        return (this.filter_dist != prevFilter);
    }




}
