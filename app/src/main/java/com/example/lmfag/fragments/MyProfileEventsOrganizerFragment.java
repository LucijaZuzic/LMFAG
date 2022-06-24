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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.adapters.CustomAdapterEventDelete;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyProfileEventsOrganizerFragment extends Fragment {
    private Context context;
    private Activity activity;
    private TextView noResults;
    private Chip upcoming, current, past;
    private boolean only_notified = false;
    private List<String> events_array;
    private List<Integer> timestamp_array;
    private List<String> event_subscriber_array;
    private List<Integer> event_subscriber_timestamp_array;

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
        return inflater.inflate(R.layout.fragment_titled_list_events_subscribed, container, false);
    }

    public void changeArray(RecyclerView recyclerViewEventsOrganizer) {
        List<String> events_array_selected_time = new ArrayList<>();
        List<String> events = new ArrayList<>();
        List<Integer> timestamps = new ArrayList<>();

        if (!only_notified) {
            events = events_array;
            timestamps = timestamp_array;
        } else {
            for (int i = 0, n = event_subscriber_array.size(); i < n; i++) {
                if (events_array.contains(event_subscriber_array.get(i))) {
                    events.add(event_subscriber_array.get(i));
                    timestamps.add(event_subscriber_timestamp_array.get(i));
                }
            }
        }

        for (int i = 0, n = events.size(); i < n; i++) {
            if (upcoming.isChecked() && timestamps.get(i) == 0) {
                events_array_selected_time.add(events.get(i));
            }
            if (current.isChecked() && timestamps.get(i) == 1) {
                events_array_selected_time.add(events.get(i));
            }
            if (past.isChecked() && timestamps.get(i) == 2) {
                events_array_selected_time.add(events.get(i));
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        Collections.sort(events_array_selected_time);
        recyclerViewEventsOrganizer.setAdapter(new CustomAdapterEventDelete(events_array_selected_time, context, preferences));
        if (events_array_selected_time.size() > 0) {
            noResults.setVisibility(View.GONE);
        } else {
            noResults.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.events_organizer);
        noResults = view.findViewById(R.id.noResults);
        upcoming = view.findViewById(R.id.upcoming);
        current = view.findViewById(R.id.current);
        past = view.findViewById(R.id.past);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsOrganizer = view.findViewById(R.id.recyclerViewList);
        SwitchCompat notificationsOnly = view.findViewById(R.id.onlyShowNotificationToggle);
        upcoming.setOnClickListener((v) -> changeArray(recyclerViewEventsOrganizer));
        current.setOnClickListener((v) -> changeArray(recyclerViewEventsOrganizer));
        past.setOnClickListener((v) -> changeArray(recyclerViewEventsOrganizer));
        events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            String[] organizer_string = preferences.getString("userOrganizer", "").split("_");
            if (!organizer_string[0].equals("")) {
                events_array.addAll(Arrays.asList(organizer_string));
            }

            String[] timestamp_string = preferences.getString("userOrganizerTimestamp", "").split("_");
            timestamp_array = new ArrayList<>();
            if (!timestamp_string[0].equals("")) {
                for (String s : timestamp_string) {
                    timestamp_array.add(Integer.parseInt(s));
                }
            }

            String[] subscriber_string = preferences.getString("userSubscriber", "").split("_");
            event_subscriber_array = new ArrayList<>();
            if (!subscriber_string[0].equals("")) {
                event_subscriber_array.addAll(Arrays.asList(subscriber_string));
            }

            String[] subscriber_timestamp_string = preferences.getString("userSubscriberTimestamp", "").split("_");
            event_subscriber_timestamp_array = new ArrayList<>();
            if (!subscriber_timestamp_string[0].equals("")) {
                for (String s : subscriber_timestamp_string) {
                    event_subscriber_timestamp_array.add(Integer.parseInt(s));
                }
            }

            if (!organizer_string[0].equals("")) {
                changeArray(recyclerViewEventsOrganizer);
            }
        }
        notificationsOnly.setOnClickListener(someView -> {
            if (notificationsOnly.isChecked()) {
                title.setText(R.string.events_subscriber_organizer);
            } else {
                title.setText(R.string.events_organizer);
            }
            only_notified = notificationsOnly.isChecked();
            changeArray(recyclerViewEventsOrganizer);
        });
    }
}
