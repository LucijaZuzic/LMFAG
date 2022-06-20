package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyProfileAreasOfInterestFragment extends Fragment {
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
    }

    private void fillUserData(@NonNull View view) {
        TextView title = view.findViewById(R.id.list_title);
        title.setText(R.string.my_areas_of_interest);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String name = preferences.getString("userID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        RecyclerView recyclerViewAreasOfInterest = view.findViewById(R.id.recyclerViewList);
        String area_string = preferences.getString("user_areas_of_interest", "");
        if (area_string.length() > 2) {
            String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
            List<String> areas_array = new ArrayList<>(Arrays.asList(area_string_array));
            String points_string = preferences.getString("user_points_levels", "");
            String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
            List<Double> points_array = new ArrayList<>();
            for (String s : points_string_array) {
                points_array.add(Double.parseDouble(s));
            }
            CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
            if (areas_array.size() > 0) {
                noResults.setVisibility(View.GONE);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        }
    }
}