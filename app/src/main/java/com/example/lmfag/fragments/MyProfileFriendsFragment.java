package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.example.lmfag.utility.adapters.TabPagerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileFriendsFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabPagerAdapter tabPagerAdapter;
    private Context context;
    private Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();
        activity = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        fillUserData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile_friends, container, false);
    }

    private void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        RecyclerView recyclerViewFriends = activity.findViewById(R.id.recyclerViewFriends);
        String name = preferences.getString("userID", "");
        db.collection("friends")
                .document(name)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    String friends_string = data.get("friends").toString();
                    if (friends_string.length() > 2) {
                        String[] friends_string_array = friends_string.substring(1, friends_string.length() - 1).split(", ");
                        List<String> friends_array = new ArrayList<>(Arrays.asList(friends_string_array));
                        CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array, context, preferences);
                        recyclerViewFriends.setAdapter(customAdapterFriends);
                    }
                }
            }
        });
    }
}