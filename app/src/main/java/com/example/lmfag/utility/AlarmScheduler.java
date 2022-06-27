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

    public static void scheduleAlarmFriendRequest(Context applicationContext) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, FriendRequestAlarmReceiver.class);

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
        AtomicBoolean sentWarning = new AtomicBoolean(false);
        db.collection("friend_requests")
                .whereEqualTo("receiver", me)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("users")
                                        .document(Objects.requireNonNull(document.getData().get("sender")).toString())
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                if (!sentWarning.get()) {
                                                    sentWarning.set(true);
                                                    scheduleAlarmFriendRequest(applicationContext);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

    }

    public static void deleteFaultyEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        checkFaulty(document.getId());
                    }
                }
            }
        });
    }

    private static void checkFaulty(String docID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(docID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String eventID = document.getId();
                    Map<String, Object> docData = document.getData();
                    Calendar current = Calendar.getInstance();
                    Calendar cldr_start = Calendar.getInstance();
                    Timestamp start_timestamp = (Timestamp) (Objects.requireNonNull(docData).get("datetime"));
                    Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                    cldr_start.setTime(start_date);
                    float minimum_pl = Float.parseFloat(Objects.requireNonNull(docData.get("minimum_players")).toString());
                    if (current.getTimeInMillis() > cldr_start.getTimeInMillis()) {
                        db.collection("event_attending").whereEqualTo("event", eventID).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                int size = task1.getResult().size();
                                if (size < minimum_pl) {
                                    deleteAnEvent(eventID);
                                }
                            }
                        });
                    }
                }
            });
    }

    private static void deleteAnEvent(String docID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(docID).delete();
        db.collection("event_attending").whereEqualTo("event", docID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        db.collection("event_attending").document(document.getId()).delete();
                    }
                }
            }
        });
    }

    public static void getAllSubscriberEvents(Context applicationContext) {
        deleteFaultyEvents();
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
                                String eventID = Objects.requireNonNull(document.getData().get("event")).toString();
                                DocumentReference docRef = db.collection("events").document(eventID);
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
                                            if (!sentWarning.get() && current.getTimeInMillis() > cldr_end.getTimeInMillis() && !Objects.requireNonNull(document.getData().get("rated")).toString().equals("true")) {
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
