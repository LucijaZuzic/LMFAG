package com.example.lmfag.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.example.lmfag.utility.DrawerHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends MenuInterfaceActivity {

    private Context context = this;
    private RecyclerView recyclerViewEventsOrganizer, recyclerViewEventsPlayer;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        DrawerHelper.fillNavbarData(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewEventsPlayer = findViewById(R.id.recyclerViewEventsPlayer);
        recyclerViewEventsOrganizer = findViewById(R.id.recyclerViewEventsOrganizer);
        getOrganizerEvents();
        showOrganizer();
        showPlayer();
        getPlayerEvents();
    }


    private void showFriends() {
        LinearLayout ll_friends_show = findViewById(R.id.linearLayoutShowFriends);
        RecyclerView ll_friends = findViewById(R.id.recyclerViewFriends);
        ImageView iv_friends = findViewById(R.id.imageViewExpandFriends);
        ll_friends_show.setOnClickListener(view -> {
            if (ll_friends.getVisibility() == View.GONE) {
                ll_friends.setVisibility(View.VISIBLE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_friends.setVisibility(View.GONE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    private void showOrganizer() {
        LinearLayout ll_areas_show = findViewById(R.id.linearLayoutShowOrganizer);
        RecyclerView ll_areas = findViewById(R.id.recyclerViewEventsOrganizer);
        ImageView iv_areas = findViewById(R.id.imageViewExpandOrganizer);
        ll_areas_show.setOnClickListener(view -> {
            if (ll_areas.getVisibility() == View.GONE) {
                ll_areas.setVisibility(View.VISIBLE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_areas.setVisibility(View.GONE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    private void showPlayer() {
        LinearLayout ll_areas_show = findViewById(R.id.linearLayoutShowPlayer);
        RecyclerView ll_areas = findViewById(R.id.recyclerViewEventsPlayer);
        ImageView iv_areas = findViewById(R.id.imageViewExpandPlayer);
        ll_areas_show.setOnClickListener(view -> {
            if (ll_areas.getVisibility() == View.GONE) {
                ll_areas.setVisibility(View.VISIBLE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_areas.setVisibility(View.GONE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    private void getOrganizerEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            db.collection("events").whereEqualTo("organizer", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    private void getPlayerEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                events_array.add(document.getData().get("event").toString());
                            }
                            CustomAdapterEvent customAdapterEvents = new CustomAdapterEvent(events_array, context, preferences);
                            recyclerViewEventsPlayer.setAdapter(customAdapterEvents);
                        }
                    }
                }
            });
        }
    }
}