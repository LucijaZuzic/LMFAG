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
import com.example.lmfag.utility.adapters.CustomAdapterEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ViewProfileEventsPlayerFragment extends Fragment {
    List<String> events_player_array;
    List<String> event_subscriber_array;
    private Context context;
    private Activity activity;
    private SharedPreferences preferences;
    private boolean only_notified = false;
    private TextView noResults;

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

    public void changeArray(RecyclerView recyclerViewEventsPlayer) {
        if (only_notified) {
            if (!event_subscriber_array.get(0).equals("")) {
                recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(event_subscriber_array, context, preferences));
                if (event_subscriber_array.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(new ArrayList<>(), context, preferences));
                noResults.setVisibility(View.VISIBLE);
            }
        } else {
            if (!events_player_array.get(0).equals("")) {
                recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(events_player_array, context, preferences));
                if (events_player_array.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(new ArrayList<>(), context, preferences));
                noResults.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        noResults = view.findViewById(R.id.noResults);
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.events_view_player);
        RecyclerView recyclerViewEventsPlayer = view.findViewById(R.id.recyclerViewList);
        SwitchCompat notificationsOnly = view.findViewById(R.id.onlyShowNotificationToggle);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            String[] player_string = preferences.getString("friendPlayer", "").split("_");
            events_player_array = new ArrayList<>();
            Collections.addAll(events_player_array, player_string);
            String[] subscriber_string = preferences.getString("friendSubscriber", "").split("_");
            event_subscriber_array = new ArrayList<>();
            event_subscriber_array.addAll(Arrays.asList(subscriber_string));
            changeArray(recyclerViewEventsPlayer);
        }
        notificationsOnly.setOnClickListener(someView -> {
            if (notificationsOnly.isChecked()) {
                title.setText(R.string.events_view_subscriber);
            } else {
                title.setText(R.string.events_view_player);
            }
            only_notified = notificationsOnly.isChecked();
            changeArray(recyclerViewEventsPlayer);
        });
    }

}
