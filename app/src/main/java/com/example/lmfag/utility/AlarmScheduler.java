package com.example.lmfag.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.example.lmfag.receivers.EventAlarmReceiver;
import com.example.lmfag.receivers.FriendRequestAlarmReceiver;
import com.example.lmfag.receivers.RateAlarmReceiver;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlarmScheduler {

    public static void scheduleAlarmStart(Context applicationContext, long timeInMillis, String icon, String name, String description, String eventID) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, EventAlarmReceiver.class);
        alarmReceiverIntent.putExtra("icon", icon);
        alarmReceiverIntent.putExtra("name", name);
        alarmReceiverIntent.putExtra("description", description);
        alarmReceiverIntent.putExtra("eventID", eventID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        }
    }

    public static void scheduleAlarmEnd(Context applicationContext) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, RateAlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }

    public static void scheduleAlarmFriendRequest(Context applicationContext,  String name, String friendID ) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, FriendRequestAlarmReceiver.class);
        alarmReceiverIntent.putExtra("name", name);
        alarmReceiverIntent.putExtra("friendID", friendID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }

    public static void cancelAllAlarms(Context applicationContext) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, EventAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static void getAllReceivedFriendRequests(Context applicationContext) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String me = preferences.getString("userID", "");
        db.collection("friend_requests")
                .whereEqualTo("receiver", me)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("users")
                                    .document(document.getData().get("sender").toString())
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            scheduleAlarmFriendRequest(applicationContext,  task2.getResult().getData().get("username").toString(), document.getData().get("sender").toString());
                                        }
                                    });
                            }
                        }
                    }
                });

    }
    public static void getAllSubscriberEvents(Context applicationContext) {
        cancelAllAlarms(applicationContext);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String userID = preferences.getString("userID", "");
        AtomicBoolean sentWarning = new AtomicBoolean(false);
        if (!userID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String isSubscribed = Objects.requireNonNull(document.getData().get("notifications")).toString();
                            if (isSubscribed.equals("true")) {
                                DocumentReference docRef = db.collection("events").document(Objects.requireNonNull(document.getData().get("event")).toString());
                                docRef.get().addOnCompleteListener(taskTime -> {
                                    if (taskTime.isSuccessful()) {
                                        DocumentSnapshot documentTime = taskTime.getResult();
                                        if (documentTime.exists()) {
                                            Map<String, Object> docData = documentTime.getData();
                                            Timestamp start_timestamp = (Timestamp) (Objects.requireNonNull(docData).get("datetime"));
                                            Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                                            Calendar cldr_start = Calendar.getInstance();
                                            cldr_start.setTime(start_date);
                                            Timestamp end_timestamp = (Timestamp) (docData.get("ending"));
                                            Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                                            Calendar cldr_end = Calendar.getInstance();
                                            cldr_end.setTime(end_date);
                                            Calendar current = Calendar.getInstance();
                                            if (current.getTimeInMillis() < cldr_start.getTimeInMillis()) {
                                                scheduleAlarmStart(applicationContext, cldr_start.getTimeInMillis() - current.getTimeInMillis(), Objects.requireNonNull(docData.get("event_type")).toString(), Objects.requireNonNull(docData.get("event_name")).toString(), Objects.requireNonNull(docData.get("event_description")).toString(), documentTime.getId());
                                            }
                                            if (current.getTimeInMillis() > cldr_end.getTimeInMillis() && !Objects.requireNonNull(document.getData().get("rated")).toString().equals("true")) {
                                                sentWarning.set(true);
                                                scheduleAlarmEnd(applicationContext);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }
}
