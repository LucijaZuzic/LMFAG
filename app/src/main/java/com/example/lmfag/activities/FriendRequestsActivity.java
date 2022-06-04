package com.example.lmfag.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriendRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendRequestsActivity extends MenuInterfaceActivity {
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsActivity context;
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        context = this;
        
        noResults = findViewById(R.id.noResults);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
    }

    public void refresh() {
        Intent myIntent = new Intent(context, FriendRequestsActivity.class);
        context.startActivity(myIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFriendRequests();
    }


    private void getFriendRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String receiver = preferences.getString("userID", "");
        List<String> friends_array = new ArrayList<>();
        db.collection("friend_requests")
                .whereEqualTo("receiver", receiver)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                friends_array.add(Objects.requireNonNull(document.get("sender")).toString());
                            }
                            CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(friends_array, receiver, context);
                            recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                    } else {
                        noResults.setVisibility(View.VISIBLE);
                    }
                });
    }
}
