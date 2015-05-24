package com.avner.lostfound.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.avner.lostfound.Constants;
import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {

    private static final String APP_KEY = "e65991aa-36e3-4f14-b26d-8bcdceefebca";
    private static final String APP_SECRET = "zbRQl1p4306UTFbNpW4CKQ==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;

//    private Intent broadcastIntent = new Intent("com.avner.lostfound.messaging.ConversationActivity");
//    private LocalBroadcastManager broadcaster;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get the current user id from Parse
        if (null != ParseUser.getCurrentUser()) {

            currentUserId = ParseUser.getCurrentUser().getObjectId();
        }
        if (currentUserId != null && !isSinchClientStarted()) {
            startSinchClient(currentUserId);
        }
//        broadcaster = LocalBroadcastManager.getInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String username) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();
        //this client listener requires that you define
        //a few methods below
        sinchClient.addSinchClientListener(this);
        //messaging is "turned-on", but calling is not
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.setSupportPushNotifications(true);
        sinchClient.checkManifest();
        sinchClient.start();
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    //The next 5 methods are for the sinch client listener
    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        sinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client) {
        client.startListeningOnActiveConnection();

        messageClient = client.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient client) {
        sinchClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration) {
    }

    @Override
    public void onLogMessage(int level, String area, String message) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    public void sendMessage(String recipientUserId, String textBody, String itemId) {
        if (messageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            message.addHeader(Constants.SinchMessage.ITEM_ID, itemId);
            messageClient.send(message);
//            Map<String, Object> params = new HashMap<String, Object>();
//            params.put("recipientId", recipientUserId);
//            params.put("senderName", ((LostFoundApplication) getApplication()).getUserDisplayName());
//            ParseCloud.callFunctionInBackground("hello", params, new FunctionCallback<String>() {
//                public void done(String result, ParseException e) {
//                    if (e == null) {
//                        Log.d("SERVER_CODE", result);
//                    } else {
//                        Log.e("SERVER_CODE", e.getMessage(), e);
//                    }
//                }
//            });

        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }

    //public interface for ListUsersActivity & MessagingActivity
    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody, String itemId) {
            MessageService.this.sendMessage(recipientUserId, textBody, itemId);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }
}
