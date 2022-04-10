package com.example.lmfag.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lmfag.BuildConfig;
import com.example.lmfag.R;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
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

public class ViewEvent extends MenuInterface {
    private Context context = this;
    final Calendar cldr_start = Calendar.getInstance();
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    final Calendar cldr_end = Calendar.getInstance();
    private String event_type;
    private ImageView imageViewChooseStartDate, imageViewChooseStartTime, imageViewChooseEndDate, imageViewChooseEndTime, apply;
    private TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime;
    private double longitude = 45.23;
    private double latitude = 45.36;
    private String organizer;
    private boolean public_event;
    private Float participate_minimum, participate_maximum;
    private Float minimum_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        DrawerHelper.fillNavbarData(this);
        imageViewChooseStartDate = findViewById(R.id.imageViewChooseStartDate);
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);

        imageViewChooseStartTime = findViewById(R.id.imageViewChooseStartTime);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);

        imageViewChooseEndDate = findViewById(R.id.imageViewChooseEndDate);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);

        imageViewChooseEndTime = findViewById(R.id.imageViewChooseEndTime);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);

        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> {
            subscribe();
        });
        ImageView edit = findViewById(R.id.imageViewEdit);
        edit.setOnClickListener(view -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String me = preferences.getString("userID", "");
            if (me.equals(organizer)) {
                if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                    Snackbar.make(imageViewChooseEndTime, "Can't edit an event that finished.", Snackbar.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(context, ViewEvent.class);
                    startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(context, CreateEvent.class);
                    context.startActivity(myIntent);
                }
            } else {
                Snackbar.make(edit, R.string.organizer_edit, Snackbar.LENGTH_SHORT).show();
            }
        });


        ImageView imageViewRate = findViewById(R.id.imageViewRate);
        imageViewRate.setOnClickListener(view -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String eventID = preferences.getString("eventID", "");
                    String userID = preferences.getString("userID", "");

            if (Calendar.getInstance().getTime().before(cldr_end.getTime())) {
                Snackbar.make(imageViewChooseStartTime, "Events can't be rated before it ends.", Snackbar.LENGTH_SHORT).show();
            } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference docRef = db.collection("event_attending");
                    docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0 && !userID.equals(organizer)) {
                                Snackbar.make(imageViewChooseStartTime, "You can't rate an event if you don't participate or organize.", Snackbar.LENGTH_SHORT).show();
                            } else {
                                boolean found = false;
                                for (QueryDocumentSnapshot doc: task.getResult()) {
                                    if (doc.getData().get("rated").toString().equals("true")) {
                                        Snackbar.make(imageViewChooseStartTime, "You can't rate an event twice.", Snackbar.LENGTH_SHORT).show();
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    Intent myIntent = new Intent(context, RateEvent.class);
                                    context.startActivity(myIntent);
                                }
                            }
                        }
                    });
            }
        });
        firstMapSetup();
    }

    private void firstMapSetup() {
        // Loading map
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        map = findViewById(R.id.map);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        latitude = preferences.getFloat("newEventLatitude", (float)latitude);
        longitude = preferences.getFloat("newEventLongitude", (float)longitude);
        String formattedLocation = getString(R.string.location) + ": " + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + " "
                + getString(R.string.longitude) + ": " + Double.toString(Math.round(longitude * 10000) / 10000.0);
        TextView location = findViewById(R.id.textViewChooseLocation);
        location.setText(formattedLocation);
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    private void refresh() {
        Intent myIntent = new Intent(context, ViewEvent.class);
        context.startActivity(myIntent);
    }

    private void checkSubscribed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfile.class);
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
                ImageView rate = findViewById(R.id.imageViewRate);
                if (task.getResult().size() == 0) {
                    ImageView apply = findViewById(R.id.imageViewApply);
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
                    ImageView apply = findViewById(R.id.imageViewApply);
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
                            Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            docuRef.add(docData);
                            refresh();
                        }
                    } else {
                        Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
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
                                                Snackbar.make(switch_notify, R.string.level_low, Snackbar.LENGTH_SHORT).show();
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
                                            Snackbar.make(switch_notify, R.string.level_low, Snackbar.LENGTH_SHORT).show();
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
                    Snackbar.make(switch_notify, R.string.too_many, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkNumberOfParticipantsRemove(QueryDocumentSnapshot docuRef) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
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
                    Snackbar.make(switch_notify, "Not enough participants.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void subscribe() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String eventID = preferences.getString("eventID", "");
            String userID = preferences.getString("userID", "");
            SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    TextView myUsername = findViewById(R.id.textViewOrganizer);
                    myUsername.setText(data.get("username").toString());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                        findViewById(R.id.profile_image).setOnClickListener(view -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID", name);
                            editor.apply();
                            Intent myIntent = new Intent(context, ViewProfile.class);
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfile.class);
            startActivity(myIntent);
            return;
        }

        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();
                    TextView eventName = findViewById(R.id.textViewEventName);
                    TextView eventType = findViewById(R.id.textViewEventType);
                    TextView description = findViewById(R.id.textViewEvenDescription);
                    TextView minimumlevel = findViewById(R.id.textViewMinimumLevel);
                    TextView switchpublic = findViewById(R.id.textViewPublic);
                    TextView switcout = findViewById(R.id.textViewOutdoor);
                    TextView slider = findViewById(R.id.textViewNumberOfPlayers);
                    SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
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
                        switcout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_nature_people_24,0,0,0);
                        switcout.setText(R.string.outdoor);
                    } else {
                        switcout.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_attribution_24,0,0,0);
                        switcout.setText(R.string.indoor);
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
                    TextView location = findViewById(R.id.textViewChooseLocation);
                    latitude = location_point.getLatitude();
                    longitude = location_point.getLongitude();

                    chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                    String formattedLocation = getString(R.string.location) + ": " + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + " "
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