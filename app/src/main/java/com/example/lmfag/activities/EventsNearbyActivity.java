package com.example.lmfag.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.receivers.LocationSettingChangedReceiver;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.Locateable;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EventsNearbyActivity extends MenuInterfaceActivity implements TextWatcher, Locateable {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private List<String> docIds;
    private List<Integer> timestamps_array;
    private List<DocumentSnapshot> snapshots;
    private RecyclerView eventNearbyRecycler;
    private Context context;
    private EditText enterRadius;
    private TextView noResults;
    private org.osmdroid.views.overlay.Polygon oPolygon = null;
    private EditText enterLongitude, enterLatitude;
    private SwitchCompat switchMapOnOff;
    private Chip upcoming, current, past;
    private LocationSettingChangedReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_nearby);
        context = this;

        locationReceiver = new LocationSettingChangedReceiver(this);
        docIds = new ArrayList<>();
        snapshots = new ArrayList<>();
        timestamps_array = new ArrayList<>();

        noResults = findViewById(R.id.noResults);
        map = findViewById(R.id.map);
        enterLatitude = findViewById(R.id.inputLatitude);
        enterLongitude = findViewById(R.id.inputLongitude);
        enterLatitude.addTextChangedListener(this);
        enterLongitude.addTextChangedListener(this);

        upcoming = findViewById(R.id.upcoming);
        current = findViewById(R.id.current);
        past = findViewById(R.id.past);
        upcoming.setOnClickListener((v) -> changeTimestampVisible());
        current.setOnClickListener((v) -> changeTimestampVisible());
        past.setOnClickListener((v) -> changeTimestampVisible());

        eventNearbyRecycler = findViewById(R.id.recyclerViewEventsNearby);
        ImageView startSearch = findViewById(R.id.imageViewBeginSearchRadius);
        enterRadius = findViewById(R.id.editTextRadius);
        firstMapSetup();
        startSearch.setOnClickListener(view -> {
            if (enterRadius.getText().toString().length() != 0 && enterLatitude.getText().toString().length() != 0 && enterLongitude.getText().toString().length() != 0) {
                double radius = Double.parseDouble(enterRadius.getText().toString());
                float temp_latitude = Float.parseFloat(enterLatitude.getText().toString().replace(',', '.'));
                float temp_longitude = Float.parseFloat(enterLongitude.getText().toString().replace(',', '.'));
                getEventsThatAreInRadius(temp_latitude, temp_longitude, radius);
                if (oPolygon != null) {
                    map.getOverlays().remove(oPolygon);
                }
                oPolygon = new org.osmdroid.views.overlay.Polygon(map);
                ArrayList<org.osmdroid.util.GeoPoint> circlePoints = new ArrayList<>();
                for (float f = 0; f < 360; f += 1) {
                    try {
                        circlePoints.add(new org.osmdroid.util.GeoPoint(temp_latitude, temp_longitude).destinationPoint(radius * 1000, f));
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(context, R.string.radius_too_large, Toast.LENGTH_SHORT).show();
                    }
                }
                try {
                    oPolygon.setPoints(circlePoints);
                    map.getOverlays().add(oPolygon);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(context, R.string.radius_too_large, Toast.LENGTH_SHORT).show();
                }
            } else {
                docIds.clear();
                map.getOverlays().clear();
                map.getOverlays().add(myLocationOverlay);
                map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
                map.getOverlays().add(chosenLocationMarker);
                mapController.setCenter(chosenLocationMarker.getPosition());
                CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(docIds, context, preferences);
                eventNearbyRecycler.setAdapter(customAdapterEvents);
            }
        });
        switchMapOnOff = findViewById(R.id.switchMapOnOff);
        switchMapOnOff.setOnClickListener(view -> changeTimestampVisible());

    }
    // Location choosing on tap
    private final MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(org.osmdroid.util.GeoPoint p) {
            return false;
        }

        @Override
        public boolean longPressHelper(org.osmdroid.util.GeoPoint p) {
            Toast.makeText(getApplicationContext(), getString(R.string.setting_location), Toast.LENGTH_SHORT).show();
            docIds.clear();
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            chosenLocationMarker.setPosition(p);
            mapController.setCenter(chosenLocationMarker.getPosition());

            enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", p.getLatitude()).replace(',', '.'));
            enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", p.getLongitude()).replace(',', '.'));

            return true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        getApplicationContext().registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(locationReceiver);
        myLocationOverlay.disableMyLocation();
        //this will refresh the osmdroid configuration on resuming.
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void firstMapSetup() {
        //Request permission dialog
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            /* Do something Boolean fineLocationGranted = GetOrDefault.getOrDefault(result, Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = GetOrDefault.getOrDefault(result, Manifest.permission.ACCESS_COARSE_LOCATION,false);
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

        // Loading map
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

                docIds.clear();
                map.getOverlays().clear();
                map.getOverlays().add(myLocationOverlay);
                map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
                map.getOverlays().add(chosenLocationMarker);
                mapController.setCenter(chosenLocationMarker.getPosition());
                enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLatitude()).replace(',', '.'));
                enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", chosenLocationMarker.getPosition().getLongitude()).replace(',', '.'));
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(getApplicationContext(), getString(R.string.waiting_location), Toast.LENGTH_SHORT).show();
            }
        });
        map.getOverlays().add(chosenLocationMarker);
    }

    private int checkTimestamp(Calendar cldr_start, Calendar cldr_end) {
        if (cldr_start.getTime().after(Calendar.getInstance().getTime()) && cldr_end.getTime().after(Calendar.getInstance().getTime())) {
            return 0;
        }
        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) && cldr_end.getTime().after(Calendar.getInstance().getTime())) {
            return 1;
        }
        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) && cldr_end.getTime().before(Calendar.getInstance().getTime())) {
            return 2;
        }
        return -1;
    }

    private void addEventMarker(DocumentSnapshot doc) {
        Marker newMarker = new Marker(map);
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(doc.getString("event_type"))));
        Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(unwrappedDrawable));
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.brown));
        newMarker.setIcon(wrappedDrawable);
        newMarker.setTitle(doc.getString("event_name"));
        newMarker.setSnippet(EventTypeToDrawable.getEventTypeToTranslation(this, Objects.requireNonNull(doc.getString("event_type"))));
        com.google.firebase.firestore.GeoPoint location_point = (com.google.firebase.firestore.GeoPoint) (doc.get("location"));
        double tmp_latitude = Objects.requireNonNull(location_point).getLatitude();
        double tmp_longitude = location_point.getLongitude();
        newMarker.setPosition(new org.osmdroid.util.GeoPoint(tmp_latitude, tmp_longitude));
        newMarker.setSubDescription(doc.getString("event_description"));
        newMarker.setOnMarkerClickListener((marker, mapView) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("eventID", doc.getId());
            editor.apply();
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            startActivity(myIntent);
            finish();
            return false;
        });
        newMarker.setDraggable(false);
        map.getOverlays().add(newMarker);
    }

    public void changeTimestampVisible() {
        List<String> events_array_selected_time = new ArrayList<>();
        List<DocumentSnapshot> snaps = new ArrayList<>();
        for (int i = 0, n = docIds.size(); i < n; i++) {
            if (upcoming.isChecked() && timestamps_array.get(i) == 0) {
                events_array_selected_time.add(docIds.get(i));
                snaps.add(snapshots.get(i));
            }
            if (current.isChecked() && timestamps_array.get(i) == 1) {
                events_array_selected_time.add(docIds.get(i));
                snaps.add(snapshots.get(i));
            }
            if (past.isChecked() && timestamps_array.get(i) == 2) {
                events_array_selected_time.add(docIds.get(i));
                snaps.add(snapshots.get(i));
            }
        }
        Collections.sort(events_array_selected_time);
        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array_selected_time, context, preferences);
        eventNearbyRecycler.setAdapter(customAdapterEvents);
        if (!switchMapOnOff.isChecked()) {
            map.setVisibility(View.GONE);
            if (events_array_selected_time.isEmpty()) {
                noResults.setVisibility(View.VISIBLE);
                eventNearbyRecycler.setVisibility(View.GONE);
            } else {
                noResults.setVisibility(View.GONE);
                eventNearbyRecycler.setVisibility(View.VISIBLE);
            }
        } else {
            map.setVisibility(View.VISIBLE);
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            mapController.setCenter(chosenLocationMarker.getPosition());
            for (DocumentSnapshot s : snaps) {
                addEventMarker(s);
            }
            if (snaps.isEmpty()) {
                Toast.makeText(context, R.string.no_results, Toast.LENGTH_SHORT).show();
            }
            noResults.setVisibility(View.GONE);
            eventNearbyRecycler.setVisibility(View.GONE);
        }
    }

    private void getEventsThatAreInRadius(float latitude, float longitude, double radiusInKm) {
        // Find cities within a distance in kilometers of location
        final GeoLocation center = new GeoLocation(latitude, longitude);
        final double radiusInM = radiusInKm * 1000;

        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
        // a separate query for each pair. There can be up to 9 pairs of bounds
        // depending on overlap, but in most cases there are 4.
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("events")
                    .orderBy("geo_hash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }
        docIds.clear();
        snapshots.clear();
        timestamps_array.clear();
        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(t -> {
                    for (Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snap = task.getResult();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            com.google.firebase.firestore.GeoPoint gp = doc.getGeoPoint("location");
                            double lat = Objects.requireNonNull(gp).getLatitude();
                            double lng = gp.getLongitude();

                            // We have to filter out a few false positives due to GeoHash
                            // accuracy, but most will match
                            GeoLocation docLocation = new GeoLocation(lat, lng);
                            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                            if (distanceInM <= radiusInM) {
                                docIds.add(doc.getId());
                                Calendar cldr_start = Calendar.getInstance();
                                Timestamp start_timestamp = (Timestamp) (doc.get("datetime"));
                                Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                                cldr_start.setTime(start_date);
                                Calendar cldr_end = Calendar.getInstance();
                                Timestamp end_timestamp = (Timestamp) (doc.get("ending"));
                                Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                                cldr_end.setTime(end_date);
                                timestamps_array.add(checkTimestamp(cldr_start, cldr_end));
                                snapshots.add(doc);
                            }
                        }
                    }
                    changeTimestampVisible();

                });
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

        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }


    @Override
    public void setMyGpsLocation() {
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            docIds.clear();
            map.getOverlays().clear();
            map.getOverlays().add(myLocationOverlay);
            map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
            map.getOverlays().add(chosenLocationMarker);
            chosenLocationMarker.setPosition(myLocationOverlay.getMyLocation());
            mapController.setCenter(myLocationOverlay.getMyLocation());

            enterLatitude.setText(String.format(Locale.getDefault(), "%.4f", myLocationOverlay.getMyLocation().getLatitude()).replace(',', '.'));
            enterLongitude.setText(String.format(Locale.getDefault(), "%.4f", myLocationOverlay.getMyLocation().getLongitude()).replace(',', '.'));

        }));
    }
}