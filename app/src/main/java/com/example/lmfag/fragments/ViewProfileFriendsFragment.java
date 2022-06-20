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
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ViewProfileFriendsFragment extends Fragment {
    private Context context;
    private Activity activity;
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
        return inflater.inflate(R.layout.fragment_titled_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        noResults = view.findViewById(R.id.noResults);
        fillUserData(view);
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.user_friends);
    }

    private void fillUserData(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewFriends = view.findViewById(R.id.recyclerViewList);
        String name = preferences.getString("friendID", "");
        db.collection("friends")
                .document(name)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            String friends_string = Objects.requireNonNull(Objects.requireNonNull(data).get("friends")).toString();
                            if (friends_string.length() > 2) {
                                String[] friends_string_array = friends_string.substring(1, friends_string.length() - 1).split(", ");
                                List<String> friends_array = new ArrayList<>(Arrays.asList(friends_string_array));
                                CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array, context, preferences);
                                recyclerViewFriends.setAdapter(customAdapterFriends);
                                if (friends_array.size() > 0) {
                                    noResults.setVisibility(View.GONE);
                                } else {
                                    noResults.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
    }
}