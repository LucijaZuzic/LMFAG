package com.example.lmfag.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lmfag.utility.AlarmScheduler;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmScheduler.getAllSubscriberEvents(context.getApplicationContext());
        }
    }

}
