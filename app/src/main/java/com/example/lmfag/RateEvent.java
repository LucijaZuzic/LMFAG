package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RateEvent extends AppCompatActivity {
    String organizer;
    private List<String> people;
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_event);
    }
    @Override
    public void onResume() {
        super.onResume();
        getWhoAttended();
        fillData();
    }

    void getWhoAttended() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        if (eventID.equals("")) {
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
                            people.add(map.get("user").toString());
                        }
                    }
                    //CustomAdapterRating customAdapter = new CustomAdapterRating(people);
                    //recyclerViewRating.setAdapter(customAdapter);
                } else {
                    Object ngs = task.getException();
                    String sd = ngs.toString();
                    Log.d("ERROR", "get failed with ", task.getException());

                }
            }
        });

    }
        void fillData() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String eventID = preferences.getString("eventID", "");
            String userID = preferences.getString("userID", "");
            if (eventID.equals("")) {
                return;
            }
            DocumentReference docRef = db.collection("events").document(eventID);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> docData = document.getData();
                        organizer = docData.get("organizer").toString();
                        getOrganizerData(organizer);

                    }
                }
            });
        }
        void getOrganizerData(String name) {
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
                            CircleImageView circleImageView = findViewById(R.id.profile_image);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            circleImageView.setImageBitmap(bmp);
                            findViewById(R.id.profile_image).setOnClickListener(view -> {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("friendID", name);
                                editor.apply();
                                Intent myIntent = new Intent(context, ViewProfile.class);
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

}