package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequests extends AppCompatActivity {
    RecyclerView recyclerViewFriendRequests;
    FriendRequests context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
    }
    void refresh() {
        Intent myIntent = new Intent(context, FriendRequests.class);
        context.startActivity(myIntent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        getFriendRequests();
    }

    void getFriendRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String receiver = preferences.getString("userID", "");
        Map<String, Object> docData = new HashMap<>();
        docData.put("receiver", receiver);
        List<String> friends_array = new ArrayList<>();
        db.collection("friend_requests")
                .whereEqualTo("receiver", receiver)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            friends_array.add(document.get("sender").toString());
                        }
                        CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(friends_array, receiver, context);
                        recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                    }
                }
            }
        });
    }

}
