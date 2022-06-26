package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewParticipantsActivity extends MenuInterfaceActivity {
    private String organizer;
    private List<String> people;
    private Context context = this;
    private RecyclerView recyclerViewPlayers;
    private CircleImageView circleImageView;
    private TextView rate_event_list_entry_banner_text, organizerUsername;
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_participants);

        noResults = findViewById(R.id.noResults);
        circleImageView = findViewById(R.id.profile_image_organizer);
        rate_event_list_entry_banner_text = findViewById(R.id.rate_event_list_entry_banner_text);
        organizerUsername = findViewById(R.id.textViewOrganizer);
        people = new ArrayList<>();
        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers);
        context = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        getWhoAttended();
    }

    private void getWhoAttended() {
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        db.collection("event_attending")
                .whereEqualTo("event", eventID)
                .whereEqualTo("attending", true)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> map = document.getData();
                                String participantID = Objects.requireNonNull(map.get("user")).toString();
                                people.add(participantID);
                            }
                        }
                        CustomAdapterFriends customAdapter = new CustomAdapterFriends(people, context, preferences);
                        recyclerViewPlayers.setAdapter(customAdapter);
                        if (people.size() > 0) {
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                        fillData();
                    }
                });
    }

    private void fillData() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();

                    rate_event_list_entry_banner_text.setText(Objects.requireNonNull(Objects.requireNonNull(docData).get("event_name")).toString());
                    rate_event_list_entry_banner_text.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(Objects.requireNonNull(document.get("event_type")).toString()), 0, 0, 0);

                    organizer = Objects.requireNonNull(Objects.requireNonNull(docData).get("organizer")).toString();
                    getOrganizerData(organizer);
                }
            }
        });
    }

    private void getOrganizerData(String name) {
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    organizerUsername.setText(Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());

                    circleImageView.setOnClickListener(view -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("friendID", name);
                        editor.apply();
                        Intent myIntent = new Intent(context, ViewProfileActivity.class);
                        startActivity(myIntent);
                        finish();
                    });
                    String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(circleImageView.getContext().getApplicationContext()).asBitmap().load(bytes).into(circleImageView)).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    }
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    finish();
                    //Log.d(TAG, "No such document");
                }
            }
        });
    }


}