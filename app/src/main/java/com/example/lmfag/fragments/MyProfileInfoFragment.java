package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.utility.DrawerHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileInfoFragment extends Fragment {
    private Context context;
    private Activity activity;
    SharedPreferences preferences;
    FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();
        DrawerHelper.fillNavbarData(activity);
        fillUserData(view);
    }

    private void fillUserData(@NonNull View view) {
        String name = preferences.getString("userID", "");
        if(name.equalsIgnoreCase(""))
        {
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
        Double points_rank = Double.parseDouble(preferences.getString("userRankPoints", ""));
        Integer rank = (int) (Math.floor(points_rank / 1000));
        String text_rank = Integer.toString(rank);
        Double upper_bound = Math.ceil(points_rank / 1000) * 1000;
        if (upper_bound.equals(0.0)) {
            upper_bound = 1000.0;
        }
        String text_rank_points = points_rank + "/" + upper_bound;
        ProgressBar progressBar = view.findViewById(R.id.determinateBar);
        progressBar.setProgress((int)((points_rank - (upper_bound - 1000)) / 10));
        myOrganizerRank.setText(text_rank);
        myOrganizerRankPoints.setText(text_rank_points);
        myDescription.setText(Objects.requireNonNull(preferences.getString("userDescription", "")));
        String encoded = preferences.getString("userPicture", "");
        byte[] imageAsBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
        CircleImageView circleImageView = view.findViewById(R.id.profile_image);
        circleImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
    }
}