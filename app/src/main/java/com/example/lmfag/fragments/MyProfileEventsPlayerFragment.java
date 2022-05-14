package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyProfileEventsPlayerFragment extends Fragment {
    private Context context;
    private Activity activity;
    private boolean only_notified = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_titled_list_events_subsribed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.list_title);
        title.setText("My playing events");

        SwitchCompat notificationsOnly = view.findViewById(R.id.onlyShowNotificationToggle);
        if (only_notified) {
            getSubscriberEvents(view);
        } else {
            getPlayerEvents(view);
        }
        notificationsOnly.setOnClickListener(someView -> {
            only_notified = !only_notified;
            if (only_notified) {
                getSubscriberEvents(view);
            } else {
                getPlayerEvents(view);
            }
        });
    }

    private void getPlayerEvents(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsPlayer = view.findViewById(R.id.recyclerViewList);
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
    private void getSubscriberEvents(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> events_array = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsPlayer = view.findViewById(R.id.recyclerViewList);
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String isSubscribed = document.getData().get("notifications").toString();
                                if (isSubscribed.equals("true")) {
                                    events_array.add(document.getData().get("event").toString());
                                }
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
