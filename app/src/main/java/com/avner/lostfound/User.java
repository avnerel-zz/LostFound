package com.avner.lostfound;

/**
 * Created by avner on 28/04/2015.
 */
public class User {

    private String name;

    private int score;

    private int imageId;


    public User(String name, int score, int imageId) {
        this.name = name;
        this.score = score;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getImageId() {
        return imageId;
    }
}
