package com.example.sapiotapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.provider.CalendarContract.EXTRA_EVENT_ID;

public class FCM extends FirebaseMessagingService {

    String TAG = "ROY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //sendNotification(remoteMessage.getNotification().getBody());
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //sendNotification(remoteMessage.getNotification().getBody());
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            sendNotification(remoteMessage.getNotification().getBody());
            // sending notification to own application (TESTING ONLY)
            // watch emulator token: fEKgPGlm-4E:APA91bHqcV47rB6k5J2uZ0QJObAIyXDNUS9ISapnfa28oITipwkWaGcjPaP5dXGM1m17afABrTGeFgLQgOrhDxmaQTr6UX6QljbjtRNUteyUhnLvdAmkHSDFsa6-v2ss6xIn3PbwuXBl
            try {
                JSONObject subJSON = new JSONObject();
                subJSON.put("title", "Firebase");
                subJSON.put("body", "Watch responded");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("notification",subJSON);
                jsonObject.put("to","dED-g7Njjxs:APA91bEIXrI_ycac4aBB4WJUE2dLFBeFp6kazSHnlcY1PpfRgY8N-MZ7kfbWIYFfHv-KtAQwtNSvUAzqCP2n95rK-lzkj3ZLX02x3MJpVnPabGbWLd02aFj6DZqQ8uz4MQl5lAxrqdPE");
                String msg = jsonObject.toString();
                String url = "https://fcm.googleapis.com/fcm/send";
                URL urlAddress = new URL(url);
                HttpsURLConnection connection  = (HttpsURLConnection) urlAddress.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", "key=AAAA3WHDiW4:APA91bHsHR2bnPzh-1MvR8NRKH35T3-Pm7QpIws8fh72J0Rdh5SPTz-cE5V8eAx2IUwtYHdLc-1mtO4l0yGfCuJSDPg5nb0h9YoTQl-vtTUb9w2-g48gkJo1RL9ak_HtBXD6sCOL5Dxr");
                //Sending the message
                byte[] bytes = msg.getBytes("UTF_8");
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(bytes);
                int status = connection.getResponseCode();
                System.out.println("Response status: " + String.valueOf(status));
            } catch (Exception ex) {
                System.out.println("Error occured while sending notification: "+ ex);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotification(String messageBody){
        int notificationId = 001;
        // The channel ID of the notification.
        String id = "my_channel_01";
        // Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        int eventId= 100 ;

        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle("IoT Alert")
                        .setContentText(messageBody)
                        .setContentIntent(viewPendingIntent);
        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        // Issue the notification with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
        System.out.println("Notification sent");
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
       // sendRegistrationToServer(token);
    }

}
