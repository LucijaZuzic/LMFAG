package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindEvents extends AppCompatActivity {
    Context context = this;
    RecyclerView recyclerViewFindEvents;
    SharedPreferences preferences;
    Spinner search_params;
    Spinner sort_params;
    Spinner sp;
    CardView nameCard, organizerCard, typeCard;
    private String selected_item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_events);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewFindEvents = findViewById(R.id.recyclerViewEvents);
        nameCard = findViewById(R.id.nameCard);
        organizerCard = findViewById(R.id.organizerCard);
        typeCard = findViewById(R.id.typeCard);
        sp = findViewById(R.id.sp);
        fillSpinner();
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                selected_item = adapter.getItemAtPosition(i).toString();
                ImageView iv = findViewById(R.id.imageViewEventType);
                iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selected_item)));
                getAllEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        ImageView ivEventName = findViewById(R.id.imageViewBeginSearchEventName);
        ivEventName.setOnClickListener(view -> {
            getAllEventsWithName();
        });
        ImageView ivOrganizerName = findViewById(R.id.imageViewBeginSearchOrganizerName);
        ivOrganizerName.setOnClickListener(view -> {
            getAllEventsWithOrganizer();
        });
    }
    public void showName(View view) {
        nameCard.setVisibility(View.VISIBLE);
        organizerCard.setVisibility(View.GONE);
        typeCard.setVisibility(View.GONE);
    }
    public void showOrganizer(View view) {
        nameCard.setVisibility(View.GONE);
        organizerCard.setVisibility(View.VISIBLE);
        typeCard.setVisibility(View.GONE);
    }
    public void showType(View view) {
        nameCard.setVisibility(View.GONE);
        organizerCard.setVisibility(View.GONE);
        typeCard.setVisibility(View.VISIBLE);
    }

    void fillSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }
    void fillSpinner3() {
        ArrayAdapter<CharSequence> adapter_search_params = ArrayAdapter.createFromResource(this,
                R.array.friend_search_params, android.R.layout.simple_spinner_item);
        adapter_search_params.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        search_params.setAdapter(adapter_search_params);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.friend_sort_params, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_params.setAdapter(adapter);
    }
    void getAllEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        Query q = db.collection("events");

        if (selected_item != null) {
            q = db.collection("events").whereEqualTo("event_type", selected_item);
        }
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            events_array.add(document.getId());
                        }
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    }
                }
            }
        });
    }
    void getAllEventsWithName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        Query q = db.collection("events");
        EditText et = findViewById(R.id.editTextEventName);
        String text = et.getText().toString();
        if (!text.equals("")) {
            q = db.collection("events").whereEqualTo("event_name", text);
        }
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            events_array.add(document.getId());
                        }
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    } else {
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    }
                } else {
                    CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                    recyclerViewFindEvents.setAdapter(customAdapterEvents);
                }
            }
        });
    }

    void getAllEventsWithOrganizer() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        Query q = db.collection("events");
        EditText et = findViewById(R.id.editTextOrganizerName);
        String organizerName = et.getText().toString();
        if (!organizerName.equals("")) {
            db.collection("users").whereEqualTo("username", organizerName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                q.whereEqualTo("organizer", document.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().size() > 0) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    events_array.add(document.getId());
                                                }
                                                CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                                                recyclerViewFindEvents.setAdapter(customAdapterEvents);
                                            } else {
                                                CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                                                recyclerViewFindEvents.setAdapter(customAdapterEvents);
                                            }
                                        } else {
                                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                                            recyclerViewFindEvents.setAdapter(customAdapterEvents);
                                        }
                                    }
                                });
                            }

                        } else {
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                            recyclerViewFindEvents.setAdapter(customAdapterEvents);
                        }
                    } else {
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    }
                }
            });

        } else {
            q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                events_array.add(document.getId());
                            }
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                            recyclerViewFindEvents.setAdapter(customAdapterEvents);
                        } else {
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                            recyclerViewFindEvents.setAdapter(customAdapterEvents);
                        }
                    } else {
                        CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(new ArrayList<String>(), context, preferences);
                        recyclerViewFindEvents.setAdapter(customAdapterEvents);
                    }
                }
            });
        }
    }
}