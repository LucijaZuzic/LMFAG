package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyProfile extends AppCompatActivity {

    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        fillUserData();
        showFriends();
        showAreasOfInterest();

        redirectToCreateEvent();
        redirectToEditProfile();

        //ArrayList<String> friends_array = new ArrayList<String>();
        //CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array);
        //RecyclerView recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        //recyclerViewFriends.setAdapter(customAdapterFriends);
    }

    void fillUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();

                        TextView myUsername = findViewById(R.id.textViewUsername);
                        TextView myLocation = findViewById(R.id.textViewMyLocation);
                        TextView myDescription = findViewById(R.id.textViewMyDescription);
                        TextView myOrganizerRank = findViewById(R.id.textViewMyOrganizerRank);
                        TextView myOrganizerRankPoints = findViewById(R.id.textViewRankPoints);
                        RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                        myUsername.setText(data.get("username").toString());
                        myLocation.setText(data.get("location").toString());
                        Double points_rank = Double.parseDouble(data.get("points_rank").toString());
                        Integer rank = (int) (Math.floor(points_rank / 1000));
                        String text_rank = Integer.toString(rank);
                        Double upper_bound = Math.ceil(points_rank / 1000) * 1000;
                        String text_rank_points = points_rank.toString() + "/" + upper_bound;
                        myOrganizerRank.setText(text_rank);
                        myOrganizerRankPoints.setText(text_rank_points);
                        String area_string = data.get("areas_of_interest").toString();
                        String[] area_string_array = area_string.substring(1,area_string.length() - 1).split(", ");
                        List<String> areas_array = new ArrayList<String>();
                        for (int i = 0; i < area_string_array.length; i++) {
                            areas_array.add(area_string_array[i]);
                        }
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1,points_string.length() - 1).split(", ");
                        List<Double> points_array = new ArrayList<Double>();
                        for (int i = 0; i < points_string_array.length; i++) {
                            points_array.add(Double.parseDouble(points_string_array[i]));
                        }
                        CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
                        myDescription.setText(data.get("description").toString());
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Intent myIntent = new Intent(context, EditProfile.class);
                        startActivity(myIntent);
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    void redirectToCreateEvent() {
        FloatingActionButton fab = findViewById(R.id.floatingActionButtonCreateEvent);
        fab.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, CreateEvent.class);
            startActivity(myIntent);
        });
    }

    void redirectToEditProfile() {
        Context context = this;
        FloatingActionButton floatingActionButtonEditProfile = findViewById(R.id.floatingActionButtonEditProfile);
        floatingActionButtonEditProfile.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, EditProfile.class);
            startActivity(myIntent);
        });
    }
    void showFriends() {
        LinearLayout ll_friends_show = findViewById(R.id.linearLayoutShowFriends);
        RecyclerView ll_friends = findViewById(R.id.recyclerViewFriends);
        ImageView iv_friends = findViewById(R.id.imageViewExpandFriends);
        ll_friends_show.setOnClickListener(view -> {
            if (ll_friends.getVisibility() == View.GONE) {
                ll_friends.setVisibility(View.VISIBLE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_friends.setVisibility(View.GONE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    void showAreasOfInterest() {
        LinearLayout ll_areas_show = findViewById(R.id.linearLayoutShowAreasOfInterest);
        RecyclerView ll_areas = findViewById(R.id.recyclerViewAreasOfInterest);
        ImageView iv_areas = findViewById(R.id.imageViewExpandAreasOfInterest);
        ll_areas_show.setOnClickListener(view -> {
            if (ll_areas.getVisibility() == View.GONE) {
                ll_areas.setVisibility(View.VISIBLE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_areas.setVisibility(View.GONE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }
}