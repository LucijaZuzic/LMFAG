package com.example.lmfag.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.example.lmfag.activities.ChooseLocationActivity;
import com.example.lmfag.utility.Locateable;

import org.osmdroid.api.IMapController;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class LocationSettingChangedReceiver extends BroadcastReceiver {
    private Locateable locateable;

    public LocationSettingChangedReceiver(Locateable locateable) {
        this.locateable = locateable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGpsEnabled) {
                locateable.setMyGpsLocation();
            }
        }
    }
}
