package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfile extends AppCompatActivity {
    Context context = this;
    ImageView friendRequest = findViewById(R.id.imageViewSendFriendRequest);
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        fillUserData();
        friendRequest.setOnClickListener(view -> {
            sendFriendRequest();
        });
    }

    void sendFriendRequest() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sender = preferences.getString("userID", "");
        String receiver = preferences.getString("friendID", "");
        Map<String, Object> docData = new HashMap<>();
        docData.put("sender", sender);
        docData.put("receiver", receiver);
        db.collection("friend_requests")
                .whereEqualTo("sender", sender)
                .whereEqualTo("receiver", receiver)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0){
                        Snackbar.make(friendRequest, R.string.alerady_sent, Snackbar.LENGTH_SHORT).show();
                    } else {
                        db.collection("friend_requests")
                                .add(docData)
                                .addOnSuccessListener(aVoid -> {
                                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                                    Snackbar.make(friendRequest, R.string.write_success, Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(friendRequest, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                                    //Log.w(TAG, "Error writing document", e);
                                });
                    }
                }
            }
        });
    }

    void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("friendID", "");
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
                    String text_rank_points = points_rank + "/" + upper_bound;
                    myOrganizerRank.setText(text_rank);
                    myOrganizerRankPoints.setText(text_rank_points);
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
                    myDescription.setText(Objects.requireNonNull(data.get("description")).toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
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