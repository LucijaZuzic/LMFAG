package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEvents extends AppCompatActivity {

    Context context = this;
    RecyclerView recyclerViewEventsOrganizer, recyclerViewEventsPlayer;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewEventsPlayer = findViewById(R.id.recyclerViewEventsPlayer);
        recyclerViewEventsOrganizer = findViewById(R.id.recyclerViewEventsOrganizer);
        getOrganizerEvents();
        getPlayerEvents();
    }

    void getOrganizerEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            db.collection("events").whereEqualTo("organizer",userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                events_array.add(document.getId());
                            }
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                            recyclerViewEventsOrganizer.setAdapter(customAdapterEvents);
                        }
                    }
                }
            });
        }
    }
    void getPlayerEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        String eventID = preferences.getString("eventID", "");
        if (!userID.equals("")) {
            db.collection("events_attending").whereEqualTo("event", eventID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("user").equals(userID)) {
                                    events_array.add(document.getId());
                                }
                            }
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                            recyclerViewEventsOrganizer.setAdapter(customAdapterEvents);
                        }
                    }
                }
            });
        }
    }
}