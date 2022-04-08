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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyMessages extends MenuInterface {
    Context context = this;
    RecyclerView recyclerViewMessages;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);
        DrawerHelper.fillNavbarData(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        getAllFriends();
    }


    void getAllFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> friends_array = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String me = preferences.getString("userID", "");
        if (me.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
        }
        db.collection("messages").whereEqualTo("sender", me).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String receiver = document.getData().get("receiver").toString();
                            if (!friends_array.contains(receiver) && !receiver.equals(me)) {
                                friends_array.add(receiver);
                            }
                        }
                        db.collection("messages").whereEqualTo("receiver", me).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String sender = document.getData().get("sender").toString();
                                            if (!friends_array.contains(sender) && !sender.equals(me)) {
                                                friends_array.add(sender);
                                            }
                                        }
                                        CustomAdapterFriendsMessages customAdapterFriends = new CustomAdapterFriendsMessages(friends_array, context, preferences);
                                        recyclerViewMessages.setAdapter(customAdapterFriends);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}