package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsNearby extends AppCompatActivity {

    List<Map<String, Object>> matchingDocs = new ArrayList<Map<String, Object>>();
    List<String> docIds = new ArrayList<String>();
    RecyclerView eventNearbyRecycler;
    ImageView startSearch;
    Context context = this;
    SharedPreferences preferences;
    EditText enterRadius;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_nearby);
        eventNearbyRecycler = findViewById(R.id.recyclerViewEventsNearby);
        startSearch = findViewById(R.id.imageViewBeginSearchRadius);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enterRadius = findViewById(R.id.editTextRadius);
        startSearch.setOnClickListener(view -> {
            double radius = Double.parseDouble(enterRadius.getText().toString());
            getEventsThatAreInRadius(45, 14, radius);
        });
    }

    void getEventsThatAreInRadius(float latitude, float longitude, double radiusInKm) {

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