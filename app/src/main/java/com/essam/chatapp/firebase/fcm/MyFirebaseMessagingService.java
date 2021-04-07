package com.essam.chatapp.firebase.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.essam.chatapp.R;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Gson gson = new GsonBuilder().create();
        Message message = gson.fromJson(data.get(Consts.MESSAGE), Message.class);
        Profile profile = gson.fromJson(data.get(Consts.PROFILE), Profile.class);
        String chatId = gson.fromJson(data.get(Consts.CHAT_ID), String.class);

        if (message != null && profile != null && chatId != null) {
            // User currently opening the same chat room no need to push notification!
            String activeChatId = FcmUtils.getActiveChatId();
            if (activeChatId != null && activeChatId.equals(chatId)){
                return;
            }

            createNewMessageNotification(message, profile);
        }
    }

    private void createNewMessageNotification(Message message, Profile profile) {
        // Instantiate builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Consts.DEFAULT_NOTIFICATION_CHANNEL_ID);

        // Create intent for the activity
        Intent notifyIntent = new Intent(this, ChatActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyIntent.putExtra(Consts.PROFILE, profile);

        // Create the pending intent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.ic_logo)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.user_placeholder))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(profile.getUserName())
                .setContentText(message.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getMessage()))
                .setColor(getResources().getColor(R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(notifyPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel if version is android 8 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(Consts.DEFAULT_NOTIFICATION_CHANNEL_ID, "New message", NotificationManager.IMPORTANCE_HIGH));
        }
        notificationManager.notify(Consts.NEW_MESSAGE_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
