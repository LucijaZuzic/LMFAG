package com.example.lmfag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriendRequest;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendRequestsActivity extends MenuInterfaceActivity {
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsActivity context;
    private TextView noResults;
    private List<String> received, sent;
    private Chip sentChip, receivedChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        context = this;
        received = new ArrayList<>();
        sent = new ArrayList<>();
        noResults = findViewById(R.id.noResults);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
        sentChip = findViewById(R.id.sent);
        receivedChip = findViewById(R.id.received);

        getFriendRequests();
        sentChip.setOnClickListener(view -> {
            receivedChip.setChecked(!sentChip.isChecked());
            checkChange();
        });
        receivedChip.setOnClickListener(view -> {
            sentChip.setChecked(!receivedChip.isChecked());
            checkChange();
        });
    }

    public void checkChange() {
        String me = preferences.getString("userID", "");
        if (receivedChip.isChecked()) {
            CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(received, me, context);
            recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
            if (received.size() > 0) {
                noResults.setVisibility(View.GONE);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        } else {
            CustomAdapterFriends customAdapterFriendRequest = new CustomAdapterFriends(sent, context, preferences);
            recyclerViewFriendRequests.setAdapter(customAdapterFriendRequest);
            if (sent.size() > 0) {
                noResults.setVisibility(View.GONE);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        }
    }

    public void refresh() {
        Intent myIntent = new Intent(context, FriendRequestsActivity.class);
        context.startActivity(myIntent);
        finish();
    }

    private void getSentFriendRequests() {
        String me = preferences.getString("userID", "");
        db.collection("friend_requests")
                .whereEqualTo("sender", me)
                .get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        if (task2.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                sent.add(Objects.requireNonNull(document2.getData().get("receiver")).toString());
                            }
                        }
                    }
                    checkChange();
                });
    }

    private void getFriendRequests() {
        String me = preferences.getString("userID", "");
        received = new ArrayList<>();
        sent = new ArrayList<>();
        db.collection("friend_requests")
                .whereEqualTo("receiver", me)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                received.add(Objects.requireNonNull(document.getData().get("sender")).toString());
                            }
                        }
                    }
                    getSentFriendRequests();
                });
    }
}
