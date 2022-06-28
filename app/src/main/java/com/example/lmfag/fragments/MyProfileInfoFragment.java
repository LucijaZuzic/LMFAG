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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.LevelTransformation;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileInfoFragment extends Fragment {
    private SharedPreferences preferences;
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
        return inflater.inflate(R.layout.fragment_my_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        DrawerHelper.fillNavbarData(activity);
        fillUserData(view);
    }

    private void fillUserData(@NonNull View view) {
        String name = preferences.getString("userID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        TextView myUsername = view.findViewById(R.id.textViewUsername);
        TextView myLocation = view.findViewById(R.id.textViewMyLocation);
        TextView myDescription = view.findViewById(R.id.textViewMyDescription);
        TextView myOrganizerRank = view.findViewById(R.id.textViewMyOrganizerRank);
        TextView myOrganizerRankPoints = view.findViewById(R.id.textViewRankPoints);
        myUsername.setText(preferences.getString("userUsername", ""));
        myLocation.setText(preferences.getString("userLocation", ""));
        double points_rank = Double.parseDouble(preferences.getString("userRankPoints", ""));
        int rank = LevelTransformation.level(points_rank);
        String text_rank = Integer.toString(rank);
        double upper_bound = LevelTransformation.upper_bound(rank);
        double lower_bound = LevelTransformation.lower_bound(rank);
        double range = upper_bound - lower_bound;
        String text_rank_points = String.format(Locale.getDefault(), "%.0f / %.0f", points_rank, upper_bound).replace(',', '.');
        ProgressBar progressBar = view.findViewById(R.id.determinateBar);
        progressBar.setProgress((int) ((points_rank - lower_bound) / range * 100));
        myOrganizerRank.setText(text_rank);
        myOrganizerRankPoints.setText(text_rank_points);
        myDescription.setText(Objects.requireNonNull(preferences.getString("userDescription", "")));
        String imageShow = preferences.getString("showImage", "");
        if (imageShow.equals("true")) {
            CircleImageView circleImageView = view.findViewById(R.id.profile_image);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
            final long ONE_MEGABYTE = 1024 * 1024;
            imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(context.getApplicationContext()).asBitmap().load(bytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView));
        }
    }
}