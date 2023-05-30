package com.jamburger.kitter.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jamburger.kitter.MainActivity;
import com.jamburger.kitter.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    final String channelID = "jamburger.kitter.channel";
    final String channelName = "kitter notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.w("TOKEN", "onNewToken: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() != null) {
            generateNotification(message.getNotification().getTitle(), message.getNotification().getBody());
        }
    }

    void generateNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.mipmap.ic_kitter_foreground)
                .setColor(getResources().getColor(R.color.ic_kitter_background))
                .setVibrate(new long[]{1000, 1000, 1000, 1000})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setContentTitle(title)
                .setCategory("General");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }
}
