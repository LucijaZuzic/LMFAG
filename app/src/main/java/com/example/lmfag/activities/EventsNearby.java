package com.example.lmfag.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.example.lmfag.utility.DrawerHelper;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
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
import java.util.Map;

public class EventsNearby extends MenuInterface {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private List<Map<String, Object>> matchingDocs = new ArrayList<Map<String, Object>>();
    private List<String> docIds = new ArrayList<String>();
    private RecyclerView eventNearbyRecycler;
    private ImageView startSearch;
    private Context context = this;
    private SharedPreferences preferences;
    private EditText enterRadius;
    private ImageView updateButton;

    private EditText enterLongitude, enterLatitude;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_nearby);

        DrawerHelper.fillNavbarData(this);

        enterLatitude = findViewById(R.id.inputLatitude);
        enterLongitude = findViewById(R.id.inputLongitude);
        updateButton = findViewById(R.id.updateLocation);

        eventNearbyRecycler = findViewById(R.id.recyclerViewEventsNearby);
        startSearch = findViewById(R.id.imageViewBeginSearchRadius);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enterRadius = findViewById(R.id.editTextRadius);
        firstMapSetup();
        startSearch.setOnClickListener(view -> {
            double radius = Double.parseDouble(enterRadius.getText().toString());
            Float temp_latitude = Float.parseFloat(enterLatitude.getText().toString());
            Float temp_longitude = Float.parseFloat(enterLongitude.getText().toString());
            if (enterRadius.getText().toString().length() != 0 && enterLatitude.getText().toString().length() != 0 && enterLongitude.getText().toString().length() != 0) {
                getEventsThatAreInRadius(temp_latitude, temp_longitude, radius);
            }
        });
        updateButton.setOnClickListener(view -> {
            if (enterLongitude.getText().toString().length() != 0 && enterLatitude.getText().toString().length() != 0) {
                Float temp_latitude = Float.parseFloat(enterLatitude.getText().toString());
                Float temp_longitude = Float.parseFloat(enterLongitude.getText().toString());
                chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(temp_latitude, temp_longitude));

                mapController.setCenter(chosenLocationMarker.getPosition());
                String formattedLocation = getString(R.string.marker_location) + ": " + getString(R.string.latitude) + ": " +
                        Double.toString(Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0) + " "
                        + getString(R.string.longitude) + ": " + Double.toString(Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0);

            }
        });
    }

    private void firstMapSetup() {
        //Request permission dialog
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = getOrDefault(result, Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = getOrDefault(result, Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        // ...

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

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
                        enterLatitude.setText(Double.toString(myLocationOverlay.getMyLocation().getLatitude()));
                        enterLongitude.setText(Double.toString(myLocationOverlay.getMyLocation().getLongitude()));
                        chosenLocationMarker.setPosition(myLocationOverlay.getMyLocation());
                        String formattedLocation = getString(org.osmdroid.library.R.string.my_location) + ": " + getString(R.string.latitude) + ": " +
                                Double.toString(Math.round(myLocationOverlay.getMyLocation().getLatitude() * 10000) / 10000.0) + " "
                                + getString(R.string.longitude) + ": " + Double.toString(Math.round(myLocationOverlay.getMyLocation().getLongitude() * 10000) / 10000.0);
                    }
                });
            }
        });
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
                Toast.makeText(EventsNearby.this, getString(R.string.setting_location), Toast.LENGTH_SHORT).show();
                chosenLocationMarker.setPosition(p);
                enterLatitude.setText(Double.toString(p.getLatitude()));
                enterLongitude.setText(Double.toString(p.getLongitude()));
                String formattedLocation = getString(R.string.marker_location) + ": " + getString(R.string.latitude) + ": " +
                        Double.toString(Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0) + " "
                        + getString(R.string.longitude) + ": " + Double.toString(Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0);

                mapController.setCenter(chosenLocationMarker.getPosition());
                return true;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        // Init marker
        chosenLocationMarker = new Marker(map);
        chosenLocationMarker.setIcon(getDrawable(R.drawable.map_marker));
        chosenLocationMarker.setDraggable(true);
        chosenLocationMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                String formattedLocation = getString(R.string.marker_location) + ": " + getString(R.string.latitude) + ": " +
                        Double.toString(Math.round(chosenLocationMarker.getPosition().getLatitude() * 10000) / 10000.0) + " "
                        + getString(R.string.longitude) + ": " + Double.toString(Math.round(chosenLocationMarker.getPosition().getLongitude() * 10000) / 10000.0);

                mapController.setCenter(chosenLocationMarker.getPosition());
                enterLatitude.setText(Double.toString(chosenLocationMarker.getPosition().getLatitude()));
                enterLongitude.setText(Double.toString(chosenLocationMarker.getPosition().getLongitude()));
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(EventsNearby.this, getString(R.string.waiting_location), Toast.LENGTH_SHORT).show();
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
        if (matchingDocs != null) {
            matchingDocs.clear();
            docIds.clear();
            map.getOverlays().clear();
            map.getOverlays().add(chosenLocationMarker);
            // Collect all the query results together into a single list
            Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Task<?>>> t) {
                            List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                            for (Task<QuerySnapshot> task : tasks) {
                                QuerySnapshot snap = task.getResult();
                                for (DocumentSnapshot doc : snap.getDocuments()) {
                                    GeoPoint gp = doc.getGeoPoint("location");
                                    double lat = gp.getLatitude();
                                    double lng = gp.getLongitude();

                                    // We have to filter out a few false positives due to GeoHash
                                    // accuracy, but most will match
                                    GeoLocation docLocation = new GeoLocation(lat, lng);
                                    double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                    if (distanceInM <= radiusInM) {
                                        matchingDocs.add(doc);
                                        docIds.add(doc.getId());
                                        Marker newMarker = new Marker(map);
                                        Drawable unwrappedDrawable = getDrawable(EventTypeToDrawable.getEventTypeToDrawable(doc.getString("event_type")));
                                        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                                        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.my_pink));
                                        newMarker.setIcon(wrappedDrawable);
                                        newMarker.setTitle(doc.getString("event_name"));
                                        newMarker.setSnippet(doc.getString("event_type"));
                                        GeoPoint location_point = (GeoPoint)(doc.get("location"));
                                        double tmp_latitude = location_point.getLatitude();
                                        double tmp_longitude = location_point.getLongitude();
                                        newMarker.setPosition(new org.osmdroid.util.GeoPoint(tmp_latitude, tmp_longitude));
                                        newMarker.setSubDescription(doc.getString("event_description"));
                                        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                           @Override
                                           public boolean onMarkerClick(Marker marker, MapView mapView) {
                                               SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                               SharedPreferences.Editor editor = preferences.edit();
                                               editor.putString("eventID", doc.getId());
                                               editor.apply();
                                               Intent myIntent = new Intent(context, ViewEvent.class);
                                               startActivity(myIntent);
                                               return false;
                                           }
                                       });
                                       newMarker.setDraggable(false);
                                       map.getOverlays().add(newMarker);
                                    }
                                }
                            }

                            // matchingDocs contains the results
                            // ...

                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(docIds, context, preferences);
                            eventNearbyRecycler.setAdapter(customAdapterEvents);
                        }
                    });
        }
    }
}