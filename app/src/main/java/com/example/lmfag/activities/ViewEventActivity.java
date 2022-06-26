package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.example.lmfag.BuildConfig;
import com.example.lmfag.R;
import com.example.lmfag.utility.AlarmScheduler;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.LevelTransformation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewEventActivity extends MenuInterfaceActivity {
    private final Calendar cldr_start = Calendar.getInstance();
    private final Calendar cldr_end = Calendar.getInstance();
    private Context context;
    private MapView map;
    private IMapController mapController;
    private Marker chosenLocationMarker;
    private SwitchCompat switch_notify;
    private String event_type;
    private ImageView apply;
    private ImageView rate;
    private ImageView edit;
    private TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime, eventName, eventType, location, description, minimum_level_view, switch_public, switch_out, slider;
    private double longitude = 45.23;
    private double latitude = 45.36;
    private boolean public_event;
    private String organizerID;
    private Float participate_minimum, participate_maximum;
    private Float minimum_level;
    private CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        context = this;

        map = findViewById(R.id.map);
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);
        circleImageView = findViewById(R.id.profile_image);
        switch_notify = findViewById(R.id.switchNotifications);

        eventName = findViewById(R.id.textViewEventName);
        eventType = findViewById(R.id.textViewEventType);
        description = findViewById(R.id.textViewEvenDescription);
        minimum_level_view = findViewById(R.id.textViewMinimumLevel);
        switch_public = findViewById(R.id.textViewPublic);
        switch_out = findViewById(R.id.textViewOutdoor);
        location = findViewById(R.id.textViewChooseLocation);
        slider = findViewById(R.id.textViewNumberOfPlayers);
        apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> subscribe());
        ImageView participants_view = findViewById(R.id.imageViewParticipants);
        participants_view.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewParticipantsActivity.class);
            context.startActivity(myIntent);
        });
        switch_notify.setOnClickListener(view -> changeNotify());

        String me = preferences.getString("userID", "");
        String eventID = preferences.getString("eventID", "");
        edit = findViewById(R.id.imageViewEdit);
        edit.setOnClickListener(view -> {
            if (me.equals(organizerID)) {
                if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime()) || cldr_start.getTime().equals(Calendar.getInstance().getTime()) || cldr_end.getTime().equals(Calendar.getInstance().getTime())) {
                    Toast.makeText(getApplicationContext(), R.string.edit_finished, Toast.LENGTH_SHORT).show();
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
            if (Calendar.getInstance().getTime().before(cldr_end.getTime()) || Calendar.getInstance().getTime().equals(cldr_end.getTime())) {
                Toast.makeText(getApplicationContext(), R.string.rate_before_end, Toast.LENGTH_SHORT).show();
            } else {
                CollectionReference docRef = db.collection("event_attending");
                docRef.whereEqualTo("event", eventID).whereEqualTo("user", me).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0 && !me.equals(organizerID)) {
                            Toast.makeText(getApplicationContext(), R.string.not_participate_rate, Toast.LENGTH_SHORT).show();
                        } else {
                            boolean found = false;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (Objects.requireNonNull(doc.getData().get("rated")).toString().equals("true")) {
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
                if (task.getResult().size() != 0) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Map<String, Object> docData = doc.getData();
                        docData.remove("notifications");
                        docData.put("notifications", switch_notify.isChecked());
                        db.collection("event_attending")
                                .document(doc.getId())
                                .set(docData);
                        Toast.makeText(this, R.string.change_notify, Toast.LENGTH_SHORT).show();
                        if (switch_notify.isChecked()) {
                            Toast.makeText(this, R.string.notifications_on, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.notifications_off, Toast.LENGTH_SHORT).show();
                        }
                        AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
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
        chosenLocationMarker.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.map_marker));
        // Centering map based on current location

        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
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
        latitude = preferences.getFloat("newEventLatitude", (float) latitude);
        longitude = preferences.getFloat("newEventLongitude", (float) longitude);
        String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Math.round(latitude * 10000) / 10000.0 + "\n"
                + getString(R.string.longitude) + ": " + Math.round(longitude * 10000) / 10000.0;
        location.setText(formattedLocation);
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    private void refresh() {
        Intent myIntent = new Intent(context, ViewEventActivity.class);
        context.startActivity(myIntent);
        finish();
    }

    private void checkValidTime() {
        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
            Toast.makeText(getApplicationContext(), R.string.end_before_begin, Toast.LENGTH_SHORT).show();
            onBackPressed();
            finish();
        }
    }

    private void checkSubscribed() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (!(cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime()) || cldr_start.getTime().equals(Calendar.getInstance().getTime()) || cldr_end.getTime().equals(Calendar.getInstance().getTime()))) {
            apply.setVisibility(View.VISIBLE);
            switch_notify.setVisibility(View.VISIBLE);
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).whereEqualTo("attending", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    apply.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_person_add_24));
                } else {
                    apply.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_person_remove_24));
                }
            }
        });
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).whereEqualTo("rated", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() != 0) {
                    rate.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_star_24));
                } else {
                    rate.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_star_outline_24));
                }
            }
        });
    }

    private void checkOtherDirection() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        CollectionReference dr = db.collection("friends");
        dr.document(userID).get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                DocumentSnapshot document2 = task2.getResult();
                if (document2.exists()) {
                    if (!Objects.requireNonNull(Objects.requireNonNull(document2.getData()).get("friends")).toString().contains(userID)) {
                        Toast.makeText(getApplicationContext(), R.string.not_friend_organizer, Toast.LENGTH_SHORT).show();
                    } else {
                        addParticipant();
                        Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                        AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
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

    private void checkFriends() {
        CollectionReference dr = db.collection("friends");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        dr.document(organizerID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (!Objects.requireNonNull(Objects.requireNonNull(document.getData()).get("friends")).toString().contains(userID)) {
                        checkOtherDirection();
                    } else {
                        addParticipant();
                        Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                        AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                        refresh();
                    }
                } else {
                    checkOtherDirection();
                }
            } else {
                checkOtherDirection();
            }
        });
    }

    private void checkNumberOfParticipantsAdd() {
        double minimum_level_val = Double.parseDouble(minimum_level.toString());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).whereEqualTo("attending", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() < participate_maximum) {
                    db.collection("users").document(userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            if (document2.exists()) {
                                Map<String, Object> data = document2.getData();
                                String area_string = Objects.requireNonNull(Objects.requireNonNull(data).get("areas_of_interest")).toString();
                                if (area_string.length() > 2) {
                                    String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                                    List<String> areas_array = new ArrayList<>();
                                    Collections.addAll(areas_array, area_string_array);
                                    String points_string = Objects.requireNonNull(data.get("points_levels")).toString();
                                    String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                                    List<Float> points_array = new ArrayList<>();
                                    for (String s : points_string_array) {
                                        points_array.add(Float.parseFloat(s));
                                    }
                                    if (areas_array.contains(event_type)) {
                                        if (minimum_level_val > 0) {
                                            if (points_array.get(areas_array.indexOf(event_type)) < LevelTransformation.lower_bound((int)minimum_level_val)) {
                                                Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (public_event) {
                                                    addParticipant();
                                                    Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                                                    AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                                                    refresh();
                                                } else {
                                                    checkFriends();
                                                }
                                            }
                                        } else {
                                            if (public_event) {
                                                addParticipant();
                                                Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                                                AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                                                refresh();
                                            } else {
                                                checkFriends();
                                            }
                                        }
                                    } else {
                                        if (minimum_level_val > 0) {
                                            Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (public_event) {
                                                addParticipant();
                                                Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                                                AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                                                refresh();
                                            } else {
                                                checkFriends();
                                            }
                                        }
                                    }
                                } else {
                                    if (minimum_level_val > 0) {
                                        Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (public_event) {
                                            addParticipant();
                                            Toast.makeText(getApplicationContext(), R.string.attending_event, Toast.LENGTH_SHORT).show();
                                            AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                                            refresh();
                                        } else {
                                            checkFriends();
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

    private void checkNumberOfParticipantsRemove() {
        String eventID = preferences.getString("eventID", "");
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).whereEqualTo("attending", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > participate_minimum) {
                    Toast.makeText(getApplicationContext(), R.string.no_longer_attending, Toast.LENGTH_SHORT).show();
                    removeParticipant();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.not_enough, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkNumberOfParticipants() {
        String eventID = preferences.getString("eventID", "");
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).whereEqualTo("attending", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                TextView participant_number = findViewById(R.id.textViewNumberOfPlayersCurrent);
                Integer size = task.getResult().size();
                participant_number.setText(size + "");
            }
        });
    }

    private void subscribe() {
        String eventID = preferences.getString("eventID", "");
        CollectionReference docRef = db.collection("event_attending");
        String userID = preferences.getString("userID", "");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).whereEqualTo("attending", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    checkNumberOfParticipantsAdd();
                } else {
                    checkNumberOfParticipantsRemove();
                }
            }
        });
    }

    private void removeParticipant() {
        String eventID = preferences.getString("eventID", "");
        CollectionReference docRef = db.collection("event_attending");
        String userID = preferences.getString("userID", "");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (!userID.equals(organizerID)) {
                            docRef.document(document.getId()).delete();
                        } else {
                            Map<String, Object> map = document.getData();
                            map.put("attending", false);
                            map.put("notifications", switch_notify.isChecked());
                            docRef.document(document.getId()).set(map);
                        }
                    }
                }
            }
        });
    }

    private void addParticipant() {
        String eventID = preferences.getString("eventID", "");
        CollectionReference docRef = db.collection("event_attending");
        String userID = preferences.getString("userID", "");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> map = document.getData();
                        map.put("attending", true);
                        map.put("notifications", switch_notify.isChecked());
                        docRef.document(document.getId()).set(map);
                    }
                } else {
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("event", eventID);
                    docData.put("user", userID);
                    docData.put("attending", true);
                    docData.put("notifications", switch_notify.isChecked());
                    docData.put("rated", false);
                    docRef.add(docData);
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
                    String organizerUserName = Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString();
                    eventName.setText(organizerUserName + ": " + eventName.getText());
                    circleImageView.setOnClickListener(view -> {
                        editor.putString("friendID", name);
                        editor.apply();
                        Intent myIntent = new Intent(context, ViewProfileActivity.class);
                        startActivity(myIntent);
                    });
                    String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(context.getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView)).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    }
                } else {
                    editor.putString("eventID", "");
                    editor.apply();
                    onBackPressed();
                    finish();
                    //Log.d(TAG, "No such document");
                }
            } else {
                editor.putString("eventID", "");
                editor.apply();
                onBackPressed();
                finish();
            }
        });
    }

    private void checkEdit() {
        String me = preferences.getString("userID", "");
        if (me.equals(organizerID)) {
            if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime()) || cldr_start.getTime().equals(Calendar.getInstance().getTime()) || cldr_end.getTime().equals(Calendar.getInstance().getTime())) {
                edit.setVisibility(View.GONE);
            } else {
                edit.setVisibility(View.VISIBLE);
            }
        } else {
            edit.setVisibility(View.GONE);
        }
    }

    private void checkRate() {
        String eventID = preferences.getString("eventID", "");
        String me = preferences.getString("userID", "");
        if (Calendar.getInstance().getTime().before(cldr_end.getTime()) || Calendar.getInstance().getTime().equals(cldr_end.getTime())) {
            rate.setVisibility(View.GONE);
        } else {
            CollectionReference docRef2 = db.collection("event_attending");
            docRef2.whereEqualTo("event", eventID).whereEqualTo("user", me).whereEqualTo("rated", true).get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    if (task2.getResult().size() != 0) {
                        rate.setVisibility(View.GONE);
                    } else {
                        rate.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void fillData() {
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
        checkNumberOfParticipants();
        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();

                    organizerID = Objects.requireNonNull(docData.get("organizer")).toString();
                    eventName.setText(Objects.requireNonNull(Objects.requireNonNull(docData).get("event_name")).toString());
                    getOrganizerData(organizerID);
                    eventType.setText(EventTypeToDrawable.getEventTypeToTranslation(this, Objects.requireNonNull(Objects.requireNonNull(docData).get("event_type")).toString()));
                    eventType.setCompoundDrawablesRelativeWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(docData.get("event_type")).toString()), 0, 0, 0);
                    description.setText(Objects.requireNonNull(docData.get("event_description")).toString());
                    minimum_level_view.setText(Objects.requireNonNull(docData.get("minimum_level")).toString());
                    if (Objects.requireNonNull(docData.get("public")).toString().equals("true")) {
                        switch_public.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_open_24, 0, 0, 0);
                        switch_public.setText(R.string.public_event);
                    } else {
                        switch_public.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_open_24, 0, 0, 0);
                        switch_public.setText(R.string.private_event);
                    }
                    if (Objects.requireNonNull(docData.get("outdoors")).toString().equals("true")) {
                        switch_out.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_nature_people_24, 0, 0, 0);
                        switch_out.setText(R.string.outdoor);
                    } else {
                        switch_out.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_attribution_24, 0, 0, 0);
                        switch_out.setText(R.string.indoor);
                    }
                    Float val1 = Float.parseFloat(Objects.requireNonNull(docData.get("minimum_players")).toString());
                    Float val2 = Float.parseFloat(Objects.requireNonNull(docData.get("maximum_players")).toString());
                    System.out.println(Objects.requireNonNull(docData.get("organizer")));

                    event_type = Objects.requireNonNull(docData.get("event_type")).toString();
                    participate_minimum = val1;
                    participate_maximum = val2;
                    minimum_level = Float.parseFloat(Objects.requireNonNull(docData.get("minimum_level")).toString());
                    slider.setText(String.format(Locale.getDefault(), "%.0f - %.0f", val1, val2));
                    Timestamp start_timestamp = (Timestamp) (docData.get("datetime"));
                    Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                    cldr_start.setTime(start_date);
                    Timestamp end_timestamp = (Timestamp) (docData.get("ending"));
                    Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                    cldr_end.setTime(end_date);
                    checkValidTime();
                    checkSubscribed();
                    checkEdit();
                    checkRate();
                    textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                    textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                    textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                    textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));
                    public_event = Objects.requireNonNull(docData.get("public")).toString().equals("true");
                    GeoPoint location_point = (GeoPoint) (docData.get("location"));
                    latitude = Objects.requireNonNull(location_point).getLatitude();
                    longitude = location_point.getLongitude();

                    chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                    String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Math.round(latitude * 10000) / 10000.0 + "\n"
                            + getString(R.string.longitude) + ": " + Math.round(longitude * 10000) / 10000.0;
                    location.setText(formattedLocation);
                    mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));

                    CollectionReference docRef2 = db.collection("event_attending");
                    docRef2.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            if (task2.getResult().size() == 0) {
                                switch_notify.setChecked(true);
                            } else {
                                for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                    Map<String, Object> docData2 = document2.getData();
                                    switch_notify.setChecked(Objects.requireNonNull(docData2.get("notifications")).toString().equals("true"));
                                }
                            }
                        } else {
                            switch_notify.setChecked(true);
                        }
                    });
                } else {
                    editor.putString("eventID", "");
                    editor.apply();
                    onBackPressed();
                    finish();
                    //Log.d(TAG, "No such document");
                }
            } else {
                editor.putString("eventID", "");
                editor.apply();
                onBackPressed();
                finish();
            }
        });
    }
}