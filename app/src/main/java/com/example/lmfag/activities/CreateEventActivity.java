package com.example.lmfag.activities;


import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.BuildConfig;
import com.example.lmfag.R;
import com.example.lmfag.utility.AlarmScheduler;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.adapters.CustomAdapterEventTypeAdd;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateEventActivity extends MenuInterfaceActivity {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private FirebaseFirestore db;
    private TextView eventName;
    private TextView description;
    private NumberPicker minimum_level;
    private SwitchCompat switch_public;
    private SwitchCompat switch_out;
    private SwitchCompat switch_organizer;
    private SwitchCompat switch_notify;
    private RangeSlider slider;
    private TextView location;
    private Marker chosenLocationMarker;
    private final Calendar cldr_start = Calendar.getInstance();
    private final Calendar cldr_end = Calendar.getInstance();
    private double longitude = 45.23;
    private double latitude = 45.36;
    private Context context = this;
    private String selected_item;
    private ImageView imageViewChooseStartDate, imageViewChooseStartTime, imageViewChooseEndDate, imageViewChooseEndTime, apply;
    private TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime;
    //Spinner sp;
    private TextView sp;
    private List<String> all_areas;
    private LinearLayout openableCard;
    private ImageView closeCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        db = FirebaseFirestore.getInstance();

         
        eventName = findViewById(R.id.editTextEventName);
        description = findViewById(R.id.editTextEventDescription);
        minimum_level = findViewById(R.id.editTextMinimumLevel);
        minimum_level.setMinValue(0);
        minimum_level.setMaxValue(100000);
        minimum_level.setWrapSelectorWheel(true);
        switch_public = findViewById(R.id.switchPublic);
        switch_out = findViewById(R.id.switchOutdoor);
        switch_organizer = findViewById(R.id.switchOrganizerPlaying);
        switch_notify = findViewById(R.id.switchNotifications);
        slider = findViewById(R.id.range_slider);
        location = findViewById(R.id.textViewChooseLocation);
        imageViewChooseStartDate = findViewById(R.id.imageViewChooseStartDate);
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);

        imageViewChooseStartTime = findViewById(R.id.imageViewChooseStartTime);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);

        imageViewChooseEndDate = findViewById(R.id.imageViewChooseEndDate);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);

        imageViewChooseEndTime = findViewById(R.id.imageViewChooseEndTime);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);


        map = findViewById(R.id.map);

        apply = findViewById(R.id.imageViewApply);
        sp = findViewById(R.id.sp);
        all_areas = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.event_types)));

        RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
        CustomAdapterEventTypeAdd customAdapterEventTypeAdd = new CustomAdapterEventTypeAdd(all_areas, this);
        recyclerViewAreasOfInterestNew.setAdapter(customAdapterEventTypeAdd);

        //fillSpinner();
        setDate();
        setTime();
        fillData();
        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> fetchDataFromUI());
        /*sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                selected_item = adapter.getItemAtPosition(i).toString();
                ImageView iv = findViewById(R.id.imageViewEventType);
                iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selected_item)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });*/
        closeCard = findViewById(R.id.closeCard);
        openableCard = findViewById(R.id.openableCard);
        sp.setOnClickListener(view -> {
            openableCard.setVisibility(View.VISIBLE);
        });
        closeCard.setOnClickListener(view -> {
            openableCard.setVisibility(View.GONE);
        });
        ImageView location_choose = findViewById(R.id.imageViewChooseLocation);
        location_choose.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ChooseLocationActivity.class);
            startActivity(myIntent);
        });
        ImageView close = findViewById(R.id.imageViewDiscard);
        close.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            startActivity(myIntent);
        });
        firstMapSetup();
    }

    public void selectAreaOfInterest(String selected_item) {
        this.selected_item = selected_item;
        sp.setText(selected_item);
        ImageView iv = findViewById(R.id.imageViewEventType);
        iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selected_item)));

        openableCard.setVisibility(View.GONE);
    }
    void firstMapSetup() {
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


        switch_organizer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switch_notify.setVisibility(View.VISIBLE);
                } else {
                    switch_notify.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        latitude = preferences.getFloat("newEventLatitude", (float) latitude);
        longitude = preferences.getFloat("newEventLongitude", (float) longitude);
        String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + "\n"
                + getString(R.string.longitude) + ": " + Double.toString(Math.round(longitude * 10000) / 10000.0);

        location.setText(formattedLocation);
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    void setDate() {
        imageViewChooseStartDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        cldr_start.set(sYear, sMonth, sDay, cldr_start.get(HOUR), cldr_start.get(MINUTE));
                        textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                        if (cldr_start.getTime().before(Calendar.getInstance().getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.begin_past, Toast.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_before_beginning, Toast.LENGTH_SHORT).show();
                        }
                    }, cldr_start.get(YEAR), cldr_start.get(MONTH), cldr_start.get(DAY_OF_MONTH));
            picker.show();
        });
        imageViewChooseEndDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        cldr_end.set(sYear, sMonth, sDay, cldr_end.get(HOUR), cldr_end.get(MINUTE));
                        textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                        if (cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_past, Toast.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_before_beginning, Toast.LENGTH_SHORT).show();
                        }
                    }, cldr_end.get(YEAR), cldr_end.get(MONTH), cldr_end.get(DAY_OF_MONTH));
            picker.show();
        });
    }

    void setTime() {
        imageViewChooseStartTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        cldr_start.set(cldr_start.get(YEAR), cldr_start.get(MONTH), cldr_start.get(DAY_OF_MONTH), sHour, sMinute);
                        textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                        if (cldr_start.getTime().before(Calendar.getInstance().getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.begin_past, Toast.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_before_beginning, Toast.LENGTH_SHORT).show();
                        }
                    }, cldr_start.get(HOUR), cldr_start.get(MINUTE), true);
            picker.show();
        });
        imageViewChooseEndTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        cldr_end.set(cldr_start.get(YEAR), cldr_start.get(MONTH), cldr_start.get(DAY_OF_MONTH), sHour, sMinute);
                        textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));
                        if (cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_in_past, Toast.LENGTH_SHORT).show();
                        }
                        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                             Toast.makeText(getApplicationContext(), R.string.end_before_begin, Toast.LENGTH_SHORT).show();
                        }
                    }, cldr_end.get(HOUR), cldr_end.get(MINUTE), true);
            picker.show();
        });
    }

   /* void fillSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }*/

    void writeAttending() {
        Double minimum_level_val = Double.parseDouble(String.valueOf(minimum_level.getValue()));
        CollectionReference docuRef = db.collection("event_attending");

        String userID = preferences.getString("userID", "");

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
                        if (areas_array.contains(selected_item)) {
                            if (minimum_level_val > 0) {
                                if (points_array.get(areas_array.indexOf(selected_item)) < minimum_level_val * 1000) {
                                     Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                } else {
                                    writeAttendingToDB();
                                }
                            } else {
                                writeAttendingToDB();
                            }
                        } else {
                            if (minimum_level_val > 0) {
                                 Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                            } else {
                                writeAttendingToDB();
                            }
                        }
                    }
                }
            }
        });

    }

    void writeAttendingToDB() {
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
                if (task.getResult().size() == 0) {
                    docRef.add(docData);
                    AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                } else {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        docRef.document(doc.getId()).set(docData);
                        AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                    }
                }
            }
        });
    }

    void writeDB(Map<String, Object> docData, boolean attending) {
        db.collection("events")
                .add(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                     Toast.makeText(getApplicationContext(), R.string.created_event, Toast.LENGTH_SHORT).show();
                    editor.putString("eventID", aVoid.getId());
                    editor.apply();
                    if (attending) {
                        writeAttending();
                    }
                    editor.apply();
                    Intent myIntent = new Intent(context, MyProfileActivity.class);
                    startActivity(myIntent);
                })
                .addOnFailureListener(e -> {
                     Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }

    void setDB(Map<String, Object> docData, boolean attending) {


        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            writeDB(docData, attending);
        } else {
            db.collection("events")
                    .document(eventID)
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {
                        //Log.d(TAG, "DocumentSnapshot successfully written!");
                         Toast.makeText(getApplicationContext(), R.string.updated_event, Toast.LENGTH_SHORT).show();
                        if (attending) {
                            writeAttending();
                        }
                    })
                    .addOnFailureListener(e -> {
                         Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error writing document", e);
                    });
        }

    }

    void fetchDataFromUI() {
        Map<String, Object> docData = new HashMap<>();

        docData.put("event_name", eventName.getText().toString());
        docData.put("event_type", sp.getText().toString());
        docData.put("event_description", description.getText().toString());
        docData.put("minimum_level", Integer.parseInt(String.valueOf(minimum_level.getValue())));
        docData.put("public", switch_public.isChecked());
        docData.put("outdoors", switch_out.isChecked());
        docData.put("minimum_players", slider.getValues().get(0));
        docData.put("maximum_players", slider.getValues().get(1));
        docData.put("datetime", new Timestamp(cldr_start.getTime()));
        docData.put("ending", new Timestamp(cldr_end.getTime()));

        String userID = preferences.getString("userID", "");
        docData.put("organizer", userID);
        docData.put("location", new GeoPoint(latitude, longitude));
        docData.put("geo_hash", GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude)));
        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
             Toast.makeText(getApplicationContext(), R.string.end_before_beginning, Toast.LENGTH_SHORT).show();
        } else {
            setDB(docData, switch_organizer.isChecked());
        }
    }

    void deleteEvent(String docid) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:


                        db.collection("events").document(docid).delete();
                        db.collection("event_attending").whereEqualTo("event", docid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            db.collection("event_attending").document(document.getId()).delete();
                                        }
                                    }
                                }
                            }
                        });
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("selectedTab", 3);
                        editor.apply();
                        Intent myIntent = new Intent(context, MyProfileActivity.class);
                        context.startActivity(myIntent);

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_event).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();

        /*db.collection("events").document(docid).delete();
        db.collection("event_attending").whereEqualTo("event", docid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("event_attending").document(document.getId()).delete();
                        }
                    }
                }
            }
        });
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selectedTab", 3);
        editor.putString("eventID", "");
        editor.apply();
        Intent myIntent = new Intent(context, MyProfileActivity.class);
        context.startActivity(myIntent);*/
    }
    public void checkIfAbleToEdit(String organizer) {
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
    }
    void fillData() {

        String eventID = preferences.getString("eventID", "");
        ImageView imageViewDelete = findViewById(R.id.imageViewDelete);
        LinearLayout imageViewDeleteLayout = findViewById(R.id.imageViewDeleteLayout);
        if (eventID.equals("")) {

        } else {
            imageViewDeleteLayout.setVisibility(View.VISIBLE);
            imageViewDelete.setOnClickListener(view -> {
                deleteEvent(eventID);
            });
                DocumentReference docRef = db.collection("events").document(eventID);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> docData = document.getData();
                            checkIfAbleToEdit(docData.get("organizer").toString());
                            eventName.setText(docData.get("event_name").toString());
                            //sp.setSelection(((ArrayAdapter) sp.getAdapter()).getPosition(docData.get("event_type").toString()));

                            selected_item = docData.get("event_type").toString();
                            sp.setText(docData.get("event_type").toString());
                            ImageView iv = findViewById(R.id.imageViewEventType);
                            iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selected_item)));

                            description.setText(docData.get("event_description").toString());
                            minimum_level.setValue(Integer.parseInt(docData.get("minimum_level").toString()));
                            switch_public.setChecked(docData.get("public").toString().equals("true"));
                            switch_out.setChecked(docData.get("outdoors").toString().equals("true"));
                            Float val1 = Float.parseFloat(docData.get("minimum_players").toString());
                            Float val2 = Float.parseFloat(docData.get("maximum_players").toString());
                            slider.setValues(val1, val2);
                            Timestamp start_timestamp = (Timestamp) (docData.get("datetime"));
                            Date start_date = start_timestamp.toDate();
                            cldr_start.setTime(start_date);
                            Timestamp end_timestamp = (Timestamp) (docData.get("ending"));
                            Date end_date = end_timestamp.toDate();
                            cldr_end.setTime(end_date);
                            textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                            textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                            textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                            textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));

                            if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime())) {
                                 Toast.makeText(getApplicationContext(), R.string.cant_edit_finished, Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }

                            GeoPoint location_point = (GeoPoint) (docData.get("location"));
                            latitude = location_point.getLatitude();
                            longitude = location_point.getLongitude();

                            String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Double.toString(Math.round(latitude * 10000) / 10000.0) + "\n"
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
                                    if (task2.getResult().size() == 0) {
                                        switch_organizer.setChecked(false);
                                        switch_notify.setChecked(false);
                                    } else {
                                        for (QueryDocumentSnapshot document2 : task2.getResult()) {
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