package com.example.lmfag.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lmfag.R;
import com.example.lmfag.activities.ViewProfileActivity;

public class FriendRequestAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        int icon = R.drawable.ic_baseline_person_24;
        String title = context.getApplicationContext().getResources().getString(R.string.friend_request_received) + " ";
        String description = "";
        vibrator.vibrate(200);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.getString("name") != null) {
                title += extras.getString("name");
            }
            if (extras.getString("friendID") != null) {
                editor.putString("friendID", extras.getString("friendID"));
                editor.apply();
            }
        }
        Intent getToEventIntent = new Intent(context, ViewProfileActivity.class);
        getToEventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, getToEventIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), context.getApplicationContext().getResources().getString(R.string.channel_id))
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);
    }
}