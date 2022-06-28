package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterFriendsMessages;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyMessagesActivity extends MenuInterfaceActivity {
    private Context context;
    private RecyclerView recyclerViewMessages;
    private List<String> friends_array;
    private List<String> old_friends_array;
    private TextView noResults;
    private Handler handlerForAlarm;
    private Runnable runnable;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);
        context = this;
        first = true;
        noResults = findViewById(R.id.noResults);
        friends_array = new ArrayList<>();
        old_friends_array = new ArrayList<>();
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        getAllFriends();
        countDownStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        first = true;
        getAllFriends();
        countDownStart();
    }

    public void countDownStart() {
        handlerForAlarm = new Handler();
        runnable = () -> {
            handlerForAlarm.postDelayed(runnable, 10000);
            try {
                getAllFriends();
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

    private void getAllFriendsReverse(String me) {
        db.collection("messages").whereEqualTo("receiver", me).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                if (task1.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task1.getResult()) {
                        String sender = Objects.requireNonNull(document.getData().get("sender")).toString();
                        if (!friends_array.contains(sender) && !sender.equals(me)) {
                            friends_array.add(sender);
                        }
                    }
                }
            }
            boolean equal = true;
            if (old_friends_array.size() != friends_array.size()) {
                equal = false;
            } else {
                for (int i = 0, n = old_friends_array.size(); i < n; i++) {
                    if (!old_friends_array.get(i).equals(friends_array.get(i))) {
                        equal = false;
                        break;
                    }
                }
            }
            if (!equal || first) {
                CustomAdapterFriendsMessages customAdapterFriends = new CustomAdapterFriendsMessages(friends_array, context, preferences);
                recyclerViewMessages.setAdapter(customAdapterFriends);
                if (friends_array.size() > 0) {
                    noResults.setVisibility(View.GONE);
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }
            first = false;
            old_friends_array = new ArrayList<>(friends_array);
        });
    }

    private void getAllFriends() {
        friends_array = new ArrayList<>();
        String me = preferences.getString("userID", "");
        if (me.equals("")) {
            handlerForAlarm.removeCallbacksAndMessages(runnable);
            handlerForAlarm.removeCallbacksAndMessages(runnable);
            handlerForAlarm.removeCallbacksAndMessages(null);
            handlerForAlarm.removeCallbacks(runnable);
            handlerForAlarm.removeCallbacks(null);
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
        db.collection("messages").whereEqualTo("sender", me).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String receiver = Objects.requireNonNull(document.getData().get("receiver")).toString();
                        if (!friends_array.contains(receiver) && !receiver.equals(me)) {
                            friends_array.add(receiver);
                        }
                    }
                }
            }
            getAllFriendsReverse(me);
        });
    }
}