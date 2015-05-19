package com.avner.lostfound;

import android.os.Environment;

/**
 * Created by avner on 26/04/2015.
 */
public class Constants {

    public static final String signed_up = "signed_up";
    public static final int SIGN_UP_REQUEST_ID = 1;
    public static final int SIGN_UP_SUCCESSFUL = 2;
    public static final String USER_NAME= "user_name";
    public static final String PASSWORD = "password";
    public static final int FACEBOOK_LOGIN_REQUEST_ID = 3;
    public static final String USER_DISPLAY_NAME = "name";
    public static final java.lang.String LONGITUDE = "longitude";
    public static final java.lang.String LATITUDE = "latitude";
    public static final int PICK_LOCATION_REQUEST_CODE = 4;
    public static final int PICK_LOCATION_SUCCESSFUL = 2;
    public static final String RECIPIENT_ID = "RECIPIENT_ID";
    public static final int REQUEST_CODE_CAMERA = 10;
    public static final int REQUEST_CODE_SELECT_FILE = 11;
    public static final String APP_IMAGE_DIRECTORY = "/lostfound";
    public static final String USER_IMAGE_FILE_NAME = "/userImage.jpg";
    public static final String USER_IMAGE_FILE_PATH = Environment.getExternalStorageDirectory()
            + Constants.APP_IMAGE_DIRECTORY + Constants.USER_IMAGE_FILE_NAME;

}
