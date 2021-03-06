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
import com.example.lmfag.utility.adapters.CustomAdapterEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyProfileEventsUnratedFragment extends Fragment {
    private Context context;
    private Activity activity;
    private TextView noResults;

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
        return inflater.inflate(R.layout.fragment_titled_list, container, false);
    }

    public void changeArray(RecyclerView recyclerViewEventsUnrated) {
        List<String> events_array_selected_time = new ArrayList<>();
        for (int i = 0, n = events_array.size(); i < n; i++) {
            if (timestamp_array.get(i) == 2) {
                events_array_selected_time.add(events_array.get(i));
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        Collections.sort(events_array_selected_time);
        recyclerViewEventsUnrated.setAdapter(new CustomAdapterEvent(events_array_selected_time, context, preferences));
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsUnrated = view.findViewById(R.id.recyclerViewList);


        events_array = new ArrayList<>();
        String userID = preferences.getString("userID", "");
        if (!userID.equals("")) {
            String[] organizer_string = preferences.getString("userUnrated", "").split("_");
            if (!organizer_string[0].equals("")) {
                events_array.addAll(Arrays.asList(organizer_string));
            }

            String[] timestamp_string = preferences.getString("userUnratedTimestamp", "").split("_");
            timestamp_array = new ArrayList<>();
            if (!timestamp_string[0].equals("")) {
                for (int i = 0; i < timestamp_string.length; i++) {
                    timestamp_array.add(Integer.parseInt(timestamp_string[i]));
                }
            }

            if (!organizer_string[0].equals("")) {
                changeArray(recyclerViewEventsUnrated);
            }
        }
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.events_unrated);
    }
}
