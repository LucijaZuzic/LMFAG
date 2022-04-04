package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Set;

public class ChooseLocation extends AppCompatActivity {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private TextView coordinatesView;
    private ImageButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        setContentView(R.layout.activity_choose_location);

        // Init text view
        coordinatesView = findViewById(R.id.coordinates);

        // Loading map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();

        // Centering map based on current location
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapController.setCenter(myLocationOverlay.getMyLocation());
                        chosenLocationMarker.setPosition(myLocationOverlay.getMyLocation());
                        String formattedLocation = String.format(
                                "Location:\nLatitude %.4f\nLongitude: %.4f",
                                myLocationOverlay.getMyLocation().getLatitude(), myLocationOverlay.getMyLocation().getLongitude()
                        );
                        coordinatesView.setText(formattedLocation);
                    }
                });
            }
        });
        map.getOverlays().add(myLocationOverlay);
        mapController.setZoom(17.0);

        // Location choosing on tap
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(ChooseLocation.this, "Setting new marker location...", Toast.LENGTH_SHORT).show();
                chosenLocationMarker.setPosition(p);
                return true;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        // Init marker
        chosenLocationMarker = new Marker(map);
        chosenLocationMarker.setDraggable(true);
        chosenLocationMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                String formattedLocation = String.format(
                        "Location:\nLatitude %.4f\nLongitude: %.4f",
                        marker.getPosition().getLatitude(), marker.getPosition().getLongitude()
                );
                coordinatesView.setText(formattedLocation);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(ChooseLocation.this, "Waiting for new location", Toast.LENGTH_SHORT).show();
            }
        });
        map.getOverlays().add(chosenLocationMarker);

        // Confirm button
        confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setMinimumHeight(confirmButton.getWidth());
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMarkerLocationToSP();
                finish();
            }
        });
    }

    private void saveMarkerLocationToSP() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putFloat("newEventLatitude", (float)chosenLocationMarker.getPosition().getLatitude());
        spEditor.putFloat("newEventLongitude", (float)chosenLocationMarker.getPosition().getLongitude());
        spEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}