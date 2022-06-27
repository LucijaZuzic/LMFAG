package com.example.lmfag.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.example.lmfag.activities.BaseActivity;
import com.example.lmfag.activities.MainActivity;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    private boolean connected;

    public ConnectionChangeReceiver(BaseActivity activity) {
        connected = isConnected(activity);
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (connected != isConnected(context)) {
            Intent changeActivityIntent = new Intent(context, MainActivity.class);
            changeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(changeActivityIntent);
        }
    }
}