package com.avner.lostfound;

import android.os.Environment;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * tag for logger.
     */
    public static final String LOST_FOUND_TAG = "LOST_FOUND_TAG";

    /**
     * for lists filtering
     */
    public static final long NO_DISTANCE_FILTER = -1;
    public static final long NO_TIME_FILTER = -1;
    public static final String NO_CONTENT_FILTER = "";
    public static final long MILLI_SECONDS_PER_DAY = 1000* 60 * 60 * 24;
    public static final Map<String, Long> daysFactor = initDaysFactorMap();
    public static final int MIN_CONTENT_FILTER_SIZE = 2;


    public class TabTexts {
        public static final String FOUND = "Found";
        public static final String LOST = "Lost";
        public static final String MY_WORLD = "My World";
        public static final String STATS = "Stats";
    }




    private static Map<String, Long> initDaysFactorMap() {
        Map<String, Long> map = new HashMap<>();

        map.put("today", 1L);
        map.put("week", 7L);
        map.put("month", 30L);
        map.put("year", 365L);

        return map;
    }

    public class ParseReport{

        public static final String ITEM_NAME = "itemName";
        public static final String ITEM_DESCRIPTION = "itemDescription";
        public static final String TIME = "time";
        public static final String LOCATION = "location";
        public static final String LOCATION_STRING = "locationString";
        public static final String USER_ID = "userId";
        public static final String ITEM_IMAGE = "itemImage";
        public static final String USER_DISPLAY_NAME = "name";
        public static final String IS_LOST = "isLost";
    }

    public class ParseQuery{

        public static final String CREATED_AT = "createdAt";
        public static final String OBJECT_ID = "objectId";
    }

    public class ParseObject{

        public static final String PARSE_LOST = "ParseLost";
        public static final String PARSE_FOUND = "ParseFound";
        public static final String PARSE_CONVERSATION= "ParseConversation";
        public static final String PARSE_MESSAGE= "ParseMessage";
    }

    public class ParseUser{

        public static final String USER_DISPLAY_NAME = "name";
    }

    public class Conversation{

        public static final String ITEM_ID = "itemId";
        public static final String RECIPIENT_ID = "recipientId";
        public static final String RECIPIENT_NAME = "recipientName";
    }

    public class ParseConversation{

        public static final String MY_USER_ID = "myUserId";
        public static final String RECIPIENT_USER_ID = "recipientUserId";
        public static final String RECIPIENT_USER_NAME = "recipientUserName";
        public static final String ITEM = "item";
        public static final String UNREAD_COUNT = "unreadCount";

    }

    public class ParseMessage{
        public static final String SENDER_ID = "senderId";
        public static final String RECIPIENT_ID = "recipientId";
        public static final String MESSAGE_TEXT = "messageText";
        public static final String SINCH_ID = "sinchId";
        public static final String ITEM_ID = "itemId";
        public static final String CREATED_AT = "createdAt";

    }

    public class ReportForm{

        public static final String IS_LOST_FORM = "lost";
        public static final String IS_EDIT_FORM = "edit";
        public static final String ITEM = "item";
    }

    public class SinchMessage {
        public static final String ITEM_ID = "itemId";
    }

    public class Geocoder {
        public static final String DESCRIPTION_NOT_AVAILABLE = "Description not Available";
        public static final String NO_LOCATION_AVAILABLE = "No location available";
    }

    public class ParsePush{
        public static final String EXTRA_NAME = "com.parse.Data";
        public static final String PUSH_TYPE = "pushType";
        public static final String SENDER_ID = "senderId";
        public static final String SENDER_NAME = "senderName";
        public static final String ITEM_ID = "itemId";
        public static final String REPORTED_ITEM = "reportedItem";

    }
}
