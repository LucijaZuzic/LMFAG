package com.example.lmfag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriendRequest;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendRequestsActivity extends MenuInterfaceActivity {
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestsActivity context;
    private TextView noResults;
    private List<String> received, sent, old_sent, old_received;
    private Chip sentChip, receivedChip;
    private Handler handlerForAlarm;
    private Runnable runnable;
    private boolean first = true;
    private boolean previous_option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        context = this;
        first = true;
        received = new ArrayList<>();
        sent = new ArrayList<>();
        old_received = new ArrayList<>();
        old_sent = new ArrayList<>();
        noResults = findViewById(R.id.noResults);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
        sentChip = findViewById(R.id.sent);
        receivedChip = findViewById(R.id.received);
        previous_option = sentChip.isChecked();

        getFriendRequests();
        countDownStart();
        sentChip.setOnClickListener(view -> {
            receivedChip.setChecked(!sentChip.isChecked());
            checkChange();
        });
        receivedChip.setOnClickListener(view -> {
            sentChip.setChecked(!receivedChip.isChecked());
            checkChange();
        });
    }

    public void countDownStart() {
        handlerForAlarm = new Handler();
        runnable = () -> {
            handlerForAlarm.postDelayed(runnable, 10000);
            try {
                getFriendRequests();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        handlerForAlarm.postDelayed(runnable, 10000);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handlerForAlarm != null) {
            handlerForAlarm.removeCallbacksAndMessages(runnable);
            handlerForAlarm.removeCallbacksAndMessages(handlerForAlarm);
            handlerForAlarm.removeCallbacksAndMessages(null);
            handlerForAlarm.removeCallbacks(runnable);
            handlerForAlarm.removeCallbacks(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handlerForAlarm != null) {
            handlerForAlarm.removeCallbacksAndMessages(runnable);
            handlerForAlarm.removeCallbacksAndMessages(handlerForAlarm);
            handlerForAlarm.removeCallbacksAndMessages(null);
            handlerForAlarm.removeCallbacks(runnable);
            handlerForAlarm.removeCallbacks(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handlerForAlarm != null) {
            handlerForAlarm.removeCallbacksAndMessages(runnable);
            handlerForAlarm.removeCallbacksAndMessages(handlerForAlarm);
            handlerForAlarm.removeCallbacksAndMessages(null);
            handlerForAlarm.removeCallbacks(runnable);
            handlerForAlarm.removeCallbacks(null);
        }
    }

    public void checkChange() {
        boolean current_option = sentChip.isChecked();
        String me = preferences.getString("userID", "");
        if (receivedChip.isChecked()) {
            boolean equal = true;
            if (old_received.size() != received.size()) {
                equal = false;
            } else {
                for (int i = 0, n = old_received.size(); i < n; i++) {
                    if (!old_received.get(i).equals(received.get(i))) {
                        equal = false;
                        break;
                    }
                }
            }
            if (!equal || first || current_option != previous_option) {
                CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(received, me, context);
                recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                if (received.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }
            first = false;
        } else {
            boolean equal = true;
            if (old_sent.size() != sent.size()) {
                equal = false;
            } else {
                for (int i = 0, n = old_sent.size(); i < n; i++) {
                    if (!old_sent.get(i).equals(sent.get(i))) {
                        equal = false;
                        break;
                    }
                }
            }
            if (!equal || first || current_option != previous_option) {
                CustomAdapterFriendRequest customAdapterAreaOfInterest = new CustomAdapterFriendRequest(sent, me, context);
                recyclerViewFriendRequests.setAdapter(customAdapterAreaOfInterest);
                if (sent.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }
            first = false;
        }
        previous_option = current_option;
    }

    public void refresh() {
        Intent myIntent = new Intent(context, FriendRequestsActivity.class);
        context.startActivity(myIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        first = true;
        getFriendRequests();
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
        old_received = new ArrayList<>(received);
        old_sent = new ArrayList<>(sent);
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
