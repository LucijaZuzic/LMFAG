package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.example.lmfag.BuildConfig;
import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewEventActivity extends MenuInterfaceActivity {
    private Context context = this;
    final Calendar cldr_start = Calendar.getInstance();
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    private SwitchCompat switch_notify;
    final Calendar cldr_end = Calendar.getInstance();
    private String event_type;
    private ImageView apply, edit, rate;
    private TextView textViewChooseStartDate, textViewChooseStartTime, myUsername, textViewChooseEndDate, textViewChooseEndTime, eventName, eventType, location, description, minimumlevel, switchpublic, switchout, slider;
    private double longitude = 45.23;
    private double latitude = 45.36;
    private String organizer;
    private boolean public_event;
    private Float participate_minimum, participate_maximum;
    private Float minimum_level;
    private CircleImageView circleImageView;
    private SharedPreferences preferences;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        map = findViewById(R.id.map);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);
        circleImageView = findViewById(R.id.profile_image);
        switch_notify = findViewById(R.id.switchNotifications);

         eventName = findViewById(R.id.textViewEventName);
         eventType = findViewById(R.id.textViewEventType);
         description = findViewById(R.id.textViewEvenDescription);
         minimumlevel = findViewById(R.id.textViewMinimumLevel);
         switchpublic = findViewById(R.id.textViewPublic);
         switchout = findViewById(R.id.textViewOutdoor);
         location = findViewById(R.id.textViewChooseLocation);
         slider = findViewById(R.id.textViewNumberOfPlayers);
         myUsername = findViewById(R.id.textViewOrganizer);
        apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> {
            subscribe();
        });

        switch_notify.setOnClickListener(view -> {
            changeNotify();
        });

        edit = findViewById(R.id.imageViewEdit);
        edit.setOnClickListener(view -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String me = preferences.getString("userID", "");
            if (me.equals(organizer)) {
                if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                     Toast.makeText(getApplicationContext(), R.string.edit_finished, Toast.LENGTH_SHORT).show();
                    //Intent myIntent = new Intent(context, ViewEventActivity.class);
                    //startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(context, CreateEventActivity.class);
                    context.startActivity(myIntent);
                }
            } else {
                 Toast.makeText(getApplicationContext(), R.string.organizer_edit, Toast.LENGTH_SHORT).show();
            }
        });

        rate = findViewById(R.id.imageViewRate);
        rate.setOnClickListener(view -> {
            String eventID = preferences.getString("eventID", "");
            String userID = preferences.getString("userID", "");

            if (Calendar.getInstance().getTime().before(cldr_end.getTime())) {
                 Toast.makeText(getApplicationContext(), R.string.rate_before_end, Toast.LENGTH_SHORT).show();
            } else {
                    CollectionReference docRef = db.collection("event_attending");
                    docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0 && !userID.equals(organizer)) {
                                 Toast.makeText(getApplicationContext(), R.string.not_participate_rate, Toast.LENGTH_SHORT).show();
                            } else {
                                boolean found = false;
                                for (QueryDocumentSnapshot doc: task.getResult()) {
                                    if (doc.getData().get("rated").toString().equals("true")) {
                                        Toast.makeText(getApplicationContext(), R.string.rate_twice, Toast.LENGTH_SHORT).show();
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    Intent myIntent = new Intent(context, RateEventActivity.class);
                                    context.startActivity(myIntent);
                                }
                            }
                        }
                    });
            }
        });
        firstMapSetup();
    }



    private void changeNotify() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");

        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() != 0) {
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        Map<String, Object> docData = doc.getData();
                        docData.remove("notifications");
                        docData.put("notifications", switch_notify.isChecked());
                        db.collection("event_attending")
                                .document(doc.getId())
                                .set(docData);

                    }
                }
            }
        });
    }
    private void firstMapSetup() {
        // Loading map
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();

        chosenLocationMarker = new Marker(map);
        chosenLocationMarker.setDraggable(false);
        chosenLocationMarker.setIcon(getDrawable(R.drawable.map_marker));
        // Centering map based on current location

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        myLocationOverlay.disableMyLocation();
        myLocationOverlay.disableFollowLocation();
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));

        map.getOverlays().add(chosenLocationMarker);
        mapController.setZoom(17.0);
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
        checkSubscribed();
        latitude = preferences.getFloat("newEventLatitude", (float)latitude);
        longitude = preferences.getFloat("newEventLongitude", (float)longitude);
        String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + "\n"
                + getString(R.string.longitude) + ": " + Double.toString(Math.round(longitude * 10000) / 10000.0);
        location.setText(formattedLocation);
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    private void refresh() {
        Intent myIntent = new Intent(context, ViewEventActivity.class);
        context.startActivity(myIntent);
    }

    private void checkSubscribed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    apply.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_add_24));
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        Map<String, Object> map = doc.getData();
                        if (map.get("rating").toString().equals("true")) {
                            rate.setImageDrawable(getDrawable(R.drawable.ic_baseline_star_24));
                        } else {
                            rate.setImageDrawable(getDrawable(R.drawable.ic_baseline_star_outline_24));
                        }
                    }
                } else {
                    apply.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_remove_24));
                    rate.setImageDrawable(getDrawable(R.drawable.ic_baseline_star_outline_24));
                }
            }
        });
    }

    private void checkOtherDirection(CollectionReference docuRef, Map<String, Object> docData) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dr = db.collection("friends");
            dr.document(userID).get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    DocumentSnapshot document2 = task2.getResult();
                    if (document2.exists()) {
                        if (!document2.getData().get("friends").toString().contains(userID)) {
                             Toast.makeText(getApplicationContext(), R.string.not_friend_organizer, Toast.LENGTH_SHORT).show();
                        } else {
                            docuRef.add(docData);

                            refresh();
                        }
                    } else {
                         Toast.makeText(getApplicationContext(), R.string.not_friend_organizer, Toast.LENGTH_SHORT).show();
                    }
                } else {
                     Toast.makeText(getApplicationContext(), R.string.not_friend_organizer, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkFriends(CollectionReference docuRef, Map<String, Object> docData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dr = db.collection("friends");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        dr.document(organizer).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        if (!document.getData().get("friends").toString().contains(userID)) {
                            checkOtherDirection(docuRef, docData);
                        } else {
                            docuRef.add(docData);

                            refresh();
                        }
                    } else {
                        checkOtherDirection(docuRef, docData);
                    }
                } else {
                    checkOtherDirection(docuRef, docData);
                }
            });
    }

    private void checkNumberOfParticipantsAdd(CollectionReference docuRef, Map<String, Object> docData) {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() < participate_maximum) {
                    db.collection("users").document(userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            if (document2.exists()) {
                                Map<String, Object> data = document2.getData();
                                String area_string = data.get("areas_of_interest").toString();
                                if (area_string.length() > 2) {
                                    String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                                    List<String> areas_array = new ArrayList<>();
                                    Collections.addAll(areas_array, area_string_array);
                                    String points_string = data.get("points_levels").toString();
                                    String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                                    List<Float> points_array = new ArrayList<>();
                                    for (String s : points_string_array) {
                                        points_array.add(Float.parseFloat(s));
                                    }
                                    if (areas_array.contains(event_type)) {
                                        if (minimum_level > 0) {
                                            if (points_array.get(areas_array.indexOf(event_type)) < minimum_level * 1000) {
                                                 Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (public_event) {
                                                    docuRef.add(docData);

                                                } else {
                                                    checkFriends(docuRef, docData);
                                                }
                                                refresh();
                                            }
                                        } else {
                                            if (public_event) {
                                                docuRef.add(docData);

                                            } else {
                                                checkFriends(docuRef, docData);
                                            }
                                            refresh();
                                        }
                                    } else {
                                        if (minimum_level > 0) {
                                             Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (public_event) {
                                                docuRef.add(docData);

                                            } else {
                                                checkFriends(docuRef, docData);
                                            }
                                            refresh();
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                     Toast.makeText(getApplicationContext(), R.string.too_many, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkNumberOfParticipantsRemove(QueryDocumentSnapshot docuRef) {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() > participate_minimum) {
                    db.collection("event_attending").document(docuRef.getId()).delete();

                    refresh();
                } else {
                     Toast.makeText(getApplicationContext(), R.string.not_enough, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void subscribe() {
            String eventID = preferences.getString("eventID", "");
            String userID = preferences.getString("userID", "");
            Map<String, Object> docData = new HashMap<>();
            docData.put("event", eventID);
            docData.put("user", userID);
            docData.put("notifications", switch_notify.isChecked());
            docData.put("rated", false);
            if (eventID.equals("")) {
                return;
            }
            if (userID.equals("")) {
                return;
            }
            CollectionReference docRef = db.collection("event_attending");
            docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        checkNumberOfParticipantsAdd(docRef, docData);
                    } else {
                        for (QueryDocumentSnapshot doc: task.getResult()) {
                            checkNumberOfParticipantsRemove(doc);
                        }
                    }
                }
            });
        }


    private void getOrganizerData(String name) {
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    myUsername.setText(data.get("username").toString());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Glide.with(context.getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView);
                        circleImageView.setOnClickListener(view -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID", name);
                            editor.apply();
                            Intent myIntent = new Intent(context, ViewProfileActivity.class);
                            startActivity(myIntent);
                        });
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    //Log.d(TAG, "No such document");
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void fillData() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }

        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();
                    eventName.setText(docData.get("event_name").toString());
                    eventType.setText(docData.get("event_type").toString());
                    eventType.setCompoundDrawablesRelativeWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(docData.get("event_type").toString()), 0, 0 ,0);
                    description.setText(docData.get("event_description").toString());
                    minimumlevel.setText(docData.get("minimum_level").toString());
                    if (docData.get("public").toString().equals("true")) {
                        switchpublic.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_open_24,0,0,0);
                        switchpublic.setText(R.string.public_event);
                    } else {
                        switchpublic.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_open_24,0,0,0);
                        switchpublic.setText(R.string.private_event);
                    }
                    if (docData.get("outdoors").toString().equals("true")) {
                        switchout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_nature_people_24,0,0,0);
                        switchout.setText(R.string.outdoor);
                    } else {
                        switchout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_attribution_24,0,0,0);
                        switchout.setText(R.string.indoor);
                    }
                    Float val1 = Float.parseFloat(docData.get("minimum_players").toString());
                    Float val2 = Float.parseFloat(docData.get("maximum_players").toString());
                    organizer = docData.get("organizer").toString();
                    event_type = docData.get("event_type").toString();
                    participate_minimum = val1;
                    participate_maximum = val2;
                    minimum_level = Float.parseFloat(docData.get("minimum_level").toString());
                    slider.setText(val1.toString() + "-" + val2.toString());
                    Timestamp start_timestamp = (Timestamp)(docData.get("datetime"));
                    Date start_date = start_timestamp.toDate();
                    cldr_start.setTime(start_date);
                    Timestamp end_timestamp = (Timestamp)(docData.get("ending"));
                    Date end_date = end_timestamp.toDate();
                    cldr_end.setTime(end_date);
                    textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                    textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                    textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                    textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));
                    public_event = docData.get("public").toString().equals("true");
                    GeoPoint location_point = (GeoPoint)(docData.get("location"));
                    latitude = location_point.getLatitude();
                    longitude = location_point.getLongitude();

                    chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                    String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + "\n"
                            + getString(R.string.longitude) + ": " + Double.toString(Math.round(longitude * 10000) / 10000.0);
                    location.setText(formattedLocation);
                    mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));

                    getOrganizerData(docData.get("organizer").toString());
                    CollectionReference docRef2 = db.collection("event_attending");
                    docRef2.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            if(task2.getResult().size() == 0) {
                                switch_notify.setChecked(true);
                            } else {
                                for (QueryDocumentSnapshot document2: task2.getResult()) {
                                    Map<String, Object> docData2 = document2.getData();
                                    switch_notify.setChecked(docData2.get("notifications").toString().equals("true"));
                                }
                            }
                        } else {
                            switch_notify.setChecked(true);
                        }
                    });
                } else {
                }
            }
        });
    }
}