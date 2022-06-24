package com.example.lmfag.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.example.lmfag.utility.adapters.CustomAdapterEventTypeAdd;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FindEventsActivity extends MenuInterfaceActivity {
    TextView sp;
    List<String> all_areas;
    private Context context = this;
    private RecyclerView recyclerViewFindEvents;
    private LinearLayout nameCard, organizerCard, typeCard;
    private List<String> events_array;
    private List<Integer> timestamps_array;
    private String selected_item;
    private LinearLayout openableCard;
    private TextView noResults;
    private ImageView imageViewEventType;
    private EditText editTextEventName, editTextOrganizerName;
    private Chip upcoming, current, past;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_events);
        context = this;

        upcoming = findViewById(R.id.upcoming);
        current = findViewById(R.id.current);
        past = findViewById(R.id.past);
        upcoming.setOnClickListener((v) -> changeTimestampVisible());
        current.setOnClickListener((v) -> changeTimestampVisible());
        past.setOnClickListener((v) -> changeTimestampVisible());

        noResults = findViewById(R.id.noResults);
        recyclerViewFindEvents = findViewById(R.id.recyclerViewEvents);
        nameCard = findViewById(R.id.nameCard);
        organizerCard = findViewById(R.id.organizerCard);
        typeCard = findViewById(R.id.typeCard);
        imageViewEventType = findViewById(R.id.imageViewEventType);
        sp = findViewById(R.id.sp);
        all_areas = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));

        RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
        CustomAdapterEventTypeAdd customAdapterEventTypeAdd = new CustomAdapterEventTypeAdd(all_areas, this);
        recyclerViewAreasOfInterestNew.setAdapter(customAdapterEventTypeAdd);

        ImageView closeCard = findViewById(R.id.closeCard);
        openableCard = findViewById(R.id.openableCard);
        sp.setOnClickListener(view -> openableCard.setVisibility(View.VISIBLE));
        closeCard.setOnClickListener(view -> openableCard.setVisibility(View.GONE));
        ImageView ivEventName = findViewById(R.id.imageViewBeginSearchEventName);
        ivEventName.setOnClickListener(view -> getAllEventsWithName());
        ImageView ivOrganizerName = findViewById(R.id.imageViewBeginSearchOrganizerName);
        ivOrganizerName.setOnClickListener(view -> getAllEventsWithOrganizer());
        RadioButton organizerRadio, nameRadio, typeRadio;
        organizerRadio = findViewById(R.id.chooseOrganizer);
        organizerRadio.setOnClickListener(view -> showOrganizer());
        nameRadio = findViewById(R.id.chooseEventName);
        nameRadio.setOnClickListener(view -> showName());
        typeRadio = findViewById(R.id.chooseEventType);
        typeRadio.setOnClickListener(view -> showType());
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextOrganizerName = findViewById(R.id.editTextOrganizerName);

        getAllEventsWithOrganizer();
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

    public void changeTimestampVisible() {
        List<String> events_array_selected_time = new ArrayList<>();
        for (int i = 0, n = events_array.size(); i < n; i++) {
            if (upcoming.isChecked() && timestamps_array.get(i) == 0) {
                events_array_selected_time.add(events_array.get(i));
            }
            if (current.isChecked() && timestamps_array.get(i) == 1) {
                events_array_selected_time.add(events_array.get(i));
            }
            if (past.isChecked() && timestamps_array.get(i) == 2) {
                events_array_selected_time.add(events_array.get(i));
            }
        }
        Collections.sort(events_array_selected_time);
        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array_selected_time, context, preferences);
        recyclerViewFindEvents.setAdapter(customAdapterEvents);
        if (events_array_selected_time.size() > 0) {
            noResults.setVisibility(View.GONE);
        } else {
            noResults.setVisibility(View.VISIBLE);
        }
    }

    public void selectAreaOfInterest(String selected_item) {
        this.selected_item = selected_item;
        sp.setText(EventTypeToDrawable.getEventTypeToTranslation(this,selected_item));
        imageViewEventType.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), EventTypeToDrawable.getEventTypeToDrawable(selected_item)));

        openableCard.setVisibility(View.GONE);
        getAllEventsOfType();
    }

    public void showName() {
        nameCard.setVisibility(View.VISIBLE);
        organizerCard.setVisibility(View.GONE);
        typeCard.setVisibility(View.GONE);
    }

    public void showOrganizer() {
        nameCard.setVisibility(View.GONE);
        organizerCard.setVisibility(View.VISIBLE);
        typeCard.setVisibility(View.GONE);
    }

    public void showType() {
        nameCard.setVisibility(View.GONE);
        organizerCard.setVisibility(View.GONE);
        typeCard.setVisibility(View.VISIBLE);
    }

    private void getAllEventsOfType() {
        events_array = new ArrayList<>();
        timestamps_array = new ArrayList<>();
        Query q = db.collection("events");

        if (selected_item != null) {
            q = db.collection("events").whereEqualTo("event_type", selected_item);
        }
        q.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        events_array.add(document.getId());
                        Calendar cldr_start = Calendar.getInstance();
                        Timestamp start_timestamp = (Timestamp) (document.getData().get("datetime"));
                        Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                        cldr_start.setTime(start_date);
                        Calendar cldr_end = Calendar.getInstance();
                        Timestamp end_timestamp = (Timestamp) (document.getData().get("ending"));
                        Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                        cldr_end.setTime(end_date);
                        timestamps_array.add(checkTimestamp(cldr_start, cldr_end));
                    }
                    changeTimestampVisible();
                } else {
                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                recyclerViewFindEvents.setAdapter(customAdapterEvents);
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAllEventsWithName() {
        events_array = new ArrayList<>();
        timestamps_array = new ArrayList<>();
        Query q = db.collection("events");
        String text = editTextEventName.getText().toString();
        if (!text.equals("")) {
            q = db.collection("events").whereEqualTo("event_name", text);
        }
        q.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        events_array.add(document.getId());
                        Calendar cldr_start = Calendar.getInstance();
                        Timestamp start_timestamp = (Timestamp) (document.getData().get("datetime"));
                        Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                        cldr_start.setTime(start_date);
                        Calendar cldr_end = Calendar.getInstance();
                        Timestamp end_timestamp = (Timestamp) (document.getData().get("ending"));
                        Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                        cldr_end.setTime(end_date);
                        timestamps_array.add(checkTimestamp(cldr_start, cldr_end));
                    }
                    changeTimestampVisible();
                } else {
                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                recyclerViewFindEvents.setAdapter(customAdapterEvents);
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAllEventsWithOrganizer() {
        events_array = new ArrayList<>();
        timestamps_array = new ArrayList<>();
        Query q = db.collection("events");
        String organizerName = editTextOrganizerName.getText().toString();
        if (!organizerName.equals("")) {
            db.collection("users").whereEqualTo("username", organizerName).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            q.whereEqualTo("organizer", document.getId()).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (task1.getResult().size() > 0) {
                                        for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                            events_array.add(document1.getId());
                                            Calendar cldr_start = Calendar.getInstance();
                                            Timestamp start_timestamp = (Timestamp) (document1.getData().get("datetime"));
                                            Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                                            cldr_start.setTime(start_date);
                                            Calendar cldr_end = Calendar.getInstance();
                                            Timestamp end_timestamp = (Timestamp) (document1.getData().get("ending"));
                                            Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                                            cldr_end.setTime(end_date);
                                            timestamps_array.add(checkTimestamp(cldr_start, cldr_end));
                                        }
                                        changeTimestampVisible();
                                    } else {
                                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                                        noResults.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                                    noResults.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else {
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                        noResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    noResults.setVisibility(View.VISIBLE);
                }
            });

        } else {
            q.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            events_array.add(document.getId());
                            Calendar cldr_start = Calendar.getInstance();
                            Timestamp start_timestamp = (Timestamp) (document.getData().get("datetime"));
                            Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                            cldr_start.setTime(start_date);
                            Calendar cldr_end = Calendar.getInstance();
                            Timestamp end_timestamp = (Timestamp) (document.getData().get("ending"));
                            Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                            cldr_end.setTime(end_date);
                            timestamps_array.add(checkTimestamp(cldr_start, cldr_end));
                        }
                        changeTimestampVisible();
                    } else {
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                        noResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<>(), context, preferences);
                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    noResults.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}