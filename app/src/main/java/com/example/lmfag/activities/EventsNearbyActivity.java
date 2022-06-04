package com.example.lmfag.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EventsNearbyActivity extends MenuInterfaceActivity {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private List<String> docIds;
    private RecyclerView eventNearbyRecycler;
    private Context context;
    private EditText enterRadius;
    private org.osmdroid.views.overlay.Polygon oPolygon = null;
    private EditText enterLongitude, enterLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_nearby);
        context = this;
        docIds = new ArrayList<>();
        

        map = findViewById(R.id.map);
        enterLatitude = findViewById(R.id.inputLatitude);
        enterLongitude = findViewById(R.id.inputLongitude);
        ImageView updateButton = findViewById(R.id.updateLocation);

        eventNearbyRecycler = findViewById(R.id.recyclerViewEventsNearby);
        ImageView startSearch = findViewById(R.id.imageViewBeginSearchRadius);
        enterRadius = findViewById(R.id.editTextRadius);
        firstMapSetup();
        startSearch.setOnClickListener(view -> {
            double radius = Double.parseDouble(enterRadius.getText().toString());
            float temp_latitude = Float.parseFloat(enterLatitude.getText().toString());
            float temp_longitude = Float.parseFloat(enterLongitude.getText().toString());
            if (enterRadius.getText().toString().length() != 0 && enterLatitude.getText().toString().length() != 0 && enterLongitude.getText().toString().length() != 0) {
                getEventsThatAreInRadius(temp_latitude, temp_longitude, radius);
                if (oPolygon != null) {
                    map.getOverlays().remove(oPolygon);
                }
                oPolygon = new org.osmdroid.views.overlay.Polygon(map);
                ArrayList<org.osmdroid.util.GeoPoint> circlePoints = new ArrayList<>();
                for (float f = 0; f < 360; f += 1) {
                    circlePoints.add(new org.osmdroid.util.GeoPoint(temp_latitude, temp_longitude).destinationPoint(radius * 1000, f));
                }
                oPolygon.setPoints(circlePoints);
                map.getOverlays().add(oPolygon);
            }
        });
        updateButton.setOnClickListener(view -> {
            if (enterLongitude.getText().toString().length() != 0 && enterLatitude.getText().toString().length() != 0) {
                float temp_latitude = Float.parseFloat(enterLatitude.getText().toString());
                float temp_longitude = Float.parseFloat(enterLongitude.getText().toString());
                chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(temp_latitude, temp_longitude));

                mapController.setCenter(chosenLocationMarker.getPosition());
                /* How to format String formattedLocation = getString(R.string.marker_location) + ":\n" + getString(R.string.latitude) + ": " +
                        Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + "\n"
                        + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0; */

            }
        });
        SwitchCompat switchMapOnOf = findViewById(R.id.switchMapOnOff);
        switchMapOnOf.setOnClickListener(view -> {
            if (eventNearbyRecycler.getVisibility() == View.GONE) {
                map.setVisibility(View.GONE);
                eventNearbyRecycler.setVisibility(View.VISIBLE);
            } else {
                map.setVisibility(View.VISIBLE);
                eventNearbyRecycler.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
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

        myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            mapController.setCenter(myLocationOverlay.getMyLocation());
            enterLatitude.setText(String.format(Locale.getDefault(), "%.6f", myLocationOverlay.getMyLocation().getLatitude()));
            enterLongitude.setText(String.format(Locale.getDefault(), "%.6f", myLocationOverlay.getMyLocation().getLongitude()));
            chosenLocationMarker.setPosition(myLocationOverlay.getMyLocation());
            /* How to format String formattedLocation = getString(org.osmdroid.library.R.string.my_location) + ":\n" + getString(R.string.latitude) + ": " +
                    Math.round(myLocationOverlay.getMyLocation().getLatitude() * 10000) / 10000.0 + "\n"
                    + getString(R.string.longitude) + ": " + Math.round(myLocationOverlay.getMyLocation().getLongitude() * 10000) / 10000.0; */
        }));
        map.getOverlays().add(myLocationOverlay);
        mapController.setZoom(17.0);

        // Location choosing on tap
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(org.osmdroid.util.GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(org.osmdroid.util.GeoPoint p) {
                Toast.makeText(getApplicationContext(), getString(R.string.setting_location), Toast.LENGTH_SHORT).show();
                chosenLocationMarker.setPosition(p);
                enterLatitude.setText(String.format(Locale.getDefault(), "%.6f", p.getLatitude()));
                enterLongitude.setText(String.format(Locale.getDefault(), "%.6f", p.getLongitude()));
                /* How to format String formattedLocation = getString(R.string.marker_location) + ": " + getString(R.string.latitude) + ": " +
                        Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + " "
                        + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0; */

                mapController.setCenter(chosenLocationMarker.getPosition());
                return true;
            }
        };
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
                /* How to format String formattedLocation = getString(R.string.marker_location) + ": " + getString(R.string.latitude) + ": " +
                        Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0 + " "
                        + getString(R.string.longitude) + ": " + Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0; */

                mapController.setCenter(chosenLocationMarker.getPosition());
                enterLatitude.setText(String.format(Locale.getDefault(), "%.6f", chosenLocationMarker.getPosition().getLatitude()));
                enterLongitude.setText(String.format(Locale.getDefault(), "%.6f", chosenLocationMarker.getPosition().getLongitude()));
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(getApplicationContext(), getString(R.string.waiting_location), Toast.LENGTH_SHORT).show();
            }
        });
        map.getOverlays().add(chosenLocationMarker);
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
        if (!docIds.isEmpty()) {
            docIds.clear();
            map.getOverlays().clear();
            map.getOverlays().add(chosenLocationMarker);
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
                                    Marker newMarker = new Marker(map);
                                    Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(doc.getString("event_type"))));
                                    Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(unwrappedDrawable));
                                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.brown));
                                    newMarker.setIcon(wrappedDrawable);
                                    newMarker.setTitle(doc.getString("event_name"));
                                    newMarker.setSnippet(doc.getString("event_type"));
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
                            }
                        }
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(docIds, context, preferences);
                        eventNearbyRecycler.setAdapter(customAdapterEvents);
                    });
        }
    }
}