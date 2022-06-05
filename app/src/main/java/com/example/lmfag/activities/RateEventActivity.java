package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.utility.adapters.CustomAdapterRating;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RateEventActivity extends MenuInterfaceActivity {
    private String organizer;
    private List<String> people;
    private List<Float> ratings;
    private Context context = this;
    private RateEventActivity rateEventActivity;
    private RecyclerView recyclerViewPlayers;
    private String event_type;
    private RatingBar ratingBarOrganizer;
    private CircleImageView circleImageView;
    private CardView rate_event_list_entry_banner_card;
    private TextView rate_event_list_entry_banner_text, organizerUsername;
    private LinearLayout organizerBanner;
    private TextView noResults;
    private final Calendar cldr_end = Calendar.getInstance();
    private final Calendar cldr_start = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_event);
        ratingBarOrganizer = findViewById(R.id.simpleRatingBarOrganizer);
        noResults = findViewById(R.id.noResults);
        circleImageView = findViewById(R.id.profile_image_organizer);
        rate_event_list_entry_banner_card = findViewById(R.id.rate_event_list_entry_banner_card);
        rate_event_list_entry_banner_text = findViewById(R.id.rate_event_list_entry_banner_text);
        organizerUsername = findViewById(R.id.textViewOrganizer);
        organizerBanner = findViewById(R.id.organizerBanner);
        people = new ArrayList<>();
        ratings = new ArrayList<>();
        rateEventActivity = this;
        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers);
        context = this;
        ImageView apply =  findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> updatePlayer(0));
        findViewById(R.id.imageViewDiscard).setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewEventActivity.class);
            context.startActivity(myIntent);
        });
    }

    private void checkRated() {
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
                        onBackPressed();
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWhoAttended();
    }

    public void updateRating(int index, Float value) {
        ratings.set(index, value);
    }

    private void getWhoAttended() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
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
                        boolean user_participated = false;
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> map = document.getData();
                                String participantID = Objects.requireNonNull(map.get("user")).toString();
                                if (!userID.equals(participantID)) {
                                    people.add(participantID);
                                    ratings.add(0.0F);
                                }
                                if (userID.equals(participantID)){
                                    user_participated = true;
                                    if (Objects.requireNonNull(map.get("rated")).toString().equals("true")) {
                                        Toast.makeText(getApplicationContext(), R.string.rate_twice, Toast.LENGTH_SHORT).show();
                                        Intent myIntent = new Intent(context, ViewEventActivity.class);
                                        startActivity(myIntent);
                                        finish();
                                        return;
                                    }
                                }
                            }
                        }
                        CustomAdapterRating customAdapter = new CustomAdapterRating(people, context, preferences, rateEventActivity);
                        recyclerViewPlayers.setAdapter(customAdapter);
                        if (people.size() > 0) {
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                        fillEventData(user_participated);
                    }
                });
    }

    private void fillEventData(boolean user_participated) {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
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

                    Timestamp start_timestamp = (Timestamp) (Objects.requireNonNull(docData).get("datetime"));
                    Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                    cldr_start.setTime(start_date);
                    Timestamp end_timestamp = (Timestamp) (Objects.requireNonNull(docData).get("ending"));
                    Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                    cldr_end.setTime(end_date);
                    if (cldr_start.getTime().after(cldr_end.getTime()) || cldr_start.getTime().equals(cldr_end.getTime())) {
                        Toast.makeText(getApplicationContext(), R.string.end_before_begin, Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(context, ViewEventActivity.class);
                        context.startActivity(myIntent);
                        finish();
                    }
                    if (Calendar.getInstance().getTime().before(cldr_end.getTime()) || Calendar.getInstance().getTime().equals(cldr_end.getTime())) {
                        Toast.makeText(getApplicationContext(), R.string.rate_before_end, Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(context, ViewEventActivity.class);
                        context.startActivity(myIntent);
                        finish();
                    }
                    event_type = Objects.requireNonNull(Objects.requireNonNull(docData).get("event_type")).toString();
                    rate_event_list_entry_banner_text.setText(Objects.requireNonNull(docData.get("event_name")).toString());
                    rate_event_list_entry_banner_text.setCompoundDrawablesWithIntrinsicBounds(EventTypeToDrawable.getEventTypeToDrawable(event_type), 0, 0, 0);

                    rate_event_list_entry_banner_card.setOnClickListener(view -> {
                        Intent myIntent = new Intent(context, ViewEventActivity.class);
                        context.startActivity(myIntent);
                    });

                    organizer = Objects.requireNonNull(Objects.requireNonNull(docData).get("organizer")).toString();
                    if (organizer.equals(userID)) {
                        organizerBanner.setVisibility(View.GONE);
                    }
                    if (!user_participated && !organizer.equals(userID)) {
                        Toast.makeText(getApplicationContext(), R.string.not_participate_rate, Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(context, ViewEventActivity.class);
                        context.startActivity(myIntent);
                        finish();
                    }
                    getOrganizerData();
                } else {
                    Intent myIntent = new Intent(context, MyProfileActivity.class);
                    startActivity(myIntent);
                    finish();
                    //Log.d(TAG, "No such document");
                }
            } else {
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                startActivity(myIntent);
                finish();
                //Log.d(TAG, "No such document");
            }
        });
    }

    private void getOrganizerData() {
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (userID.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        if (eventID.equals("")) {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        DocumentReference docRef = db.collection("users").document(organizer);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    organizerUsername.setText(Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());

                    circleImageView.setOnClickListener(view -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("friendID", organizer);
                        editor.apply();
                        Intent myIntent = new Intent(context, ViewProfileActivity.class);
                        startActivity(myIntent);
                    });
                    String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + organizer);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(circleImageView.getContext().getApplicationContext()).asBitmap().load(bytes).into(circleImageView)).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    }
                } else {
                    Intent myIntent = new Intent(context, MyProfileActivity.class);
                    startActivity(myIntent);
                    finish();
                    //Log.d(TAG, "No such document");
                }
            } else {
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                startActivity(myIntent);
                finish();
                //Log.d(TAG, "No such document");
            }
        });
    }


    private void updatePlayer(int index) {
        String userID = preferences.getString("userID", "");
        if (userID.equals(people.get(index))) {
            return;
        }
        DocumentReference docRef = db.collection("users").document(people.get(index));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (organizer.equals(document.getId()) && !userID.equals(organizer)) {
                        float value = Float.parseFloat(Objects.requireNonNull(Objects.requireNonNull(data).get("points_rank")).toString());
                        value += ratingBarOrganizer.getRating();
                        data.put("points_rank", value);
                    }
                    String area_string = Objects.requireNonNull(Objects.requireNonNull(data).get("areas_of_interest")).toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        List<String> areas_array = new ArrayList<>();
                        Collections.addAll(areas_array, area_string_array);
                        String points_string = Objects.requireNonNull(data.get("points_levels")).toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        List<Float> points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Float.parseFloat(s));
                        }
                        if (areas_array.contains(event_type)) {
                            points_array.set(areas_array.indexOf(event_type), points_array.get(areas_array.indexOf(event_type)) + ratings.get(index));
                        } else {
                            areas_array.add(event_type);
                            points_array.add(ratings.get(index));
                        }
                        data.put("areas_of_interest", areas_array);
                        data.put("points_levels", points_array);
                    } else {
                        List<String> areas_array = new ArrayList<>();
                        List<Float> points_array = new ArrayList<>();
                        areas_array.add(event_type);
                        points_array.add(ratings.get(index));
                        data.put("areas_of_interest", areas_array);
                        data.put("points_levels", points_array);
                    }
                    docRef.set(data);
                    if (index != people.size() - 1) {
                        updatePlayer(index + 1);
                    } else {
                        if (!people.contains(organizer) && !userID.equals(organizer)) {
                            updateOrganizer();
                        } else {
                            checkRated();
                        }
                    }
                }
            }
        });
    }

    private void updateOrganizer() {
        String userID = preferences.getString("userID", "");
        if (userID.equals(organizer)) {
            return;
        }
        DocumentReference docRef = db.collection("users").document(organizer);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (organizer.equals(document.getId()) && !userID.equals(organizer)) {
                        float value = Float.parseFloat(Objects.requireNonNull(Objects.requireNonNull(data).get("points_rank")).toString());
                        value += ratingBarOrganizer.getRating();
                        data.put("points_rank", value);
                    }
                    docRef.set(Objects.requireNonNull(data));
                    checkRated();
                }
            }
        });
    }
}