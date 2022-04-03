package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindFriends extends AppCompatActivity {
    Context context = this;
    RecyclerView recyclerViewFindFriends;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewFindFriends = findViewById(R.id.recyclerViewFriends);
        getAllFriends();
    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(context, MyProfile.class);
        context.startActivity(myIntent);
    }
    void getAllFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> friends_array = new ArrayList<>();
        db.collection("users").orderBy("username").limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(!(document.getId().equals(preferences.getString("userID", "")))) {
                                friends_array.add(document.getId());
                            }
                        }
                        CustomAdapterFriends customAdapterFriends = new CustomAdapterFriends(friends_array, context, preferences);
                        recyclerViewFindFriends.setAdapter(customAdapterFriends);
                    }
                }
            }
        });
    }

}