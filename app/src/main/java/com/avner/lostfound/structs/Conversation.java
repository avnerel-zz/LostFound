package com.avner.lostfound.structs;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.avner.lostfound.Constants;
import com.parse.ParseObject;

public class Conversation  implements Parcelable{

    public final String conversationId;
    public final String userName;
    public final String userId;
    public final Item item;
    public int unreadCount;

    public Conversation(ParseObject parseConversation){

        this.userName  = (String) parseConversation.get(Constants.ParseConversation.RECIPIENT_USER_NAME);
        this.userId    = (String) parseConversation.get(Constants.ParseConversation.RECIPIENT_USER_ID);

        ParseObject parseItem = (ParseObject) parseConversation.get(Constants.ParseConversation.ITEM);
        if(parseItem == null){
            Log.e(Constants.LOST_FOUND_TAG, "item is null, probably removed.");
        }
        this.item = new Item(parseItem);
        this.conversationId = parseConversation.getObjectId();
        this.unreadCount = (int) parseConversation.get(Constants.ParseConversation.UNREAD_COUNT);
    }

    public Item getItem() {
        return item;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeStringArray(new String[]{conversationId,userName, userId});
        item.writeToParcel(dest,flags);
        dest.writeInt(unreadCount);
    }

    public Conversation(Parcel source) {

        String[] stringFields = new String[3]; //new String[]{locationAsString, imageUrl, userId, userDisplayName, name, description, itemId};
        source.readStringArray(stringFields);
        conversationId = stringFields[0];
        userName = stringFields[1];
        userId = stringFields[2];
        item = (Item) Item.CREATOR.createFromParcel(source);
        unreadCount = source.readInt();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new Conversation(source);

        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    };

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Object getId() {
        return conversationId;
    }
}
