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
import java.util.List;

public class ViewProfileEventsPlayerFragment extends Fragment {
    private Context context;
    private Activity activity;
    private SharedPreferences preferences;
    private boolean only_notified = false;
    List<String> events_player_array;
    List<String> event_subscriber_array;

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
            }
        } else {
            if (!events_player_array.get(0).equals("")) {
                recyclerViewEventsPlayer.setAdapter(new CustomAdapterEvent(events_player_array, context, preferences));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        TextView title = view.findViewById(R.id.list_title);
        title.setText("Playing events");
        RecyclerView recyclerViewEventsPlayer = view.findViewById(R.id.recyclerViewList);
        SwitchCompat notificationsOnly = view.findViewById(R.id.onlyShowNotificationToggle);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            String[] player_string = preferences.getString("friendPlayer", "").split("_");
            events_player_array = new ArrayList<>();
            for (String event: player_string) {
                events_player_array.add(event);
            }
            String[] subscriber_string = preferences.getString("friendSubscriber", "").split("_");
            event_subscriber_array = new ArrayList<>();
            for (String event: subscriber_string) {
                event_subscriber_array.add(event);
            }
            changeArray(recyclerViewEventsPlayer);
        }
        notificationsOnly.setOnClickListener(someView -> {
            only_notified = notificationsOnly.isChecked();
            changeArray(recyclerViewEventsPlayer);
        });
    }

}
