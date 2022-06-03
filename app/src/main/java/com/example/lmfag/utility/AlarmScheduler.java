package com.example.lmfag.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lmfag.receivers.EventAlarmReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class AlarmScheduler {

    public static void scheduleAlarm(Context applicationContext, long timeInMillis, String icon, String name, String description, String eventID) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, EventAlarmReceiver.class);
        alarmReceiverIntent.putExtra("icon",icon);
        alarmReceiverIntent.putExtra("name",name);
        alarmReceiverIntent.putExtra("description",description);
        alarmReceiverIntent.putExtra("eventID",eventID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        }
    }
    public static void cancelAllAlarms(Context applicationContext) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, EventAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(applicationContext, "Cancelled all alarms.", Toast.LENGTH_SHORT).show();
    }
    public static void getAllSubscriberEvents(Context applicationContext) {
        cancelAllAlarms(applicationContext);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String isSubscribed = document.getData().get("notifications").toString();
                                if (isSubscribed.equals("true")) {
                                    DocumentReference docRef = db.collection("events").document(document.getData().get("event").toString());
                                    docRef.get().addOnCompleteListener(taskTime -> {
                                        if (taskTime.isSuccessful()) {
                                            DocumentSnapshot documentTime = taskTime.getResult();
                                            if (documentTime.exists()) {
                                                Map<String, Object> docData = documentTime.getData();
                                                Timestamp start_timestamp = (Timestamp)(docData.get("datetime"));
                                                Date start_date = start_timestamp.toDate();
                                                Calendar cldr_start = Calendar.getInstance();
                                                cldr_start.setTime(start_date);
                                                Timestamp end_timestamp = (Timestamp)(docData.get("ending"));
                                                Date end_date = end_timestamp.toDate();
                                                Calendar cldr_end = Calendar.getInstance();
                                                cldr_end.setTime(end_date);
                                                Calendar current = Calendar.getInstance();
                                                if (current.getTimeInMillis() < cldr_start.getTimeInMillis()) {
                                                    scheduleAlarm(applicationContext, cldr_start.getTimeInMillis() - current.getTimeInMillis(), docData.get("event_type").toString(), docData.get("event_name").toString(), docData.get("event_description").toString(), documentTime.getId());
                                                    Toast.makeText(applicationContext, "Scheduled an alarm for event " + docData.get("event_name").toString() + " in " + (cldr_start.getTimeInMillis() - current.getTimeInMillis())  / 1000 + " seconds", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                            }
                                        }
                                    });
                                }
                            }
                        } else {

                        }
                    } else {

                    }
                }
            });
        }
    }
}
