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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileEventsOrganizerFragment extends Fragment {
    private Context context;
    private Activity activity;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        getOrganizerEvents(view);

        TextView title = view.findViewById(R.id.list_title);
        title.setText("Organizing events");
    }

    private void getOrganizerEvents(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewEventsOrganizer = view.findViewById(R.id.recyclerViewList);
        List<String> events_array = new ArrayList<>();
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            db.collection("events").whereEqualTo("organizer", friendID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
}
