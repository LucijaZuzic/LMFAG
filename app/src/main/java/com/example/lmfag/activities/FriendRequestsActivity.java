package com.example.lmfag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriendRequest;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendRequestsActivity extends MenuInterfaceActivity {
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsActivity context;
    private TextView noResults;
    private List<String> received, sent;
    private SwitchCompat switchSentReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        context = this;
        received = new ArrayList<>();
        sent = new ArrayList<>();
        noResults = findViewById(R.id.noResults);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
        String me = preferences.getString("userID", "");
        switchSentReceived = findViewById(R.id.toggleReceivedSent);
        getFriendRequests();
        switchSentReceived.setOnClickListener(view -> {
            if (!switchSentReceived.isChecked()) {
                switchSentReceived.setText(R.string.received);
                switchSentReceived.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_inbox_24, 0, 0, 0);
                CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(received, me, context);
                recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                if (received.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            } else {
                switchSentReceived.setText(R.string.sent);
                switchSentReceived.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_send_24, 0, 0, 0);
                CustomAdapterFriends customAdapterFriendRequest = new CustomAdapterFriends(sent, context, preferences);
                recyclerViewFriendRequests.setAdapter(customAdapterFriendRequest);
                if (sent.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }
        });
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
                    if (!switchSentReceived.isChecked()) {
                        switchSentReceived.setText(R.string.received);
                        switchSentReceived.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_inbox_24, 0, 0, 0);
                        CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(received, me, context);
                        recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                        if (received.size() > 0) {
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                    } else {
                        switchSentReceived.setText(R.string.sent);
                        switchSentReceived.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_send_24, 0, 0, 0);
                        CustomAdapterFriends customAdapterFriendRequest = new CustomAdapterFriends(sent, context, preferences);
                        recyclerViewFriendRequests.setAdapter(customAdapterFriendRequest);
                        if (sent.size() > 0) {
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                    }
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
