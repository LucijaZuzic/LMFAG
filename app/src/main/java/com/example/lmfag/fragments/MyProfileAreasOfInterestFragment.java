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
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterest;
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

public class MyProfileAreasOfInterestFragment extends Fragment {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile_areas_of_interest, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillUserData();
    }

    private void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String name = preferences.getString("userID", "");
        if(name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }

        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    RecyclerView recyclerViewAreasOfInterest = activity.findViewById(R.id.recyclerViewAreasOfInterest);
                    String area_string = data.get("areas_of_interest").toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        List<String> areas_array = new ArrayList<>(Arrays.asList(area_string_array));
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        List<Double> points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Double.parseDouble(s));
                        }
                        CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
                    }
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    //Log.d(TAG, "No such document");
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}