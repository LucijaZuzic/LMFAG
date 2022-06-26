package com.example.lmfag.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lmfag.R;
import com.example.lmfag.activities.FriendRequestsActivity;

public class FriendRequestAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        Intent getToRequestsIntent = new Intent(context, FriendRequestsActivity.class);
        getToRequestsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, getToRequestsIntent, PendingIntent.FLAG_IMMUTABLE);
        int icon = R.drawable.ic_baseline_mail_24;
        String title = context.getApplicationContext().getResources().getString(R.string.friend_request_received);
        String description = "";
        vibrator.vibrate(200);

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