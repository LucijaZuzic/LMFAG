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
    private List<String> events_array;
    private List<Integer> timestamp_array;

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
        return inflater.inflate(R.layout.fragment_titled_list_events_time, container, false);
    }

    public void changeArray(RecyclerView recyclerViewEventsOrganizer) {
        List<String> events_array_selected_time = new ArrayList<>();
        for (int i = 0, n = events_array.size(); i < n; i++) {
            if (upcoming.isChecked() && timestamp_array.get(i) == 0) {
                events_array_selected_time.add(events_array.get(i));
            }
            if (current.isChecked() && timestamp_array.get(i) == 1) {
                events_array_selected_time.add(events_array.get(i));
            }
            if (past.isChecked() && timestamp_array.get(i) == 2) {
                events_array_selected_time.add(events_array.get(i));
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
        noResults = view.findViewById(R.id.noResults);
        upcoming = view.findViewById(R.id.upcoming);
        current = view.findViewById(R.id.current);
        past = view.findViewById(R.id.past);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsOrganizer = view.findViewById(R.id.recyclerViewList);
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
                for (int i = 0; i < timestamp_string.length; i++) {
                    timestamp_array.add(Integer.parseInt(timestamp_string[i]));
                }
            }

            if (!organizer_string[0].equals("")) {
                changeArray(recyclerViewEventsOrganizer);
            }
        }
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.events_organizer);
    }
}
