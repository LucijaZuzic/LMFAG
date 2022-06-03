package com.example.lmfag.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.lmfag.receivers.EventAlarmReceiver;

public class AlarmScheduler {
    public static void scheduleAlarm(Context applicationContext, int timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmReceiverIntent = new Intent(applicationContext, EventAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, alarmReceiverIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis - System.currentTimeMillis(), pendingIntent);
    }
}
