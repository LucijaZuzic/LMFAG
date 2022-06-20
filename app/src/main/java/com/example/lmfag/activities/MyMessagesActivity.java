package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
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
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);
        context = this;
        
        noResults = findViewById(R.id.noResults);
        friends_array = new ArrayList<>();
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        getAllFriends();
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
            CustomAdapterFriendsMessages customAdapterFriends = new CustomAdapterFriendsMessages(friends_array, context, preferences);
            recyclerViewMessages.setAdapter(customAdapterFriends);
            if (friends_array.size() > 0) {
                noResults.setVisibility(View.GONE);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAllFriends() {
        friends_array = new ArrayList<>();
        String me = preferences.getString("userID", "");
        if (me.equals("")) {
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