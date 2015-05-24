package com.avner.lostfound.messaging;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;


public class MessagingActivity extends Activity implements TextWatcher {
    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener;
    private ListView messagesList;
    private MessageAdapter messageAdapter;
    private ImageButton sendButton;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra(Constants.Conversation.RECIPIENT_ID);
        itemId = intent.getStringExtra(Constants.Conversation.ITEM_ID);

        currentUserId = ParseUser.getCurrentUser().getObjectId();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        messageBodyField.addTextChangedListener(this);

        initSendButton();

        initMessageList();
    }

    private void initMessageList() {
        messagesList = (ListView) findViewById(R.id.lv_messages_list);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        String[] userIds = {currentUserId, recipientId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_MESSAGE);
        query.whereContainedIn(Constants.ParseMessage.SENDER_ID, Arrays.asList(userIds));
        query.whereContainedIn(Constants.ParseMessage.RECIPIENT_ID, Arrays.asList(userIds));
        query.whereEqualTo(Constants.ParseMessage.ITEM_ID, itemId);
        query.orderByAscending(Constants.ParseMessage.CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message = new WritableMessage(messageList.get(i).get(Constants.ParseMessage.RECIPIENT_ID).toString(),
                                                                    messageList.get(i).get(Constants.ParseMessage.MESSAGE_TEXT).toString());
                        if (messageList.get(i).get(Constants.ParseMessage.SENDER_ID).toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                        }
                    }
                }
            }
        });
    }

    private void initSendButton() {

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        //listen for a click on the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                messageBody = messageBodyField.getText().toString();
                messageService.sendMessage(recipientId, messageBody,itemId);

                //reset message
                messageBodyField.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        ((LostFoundApplication)getApplication()).updateMessagingStatus(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        ((LostFoundApplication)getApplication()).updateMessagingStatus(false);
        super.onPause();
    }

    //unbind the service when the activity is destroyed
    @Override
    public void onDestroy() {
        serviceConnection.onServiceDisconnected(null);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(s.toString().isEmpty()){

            sendButton.setVisibility(ImageButton.INVISIBLE);
        }else{
            sendButton.setVisibility(ImageButton.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}


    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageClientListener = new MyMessageClientListener();
            messageService.addMessageClientListener(messageClientListener);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService.removeMessageClientListener(messageClientListener);
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {

        //Notify the user if their message failed to send
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            //TODO check this failure message is o.k.
            Toast.makeText(MessagingActivity.this, failureInfo.getSinchError().getMessage(), Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            if (message.getSenderId().equals(recipientId) && message.getHeaders().get(Constants.SinchMessage.ITEM_ID).equals(itemId)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            }
        }
        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {

            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //only add message to parse database if it doesn't already exist there
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_MESSAGE);

            query.whereEqualTo(Constants.ParseMessage.SINCH_ID, message.getMessageId());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject(Constants.ParseObject.PARSE_MESSAGE);
                            parseMessage.put(Constants.ParseMessage.SENDER_ID, currentUserId);
                            parseMessage.put(Constants.ParseMessage.RECIPIENT_ID, writableMessage.getRecipientIds().get(0));
                            parseMessage.put(Constants.ParseMessage.MESSAGE_TEXT, writableMessage.getTextBody());
                            parseMessage.put(Constants.ParseMessage.SINCH_ID, writableMessage.getMessageId());
                            parseMessage.put(Constants.ParseMessage.ITEM_ID, itemId);
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                        }
                    }
                }
            });

            sendPushNotification(recipientId, message);
        }

        private void sendPushNotification(String recipientId,Message message) {

//            ParseQuery pushQuery = ParseInstallation.getQuery();
//            pushQuery.whereEqualTo("user", recipientId);
//            ParsePush push = new ParsePush();
//            push.setQuery(pushQuery); // Set our Installation query
//            push.setMessage("user: " + ((LostFoundApplication) getApplication()).getUserDisplayName() + "sent: " + message.getTextBody());
//            push.sendInBackground();
//
//            Log.d("messaging", "sent " + message.getTextBody() + " to user id: " + recipientId);
        }

        //Do you want to notify your user when the message is delivered?
        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}
        //Don't worry about this right now
        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}