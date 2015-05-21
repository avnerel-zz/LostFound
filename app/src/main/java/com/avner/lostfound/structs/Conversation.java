package com.avner.lostfound.structs;

import com.parse.ParseObject;

/**
 * Created by avner on 21/05/2015.
 */
public class Conversation {

    public String userName;
    public String userId;
    public Item item;

    public Conversation(ParseObject parseConversation){

        this.userName  = (String) parseConversation.get("conversationUserName");
        this.userId = (String) parseConversation.get("conversationUserId");
        this.item = new Item((ParseObject) parseConversation.get("conversationItem"));
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
