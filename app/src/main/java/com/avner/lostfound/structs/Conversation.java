package com.avner.lostfound.structs;

import com.avner.lostfound.Constants;
import com.parse.ParseObject;

/**
 * Created by avner on 21/05/2015.
 */
public class Conversation {

    public final String userName;
    public final String userId;
    public final Item item;

    public Conversation(ParseObject parseConversation){

        this.userName  = (String) parseConversation.get(Constants.Conversation.CONVERSATION_FIELD_USER_NAME);
        this.userId    = (String) parseConversation.get(Constants.Conversation.CONVERSATION_FIELD_USER_ID);
        this.item      = new Item((ParseObject) parseConversation.get(Constants.Conversation.CONVERSATION_FIELD_ITEM));
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

}
