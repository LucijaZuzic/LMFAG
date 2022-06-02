package com.example.lmfag.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterRating;
import com.example.lmfag.utility.DrawerHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RateEventActivity extends MenuInterfaceActivity {
    private String organizer;
    private List<String> people = new ArrayList<>();
    private List<Float> ratings = new ArrayList<>();
    private Context context = this;
    private RateEventActivity rateEventActivity = this;
    private RecyclerView recyclerViewPlayers;
    private String event_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_event);
         
        findViewById(R.id.imageViewApply).setOnClickListener(view -> {
            for (int i = 0; i < people.size(); i++) {
                updatePlayer(people.get(i), ratings.get(i));
            }
            checkRated();
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            startActivity(myIntent);
        });
        findViewById(R.id.imageViewDiscard).setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            context.startActivity(myIntent);
        });
    }

    private void checkRated() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> map = document.getData();
                        map.put("rated", true);
                        docRef.document(document.getId()).set(map);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWhoAttended();
        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers);
        fillData();
    }

    public void updateRating(int index, Float value) {
        ratings.set(index, value);
    }

    private void getWhoAttended() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
        db.collection("event_attending")
                .whereEqualTo("event", eventID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> map = document.getData();
                            String participantID = map.get("user").toString();
                            if (!userID.equals(participantID)) {
                                people.add(participantID);
                                ratings.add(0.0F);
                            }
                        }
                    }
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    CustomAdapterRating customAdapter = new CustomAdapterRating(people, context, preferences, rateEventActivity);
                    recyclerViewPlayers.setAdapter(customAdapter);
                } else {

                }
            }
        });
    }

    private void fillData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();
                    organizer = docData.get("organizer").toString();
                    if (organizer.equals(userID)) {
                        findViewById(R.id.organizerBanner).setVisibility(View.GONE);
                    }
                    getOrganizerData(organizer);
                    getEventType();
                }
            }
        });
    }

    private void getOrganizerData(String name) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    TextView myUsername = findViewById(R.id.textViewOrganizer);
                    myUsername.setText(data.get("username").toString());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image_organizer);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                        findViewById(R.id.profile_image_organizer).setOnClickListener(view -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID", name);
                            editor.apply();
                            Intent myIntent = new Intent(context, ViewProfileActivity.class);
                            startActivity(myIntent);
                        });
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
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

    private void getEventType() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();
                    event_type = docData.get("event_type").toString();
                }
            }
        });
    }

    private void updatePlayer(String playerID, Float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(playerID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (organizer.equals(document.getId())) {
                        Float value = Float.parseFloat(data.get("points_rank").toString());
                        RatingBar ratingBarOrganizer = findViewById(R.id.simpleRatingBarOrganizer);
                        value += ratingBarOrganizer.getRating();
                        data.put("points_rank", value);
                    }
                    String area_string = data.get("areas_of_interest").toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        List<String> areas_array = new ArrayList<>();
                        Collections.addAll(areas_array, area_string_array);
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        List<Float> points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Float.parseFloat(s));
                        }
                        if (areas_array.contains(event_type)) {
                            points_array.set(areas_array.indexOf(event_type), points_array.get(areas_array.indexOf(event_type)) + rating);
                        } else {
                            areas_array.add(event_type);
                            points_array.add(rating);
                        }
                        data.put("areas_of_interest", areas_array);
                        data.put("points_levels", points_array);
                    } else {
                        List<String> areas_array = new ArrayList<>();
                        List<Float> points_array = new ArrayList<>();
                        data.put("areas_of_interest", areas_array);
                        data.put("points_levels", points_array);
                    }
                    docRef.set(data);
                }
            }
        });
    }
}