package com.example.lmfag.activities;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.lmfag.R;
import com.example.lmfag.receivers.LocationSettingChangedReceiver;
import com.example.lmfag.utility.Locateable;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Locale;


public class ChooseLocationActivity extends MenuInterfaceActivity implements TextWatcher, Locateable {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private TextView coordinatesView;
    private TextView markerView;
    private EditText enterLongitude, enterLatitude;
    private LocationSettingChangedReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        Context context = this;
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        locationReceiver = new LocationSettingChangedReceiver(this);

        enterLatitude = findViewById(R.id.inputLatitude);
        enterLongitude = findViewById(R.id.inputLongitude);
        enterLatitude.addTextChangedListener(this);
        enterLongitude.addTextChangedListener(this);

        //Request permission dialog
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            /*Do something Boolean fineLocationGranted = GetOrDefault.getOrDefault(result, Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = GetOrDefault.getOrDefault(result, Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }*/
                        }
                );
        // ...

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Init text view
        markerView = findViewById(R.id.marker);
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

        setMyGpsLocation();
        map.getOverlays().add(myLocationOverlay);
        mapController.setZoom(17.0);

        map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        // Init marker
        chosenLocationMarker = new Marker(map);
        chosenLocationMarker.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.map_marker));
        chosenLocationMarker.setDraggable(true);
        chosenLocationMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                map.getOverlays().clear();
                map.getOverlays().add(myLocationOverlay);
                map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
                map.getOverlays().add(chosenLocationMarker);
                mapController.setCenter(chosenLocationMarker.getPosition());

                enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLatitude()).replace(',', '.'));
                enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLongitude()).replace(',', '.'));
                String formattedLocation = getString(R.string.marker_location) + ":\n" + getString(R.string.latitude) + ": " +
                        Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + "\n"
                        + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0;
                markerView.setText(formattedLocation);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(getApplicationContext(), getString(R.string.waiting_location), Toast.LENGTH_SHORT).show();
            }
        });
        map.getOverlays().add(chosenLocationMarker);

        // Confirm button
        ImageView confirmButton = findViewById(R.id.confirm_button);

        confirmButton.setOnClickListener(view -> {
            saveMarkerLocationToSP();
            finish();
        });

    }
    // Location choosing on tap
    private final MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            Toast.makeText(getApplicationContext(), getString(R.string.setting_location), Toast.LENGTH_SHORT).show();
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            mapController.setCenter(chosenLocationMarker.getPosition());

            enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", p.getLatitude()).replace(',', '.'));
            enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", p.getLongitude()).replace(',', '.'));
            String formattedLocation = getString(R.string.marker_location) + ":\n" + getString(R.string.latitude) + ": " +
                    Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + "\n"
                    + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0;
            markerView.setText(formattedLocation);
            return true;
        }
    };

    private void saveMarkerLocationToSP() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putFloat("newEventLatitude", (float) chosenLocationMarker.getPosition().getLatitude());
        spEditor.putFloat("newEventLongitude", (float) chosenLocationMarker.getPosition().getLongitude());
        spEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(locationReceiver, intentFilter);
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        myLocationOverlay.disableMyLocation();
        unregisterReceiver(locationReceiver);
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        float temp_latitude = 0.0f, temp_longitude = 0.0f;
        if (enterLatitude.getText().toString().length() > 0) {
            temp_latitude = Float.parseFloat(enterLatitude.getText().toString().replace(',', '.'));
            if (temp_latitude > 85) {
                enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLatitude()).replace(',', '.'));
                temp_latitude = (float) chosenLocationMarker.getPosition().getLatitude();
            }
            if (temp_latitude < -85) {
                enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLatitude()).replace(',', '.'));
                temp_latitude = (float) chosenLocationMarker.getPosition().getLatitude();
            }
        }
        if (enterLongitude.getText().toString().length() > 0) {
            temp_longitude = Float.parseFloat(enterLongitude.getText().toString().replace(',', '.'));
            if (temp_longitude > 180) {
                enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLongitude()).replace(',', '.'));
                temp_longitude = (float) chosenLocationMarker.getPosition().getLongitude();
            }
            if (temp_longitude < -180) {
                enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLongitude()).replace(',', '.'));
                temp_longitude = (float) chosenLocationMarker.getPosition().getLongitude();
            }
        }
        if (enterLongitude.getText().toString().length() > 0 && enterLatitude.getText().toString().length() > 0) {
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            chosenLocationMarker.setPosition(new GeoPoint(temp_latitude, temp_longitude));
            mapController.setCenter(chosenLocationMarker.getPosition());

            String formattedLocation = getString(R.string.marker_location) + ":\n" + getString(R.string.latitude) + ": " +
                    Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + "\n"
                    + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0;
            markerView.setText(formattedLocation);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void setMyGpsLocation() {
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            mapController.setCenter(chosenLocationMarker.getPosition());

            enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", myLocationOverlay.getMyLocation().getLatitude()).replace(',', '.'));
            enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", myLocationOverlay.getMyLocation().getLongitude()).replace(',', '.'));
            String formattedLocation = getString(R.string.my_location) + ":\n" + getString(R.string.latitude) + ": " +
                    Math.round(myLocationOverlay.getMyLocation().getLatitude() * 10000) / 10000.0 + "\n"
                    + getString(R.string.longitude) + ": " + Math.round(myLocationOverlay.getMyLocation().getLongitude() * 10000) / 10000.0;
            coordinatesView.setText(formattedLocation);
        }));
    }
}