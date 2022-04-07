package com.example.lmfag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlinx.coroutines.SchedulerTaskKt;

public class CreateEvent extends AppCompatActivity {

    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private Marker chosenLocationMarker;
    final Calendar cldr_start = Calendar.getInstance();
    int hours_start = cldr_start.get(Calendar.HOUR_OF_DAY);
    int minutes_start = cldr_start.get(Calendar.MINUTE);
    int year_start = cldr_start.get(Calendar.YEAR);
    int month_start = cldr_start.get(Calendar.MONTH);
    int day_start = cldr_start.get(Calendar.DAY_OF_MONTH);
    final Calendar cldr_end = Calendar.getInstance();
    int hours_end = cldr_end.get(Calendar.HOUR_OF_DAY);
    int minutes_end = cldr_end.get(Calendar.MINUTE);
    int year_end = cldr_end.get(Calendar.YEAR);
    int month_end = cldr_end.get(Calendar.MONTH);
    int day_end = cldr_end.get(Calendar.DAY_OF_MONTH);
    double longitude = 45.23;
    double latitude = 45.36;
    Context context = this;
    private String selected_item;
    ImageView imageViewChooseStartDate, imageViewChooseStartTime, imageViewChooseEndDate, imageViewChooseEndTime, apply;
    TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime;
    Spinner sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        imageViewChooseStartDate = findViewById(R.id.imageViewChooseStartDate);
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);

        imageViewChooseStartTime = findViewById(R.id.imageViewChooseStartTime);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);

        imageViewChooseEndDate = findViewById(R.id.imageViewChooseEndDate);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);

        imageViewChooseEndTime = findViewById(R.id.imageViewChooseEndTime);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);

        apply = findViewById(R.id.imageViewApply);
        sp = findViewById(R.id.sp);
        fillSpinner();
        setDate();
        setTime();
        fillData();
        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> fetchDataFromUI());
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                selected_item = adapter.getItemAtPosition(i).toString();
                ImageView iv = findViewById(R.id.imageViewEventType);
                iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selected_item)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        ImageView location_choose = findViewById(R.id.imageViewChooseLocation);
        location_choose.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ChooseLocation.class);
            startActivity(myIntent);
        });
        ImageView close = findViewById(R.id.imageViewDiscard);
        close.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewEvent.class);
            startActivity(myIntent);
        });
        firstMapSetup();
    }
    void firstMapSetup() {
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
    void setDate() {
        imageViewChooseStartDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        day_start = sDay;
                        month_start = sMonth;
                        year_start = sYear;
                        cldr_start.set(year_start, month_start, day_start, hours_start, minutes_start);
                        textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                        if (cldr_start.getTime().before(Calendar.getInstance().getTime())) {
                            Snackbar.make(imageViewChooseStartDate, "Event can't begin in the past.", Snackbar.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                            Snackbar.make(imageViewChooseStartDate, "Event can't end before beginning.", Snackbar.LENGTH_SHORT).show();
                        }
                    }, year_start, month_start, day_start);
            picker.show();
        });
        imageViewChooseEndDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        day_end = sDay;
                        month_end = sMonth;
                        year_end = sYear;
                        cldr_end.set(year_end, month_end, day_end, hours_end, minutes_end);
                        textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                        if (cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                            Snackbar.make(imageViewChooseStartDate, "Event can't end in the past.", Snackbar.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                            Snackbar.make(imageViewChooseEndDate, "Event can't end before beginning.", Snackbar.LENGTH_SHORT).show();
                        }
                    }, year_end, month_end, day_end);
            picker.show();
        });
    }

    void setTime() {
        imageViewChooseStartTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        hours_start = sHour;
                        minutes_start = sMinute;
                        cldr_start.set(year_start, month_start, day_start, hours_start, minutes_start);
                        textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                        if (cldr_start.getTime().before(Calendar.getInstance().getTime())) {
                            Snackbar.make(imageViewChooseStartDate, "Event can't begin in the past.", Snackbar.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                            Snackbar.make(imageViewChooseStartTime, "Event can't end before beginning.", Snackbar.LENGTH_SHORT).show();
                        }
                    }, hours_start, minutes_start, true);
            picker.show();
        });
        imageViewChooseEndTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        hours_end = sHour;
                        minutes_end = sMinute;
                        cldr_end.set(year_end, month_end, day_end, hours_end, minutes_end);
                        textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));
                        if (cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                            Snackbar.make(imageViewChooseEndDate, "Event can't end in the past.", Snackbar.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                            Snackbar.make(imageViewChooseStartTime, "Event can't end before beginning.", Snackbar.LENGTH_SHORT).show();
                        }
                    }, hours_end, minutes_end, true);
            picker.show();
        });
    }
    void fillSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    void writeAttending() {
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
                    docRef.add(docData);
                } else {
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        docRef.document(doc.getId()).set(docData);
                    }
                }
            }
        });
    }
    void writeDB(Map<String, Object> docData, boolean attending) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .add(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    Snackbar.make(apply, R.string.created_event, Snackbar.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("eventID", aVoid.getId());
                    editor.apply();
                    if (attending) {
                        writeAttending();
                    }
                    editor.apply();
                    Intent myIntent = new Intent(context, MyProfile.class);
                    startActivity(myIntent);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }
    void setDB(Map<String, Object> docData, boolean attending) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            writeDB(docData, attending);
        } else {
            db.collection("events")
                    .document(eventID)
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {
                        //Log.d(TAG, "DocumentSnapshot successfully written!");
                        Snackbar.make(apply, R.string.updated_event, Snackbar.LENGTH_SHORT).show();
                        if (attending) {
                            writeAttending();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error writing document", e);
                    });
        }

    }
    void fetchDataFromUI() {
        Map<String, Object> docData = new HashMap<>();
        EditText eventName = findViewById(R.id.editTextEventName);
        EditText description = findViewById(R.id.editTextEventDescription);
        EditText minimum_level = findViewById(R.id.editTextMinimumLevel);
        SwitchCompat switch_public = findViewById(R.id.switchPublic);
        SwitchCompat switch_out = findViewById(R.id.switchOutdoor);
        SwitchCompat switch_organizer = findViewById(R.id.switchOrganizerPlaying);
        RangeSlider slider = findViewById(R.id.range_slider);
        docData.put("event_name", eventName.getText().toString());
        docData.put("event_type", selected_item);
        docData.put("event_description", description.getText().toString());
        docData.put("minimum_level", Integer.parseInt(minimum_level.getText().toString()));
        docData.put("public", switch_public.isChecked());
        docData.put("outdoors", switch_out.isChecked());
        docData.put("minimum_players", slider.getValues().get(0));
        docData.put("maximum_players", slider.getValues().get(1));
        docData.put("datetime", new Timestamp(cldr_start.getTime()));
        docData.put("ending", new Timestamp(cldr_end.getTime()));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        docData.put("organizer", userID);
        docData.put("location", new GeoPoint(latitude, longitude));
        docData.put("geo_hash", GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude)));
        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
            Snackbar.make(imageViewChooseStartTime, "Event can't end before beginning.", Snackbar.LENGTH_SHORT).show();
        } else {
            setDB(docData, switch_organizer.isChecked());
        }
    }
    void fillData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        if (!eventID.equals("")) {
            DocumentReference docRef = db.collection("events").document(eventID);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> docData = document.getData();
                        TextView eventName = findViewById(R.id.editTextEventName);
                        TextView description = findViewById(R.id.editTextEventDescription);
                        TextView minimum_level = findViewById(R.id.editTextMinimumLevel);
                        SwitchCompat switch_public = findViewById(R.id.switchPublic);
                        SwitchCompat switch_out = findViewById(R.id.switchOutdoor);
                        SwitchCompat switch_organizer = findViewById(R.id.switchOrganizerPlaying);
                        SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
                        RangeSlider slider = findViewById(R.id.range_slider);
                        TextView location = findViewById(R.id.textViewChooseLocation);
                        eventName.setText(docData.get("event_name").toString());
                        sp.setSelection(((ArrayAdapter)sp.getAdapter()).getPosition(docData.get("event_type").toString()));
                        description.setText(docData.get("event_description").toString());
                        minimum_level.setText(docData.get("minimum_level").toString());
                        switch_public.setChecked(docData.get("public").toString().equals("true"));
                        switch_out.setChecked(docData.get("outdoors").toString().equals("true"));
                        Float val1 = Float.parseFloat(docData.get("minimum_players").toString());
                        Float val2 = Float.parseFloat(docData.get("maximum_players").toString());
                        slider.setValues(val1, val2);
                        Timestamp start_timestamp = (Timestamp)(docData.get("datetime"));
                        Date start_date = start_timestamp.toDate();
                        cldr_start.setTime(start_date);
                        Timestamp end_timestamp = (Timestamp)(docData.get("ending"));
                        Date end_date = end_timestamp.toDate();
                        cldr_end.setTime(end_date);
                        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                            Snackbar.make(sp, "Can't edit an event that finished.", Snackbar.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(context, ViewEvent.class);
                            startActivity(myIntent);
                        }
                        textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                        textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                        textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                        textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));

                        GeoPoint location_point = (GeoPoint)(docData.get("location"));
                        latitude = location_point.getLatitude();
                        longitude = location_point.getLongitude();

                        String formattedLocation = getString(R.string.location) + ": " + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + " "
                                + getString(R.string.longitude) + ": " + Double.toString(Math.round(longitude * 10000) / 10000.0);
                        location.setText(formattedLocation);
                        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));

                        String userID = preferences.getString("userID", "");
                        if (userID.equals("")) {
                            Intent myIntent = new Intent(context, MainActivity.class);
                            startActivity(myIntent);
                            return;
                        }
                        CollectionReference docRef2 = db.collection("event_attending");
                        docRef2.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                if(task2.getResult().size() == 0) {
                                    switch_organizer.setChecked(false);
                                    switch_notify.setChecked(false);
                                } else {
                                    for (QueryDocumentSnapshot document2: task2.getResult()) {
                                        Map<String, Object> docData2 = document2.getData();
                                        switch_organizer.setChecked(true);
                                        switch_notify.setChecked(docData2.get("notifications").toString().equals("true"));
                                    }
                                }
                            } else {
                                switch_organizer.setChecked(false);
                                switch_notify.setChecked(false);
                            }
                        });
                    }
                }
            });
        }
    }
}