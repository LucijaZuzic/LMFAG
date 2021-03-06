package com.example.lmfag.activities;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.BuildConfig;
import com.example.lmfag.R;
import com.example.lmfag.utility.AlarmScheduler;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.LevelTransformation;
import com.example.lmfag.utility.adapters.CustomAdapterEventTypeAdd;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
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
import java.util.Objects;

public class CreateEventActivity extends MenuInterfaceActivity {
    private final Calendar cldr_start = Calendar.getInstance();
    private final Calendar cldr_end = Calendar.getInstance();
    private MapView map;
    private IMapController mapController;
    private TextView eventName;
    private TextView description;
    private NumberPicker minimum_level, slider_min, slider_max;
    private SwitchCompat switch_public;
    private SwitchCompat switch_out;
    private SwitchCompat switch_organizer;
    private SwitchCompat switch_notify;
    private TextView location;
    private Marker chosenLocationMarker;
    private double longitude;
    private double latitude;
    private Context context;
    private String selected_item;
    private ImageView imageViewChooseStartDate;
    private ImageView imageViewChooseStartTime;
    private ImageView imageViewChooseEndDate;
    private ImageView imageViewChooseEndTime;
    private ImageView imageViewEventType;
    private ImageView imageViewDelete;
    private TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime;
    private TextView sp;
    private LinearLayout openableCard;
    private LinearLayout imageViewDeleteLayout;

    @Override
    public void onBackPressed() {
        String userID = preferences.getString("userID", "");
        if (userID.equals("")) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, MyProfileActivity.class));
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        context = this;
        eventName = findViewById(R.id.editTextEventName);
        description = findViewById(R.id.editTextEventDescription);
        minimum_level = findViewById(R.id.editTextMinimumLevel);
        minimum_level.setMinValue(0);
        minimum_level.setMaxValue(20);
        minimum_level.setWrapSelectorWheel(true);
        switch_public = findViewById(R.id.switchPublic);
        switch_out = findViewById(R.id.switchOutdoor);
        switch_organizer = findViewById(R.id.switchOrganizerPlaying);
        switch_notify = findViewById(R.id.switchNotifications);
        slider_min = findViewById(R.id.pickerMinimumPlayers);
        slider_max = findViewById(R.id.pickerMaximumPlayers);
        slider_max.setMaxValue(1000);
        slider_min.setMaxValue(1000);
        slider_max.setMinValue(2);
        slider_min.setMinValue(2);
        slider_max.setOnValueChangedListener((v1, v2, v3) -> fixMax());
        slider_min.setOnValueChangedListener((v1, v2, v3) -> fixMin());
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
        sp = findViewById(R.id.sp);
        imageViewDelete = findViewById(R.id.imageViewDelete);
        imageViewDeleteLayout = findViewById(R.id.imageViewDeleteLayout);
        openableCard = findViewById(R.id.openableCard);
        imageViewEventType = findViewById(R.id.imageViewEventType);
        ImageView closeCard = findViewById(R.id.closeCard);
        ImageView close = findViewById(R.id.imageViewDiscard);
        List<String> all_areas = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
        RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
        CustomAdapterEventTypeAdd customAdapterEventTypeAdd = new CustomAdapterEventTypeAdd(all_areas, this);
        recyclerViewAreasOfInterestNew.setAdapter(customAdapterEventTypeAdd);
        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> fetchDataFromUI());
        sp.setOnClickListener(view -> openableCard.setVisibility(View.VISIBLE));
        closeCard.setOnClickListener(view -> openableCard.setVisibility(View.GONE));
        close.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });
        setDate();
        setTime();
        fillData();
        firstMapSetup();
    }

    public void fixMin() {
        int minimum_pl = Integer.parseInt(String.valueOf(slider_min.getValue()));
        int maximum_pl = Integer.parseInt(String.valueOf(slider_max.getValue()));
        if (minimum_pl > maximum_pl) {
            slider_min.setValue(maximum_pl);
        }
    }

    public void fixMax() {
        int minimum_pl = Integer.parseInt(String.valueOf(slider_min.getValue()));
        int maximum_pl = Integer.parseInt(String.valueOf(slider_max.getValue()));
        if (minimum_pl > maximum_pl) {
            slider_max.setValue(minimum_pl);
        }
    }

    public void selectAreaOfInterest(String selected_item) {
        this.selected_item = selected_item;
        sp.setText(EventTypeToDrawable.getEventTypeToTranslation(this, selected_item));
        imageViewEventType.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), EventTypeToDrawable.getEventTypeToDrawable(selected_item)));
        openableCard.setVisibility(View.GONE);
    }

    void firstMapSetup() {
        // Loading map
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(false);
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
        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(org.osmdroid.util.GeoPoint p) {
                Intent myIntent = new Intent(context, ChooseLocationActivity.class);
                startActivity(myIntent);
                return false;
            }

            @Override
            public boolean longPressHelper(org.osmdroid.util.GeoPoint p) {
                return false;
            }
        }));
        mapController.setZoom(17.0);
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));

    }

    @Override
    protected void onResume() {
        super.onResume();
        latitude = preferences.getFloat("newEventLatitude", (float) latitude);
        longitude = preferences.getFloat("newEventLongitude", (float) longitude);
        String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Math.round(latitude * 10000) / 10000.0 + "\n"
                + getString(R.string.longitude) + ": " + Math.round(longitude * 10000) / 10000.0;
        location.setText(formattedLocation);
        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
    }

    private boolean checkCorrectTime() {
        boolean correct = true;
        if (cldr_end.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().equals(Calendar.getInstance().getTime())) {
            Toast.makeText(getApplicationContext(), R.string.end_past, Toast.LENGTH_SHORT).show();
            correct = false;
        }
        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_start.getTime().equals(Calendar.getInstance().getTime())) {
            Toast.makeText(getApplicationContext(), R.string.begin_past, Toast.LENGTH_SHORT).show();
            correct = false;
        }
        if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
            Toast.makeText(getApplicationContext(), R.string.end_before_begin, Toast.LENGTH_SHORT).show();
            correct = false;
        }
        return correct;
    }

    private void setDate() {
        imageViewChooseStartDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        cldr_start.set(sYear, sMonth, sDay, cldr_start.get(HOUR_OF_DAY), cldr_start.get(MINUTE));
                        textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));

                    }, cldr_start.get(YEAR), cldr_start.get(MONTH), cldr_start.get(DAY_OF_MONTH));
            picker.show();
        });
        imageViewChooseEndDate.setOnClickListener(v -> {
            // time picker dialog
            DatePickerDialog picker = new DatePickerDialog(context,
                    (dp, sYear, sMonth, sDay) -> {
                        cldr_end.set(sYear, sMonth, sDay, cldr_end.get(HOUR_OF_DAY), cldr_end.get(MINUTE));
                        textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));

                    }, cldr_end.get(YEAR), cldr_end.get(MONTH), cldr_end.get(DAY_OF_MONTH));
            picker.show();
        });
    }

    private void setTime() {
        imageViewChooseStartTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        cldr_start.set(cldr_start.get(YEAR), cldr_start.get(MONTH), cldr_start.get(DAY_OF_MONTH), sHour, sMinute);
                        textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));

                    }, cldr_start.get(HOUR_OF_DAY), cldr_start.get(MINUTE), true);
            picker.show();
        });
        imageViewChooseEndTime.setOnClickListener(v -> {
            // time picker dialog
            TimePickerDialog picker = new TimePickerDialog(context,
                    (tp, sHour, sMinute) -> {
                        cldr_end.set(cldr_end.get(YEAR), cldr_end.get(MONTH), cldr_end.get(DAY_OF_MONTH), sHour, sMinute);
                        textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));

                    }, cldr_end.get(HOUR_OF_DAY), cldr_end.get(MINUTE), true);
            picker.show();
        });
    }

    private void checkAbleToAttend() {
        double minimum_level_val = Double.parseDouble(String.valueOf(minimum_level.getValue()));
        String userID = preferences.getString("userID", "");
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
                        if (areas_array.contains(selected_item)) {
                            if (minimum_level_val > 0) {
                                if (points_array.get(areas_array.indexOf(selected_item)) < LevelTransformation.lower_bound((int) minimum_level_val)) {
                                    Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                    writeAttendingToDB(false);
                                } else {
                                    writeAttendingToDB(true);
                                }
                            } else {
                                writeAttendingToDB(true);
                            }
                        } else {
                            if (minimum_level_val > 0) {
                                Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                                writeAttendingToDB(false);
                            } else {
                                writeAttendingToDB(true);
                            }
                        }
                    } else {
                        if (minimum_level_val > 0) {
                            Toast.makeText(getApplicationContext(), R.string.level_low, Toast.LENGTH_SHORT).show();
                            writeAttendingToDB(false);
                        } else {
                            writeAttendingToDB(true);
                        }
                    }
                }
            }
        });
    }

    void writeAttendingToDB(boolean able_to_attend) {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        Map<String, Object> docData = new HashMap<>();
        docData.put("event", eventID);
        docData.put("user", userID);
        docData.put("attending", switch_organizer.isChecked() && able_to_attend);
        docData.put("notifications", switch_notify.isChecked());
        docData.put("rated", false);
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        docRef.document(doc.getId()).delete();
                    }
                }
            }
        });
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    docRef.add(docData);
                } else {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        docRef.document(doc.getId()).set(docData);
                    }
                }
                if (switch_organizer.isChecked() && able_to_attend) {
                    Toast.makeText(this, R.string.attending_event, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.no_longer_attending, Toast.LENGTH_SHORT).show();
                }
                if (switch_notify.isChecked()) {
                    Toast.makeText(this, R.string.notifications_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.notifications_off, Toast.LENGTH_SHORT).show();
                }
                AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                onBackPressed();
            }
        });
    }


    void setDB(Map<String, Object> docData) {
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            db.collection("events")
                    .add(docData)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(getApplicationContext(), R.string.created_event, Toast.LENGTH_SHORT).show();
                        editor.putString("eventID", aVoid.getId());
                        editor.apply();
                        checkAbleToAttend();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();

                    });
        } else {
            db.collection("events")
                    .document(eventID)
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(getApplicationContext(), R.string.updated_event, Toast.LENGTH_SHORT).show();
                        checkAbleToAttend();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();

                    });
        }
    }

    void fetchDataFromUI() {
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            continueEdit();
            return;
        }
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    continueEdit();
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.erased_subscriptions).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    void continueEdit() {
        boolean correct = true;
        if (!Arrays.asList(getResources().getStringArray(R.array.event_types)).contains(selected_item)) {
            Toast.makeText(getApplicationContext(), R.string.please_choose_an_event_type, Toast.LENGTH_SHORT).show();
            correct = false;
        }
        if (!checkCorrectTime()) {
            correct = false;
        }
        if (!correct) {
            return;
        }
        String userID = preferences.getString("userID", "");
        Map<String, Object> docData = new HashMap<>();
        docData.put("event_name", eventName.getText().toString());
        docData.put("event_type", selected_item);
        docData.put("event_description", description.getText().toString());
        docData.put("minimum_level", Integer.parseInt(String.valueOf(minimum_level.getValue())));
        docData.put("public", switch_public.isChecked());
        docData.put("outdoors", switch_out.isChecked());
        docData.put("minimum_players", Integer.parseInt(String.valueOf(slider_min.getValue())));
        docData.put("maximum_players", Integer.parseInt(String.valueOf(slider_max.getValue())));
        docData.put("datetime", new Timestamp(cldr_start.getTime()));
        docData.put("ending", new Timestamp(cldr_end.getTime()));
        docData.put("organizer", userID);
        docData.put("location", new GeoPoint(latitude, longitude));
        docData.put("geo_hash", GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude)));
        setDB(docData);
    }

    void deleteEvent(String docID) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    db.collection("events").document(docID).delete();
                    db.collection("event_attending").whereEqualTo("event", docID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    db.collection("event_attending").document(document.getId()).delete();
                                }
                            }
                        }
                    });
                    editor.putString("eventID", "");
                    editor.apply();
                    onBackPressed();
                    finish();
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_event).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    public void checkIfAbleToEdit(String organizer) {
        String me = preferences.getString("userID", "");
        if (me.equals(organizer)) {
            if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                Toast.makeText(getApplicationContext(), R.string.end_before_begin, Toast.LENGTH_SHORT).show();
                onBackPressed();
                finish();
            }
            if (cldr_start.getTime().before(Calendar.getInstance().getTime()) || cldr_end.getTime().before(Calendar.getInstance().getTime()) || cldr_start.getTime().equals(Calendar.getInstance().getTime()) || cldr_end.getTime().equals(Calendar.getInstance().getTime())) {
                Toast.makeText(getApplicationContext(), R.string.edit_finished, Toast.LENGTH_SHORT).show();
                onBackPressed();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.organizer_edit, Toast.LENGTH_SHORT).show();
            onBackPressed();
            finish();
        }
    }

    void fillData() {
        String userID = preferences.getString("userID", "");
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        String eventID = preferences.getString("eventID", "");
        if (!eventID.equals("")) {
            imageViewDeleteLayout.setVisibility(View.VISIBLE);
            imageViewDelete.setOnClickListener(view -> deleteEvent(eventID));
            DocumentReference docRef = db.collection("events").document(eventID);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> docData = document.getData();
                        eventName.setText(Objects.requireNonNull(Objects.requireNonNull(docData).get("event_name")).toString());
                        selected_item = Objects.requireNonNull(docData.get("event_type")).toString();
                        sp.setText(EventTypeToDrawable.getEventTypeToTranslation(this, Objects.requireNonNull(docData.get("event_type")).toString()));
                        imageViewEventType.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), EventTypeToDrawable.getEventTypeToDrawable(selected_item)));
                        description.setText(Objects.requireNonNull(docData.get("event_description")).toString());
                        minimum_level.setValue(Integer.parseInt(Objects.requireNonNull(docData.get("minimum_level")).toString()));
                        switch_public.setChecked(Objects.requireNonNull(docData.get("public")).toString().equals("true"));
                        switch_out.setChecked(Objects.requireNonNull(docData.get("outdoors")).toString().equals("true"));
                        float val_min = Float.parseFloat(Objects.requireNonNull(docData.get("minimum_players")).toString());
                        float val_max = Float.parseFloat(Objects.requireNonNull(docData.get("maximum_players")).toString());
                        slider_min.setValue((int) Math.floor(val_min));
                        slider_max.setValue((int) Math.floor(val_max));
                        Timestamp start_timestamp = (Timestamp) (docData.get("datetime"));
                        Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                        cldr_start.setTime(start_date);
                        Timestamp end_timestamp = (Timestamp) (docData.get("ending"));
                        Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                        cldr_end.setTime(end_date);
                        textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                        textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                        textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                        textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));

                        checkIfAbleToEdit(Objects.requireNonNull(Objects.requireNonNull(docData).get("organizer")).toString());

                        GeoPoint location_point = (GeoPoint) (docData.get("location"));
                        latitude = Objects.requireNonNull(location_point).getLatitude();
                        longitude = location_point.getLongitude();
                        String formattedLocation = getString(R.string.location) + "\n" + getString(R.string.latitude) + ": " + Math.round(latitude * 10000) / 10000.0 + "\n"
                                + getString(R.string.longitude) + ": " + Math.round(longitude * 10000) / 10000.0;
                        location.setText(formattedLocation);
                        chosenLocationMarker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                        mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));
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
                                        switch_notify.setChecked(Objects.requireNonNull(docData2.get("notifications")).toString().equals("true"));
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