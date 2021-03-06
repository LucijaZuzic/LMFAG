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
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ViewProfileEventsPlayerFragment extends Fragment {
    private List<String> events_player_array;
    private List<String> event_subscriber_array;
    private List<Integer> events_player_timestamp_array;
    private List<Integer> event_subscriber_timestamp_array;
    private Context context;
    private Activity activity;
    private SharedPreferences preferences;
    private TextView noResults;
    private Chip upcoming, current, past;

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

    public void changeArray(RecyclerView recyclerViewEventsPlayer) {
        List<String> events_array_selected_time = new ArrayList<>();

        for (int i = 0, n = events_player_array.size(); i < n; i++) {
            if (upcoming.isChecked() && events_player_timestamp_array.get(i) == 0) {
                events_array_selected_time.add(events_player_array.get(i));
            }
            if (current.isChecked() && events_player_timestamp_array.get(i) == 1) {
                events_array_selected_time.add(events_player_array.get(i));
            }
            if (past.isChecked() && events_player_timestamp_array.get(i) == 2) {
                events_array_selected_time.add(events_player_array.get(i));
            }
        }

        Collections.sort(events_array_selected_time);
        recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(events_array_selected_time, context, preferences));
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
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.events_view_player);
        upcoming = view.findViewById(R.id.upcoming);
        current = view.findViewById(R.id.current);
        past = view.findViewById(R.id.past);

        RecyclerView recyclerViewEventsPlayer = view.findViewById(R.id.recyclerViewList);

        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        upcoming.setOnClickListener((v) -> changeArray(recyclerViewEventsPlayer));
        current.setOnClickListener((v) -> changeArray(recyclerViewEventsPlayer));
        past.setOnClickListener((v) -> changeArray(recyclerViewEventsPlayer));

        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {

            String[] player_string = preferences.getString("friendPlayer", "").split("_");
            events_player_array = new ArrayList<>();
            if (!player_string[0].equals("")) {
                events_player_array.addAll(Arrays.asList(player_string));
            }

            String[] player_timestamp_string = preferences.getString("friendPlayerTimestamp", "").split("_");
            events_player_timestamp_array = new ArrayList<>();
            if (!player_timestamp_string[0].equals("")) {
                for (int i = 0; i < player_timestamp_string.length; i++) {
                    events_player_timestamp_array.add(Integer.parseInt(player_timestamp_string[i]));
                }
            }

            String[] subscriber_string = preferences.getString("friendSubscriber", "").split("_");
            event_subscriber_array = new ArrayList<>();
            if (!subscriber_string[0].equals("")) {
                event_subscriber_array.addAll(Arrays.asList(subscriber_string));
            }

            String[] subscriber_timestamp_string = preferences.getString("friendSubscriberTimestamp", "").split("_");
            event_subscriber_timestamp_array = new ArrayList<>();
            if (!subscriber_timestamp_string[0].equals("")) {
                for (int i = 0; i < subscriber_timestamp_string.length; i++) {
                    event_subscriber_timestamp_array.add(Integer.parseInt(subscriber_timestamp_string[i]));
                }
            }
            changeArray(recyclerViewEventsPlayer);
        }
        title.setText(R.string.events_view_player);
    }

}
