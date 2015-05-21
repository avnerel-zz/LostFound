package com.avner.lostfound;

import android.os.Environment;

/**
 * Created by avner on 26/04/2015.
 * This class holds all common constants used by different components.
 */
public class Constants {

    public static final int REQUEST_CODE_SIGN_UP = 1;
    public static final int REQUEST_CODE_FACEBOOK_LOGIN = 3;
    public static final int REQUEST_CODE_PICK_LOCATION = 4;
    public static final int REQUEST_CODE_CAMERA = 10;
    public static final int REQUEST_CODE_SELECT_FILE = 11;

    public static final String signed_up = "signed_up";
    public static final String USER_NAME= "user_name";
    public static final String PASSWORD = "password";
    public static final String USER_DISPLAY_NAME = "name";
    public static final java.lang.String LONGITUDE = "longitude";
    public static final java.lang.String LATITUDE = "latitude";

    public static final String APP_IMAGE_DIRECTORY_NAME = "/lostfound";
    public static final String USER_IMAGE_FILE_NAME = "userImage.png";
    public static final String USER_IMAGE_FILE_PATH = Environment.getExternalStorageDirectory()
            + Constants.APP_IMAGE_DIRECTORY_NAME + "/" + Constants.USER_IMAGE_FILE_NAME;

    public static final String IS_LOST_FORM = "lost";

    /**
     * tag for logger.
     */
    public static final String LOST_FOUND_TAG = "lostfound";

    public class ParseReport{

        public static final String ITEM_NAME = "itemName";
        public static final String ITEM_DESCRIPTION = "itemDescription";
        public static final String TIME = "time";
        public static final String LOCATION = "location";
        public static final String LOCATION_STRING = "locationString";
        public static final String USER_ID = "userId";
        public static final String ITEM_IMAGE = "itemImage";
        public static final String USER_DISPLAY_NAME = "name";
    }

    public class ParseObject{

        public static final String PARSE_LOST = "ParseLost";
        public static final String PARSE_FOUND = "ParseFound";
    }

    public class ParseUser{

        public static final String USER_DISPLAY_NAME = "name";
    }

    public class Conversation{

        public static final String ITEM_ID = "itemId";
        public static final String RECIPIENT_ID = "RECIPIENT_ID";
    }
}
