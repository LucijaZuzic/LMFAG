package com.example.lmfag.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMessagesActivity extends MenuInterfaceActivity {
    private ViewMessagesActivity context = this;
    private RecyclerView recyclerViewMessages;
    private FirebaseFirestore db;
    private List<String> messages = new ArrayList<>();
    private List<String> times = new ArrayList<>();
    private List<String> sender = new ArrayList<>();
    private List<String> ids = new ArrayList<>();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String me, other, myUsername, otherUsername;
    private Bitmap myImage, otherImage;
    private ImageView imageViewSend;
    private CircleImageView circleImageView;
    private TextView usernameFriend;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        me = preferences.getString("userID", "");
        other = preferences.getString("friendID", "");
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        circleImageView = findViewById(R.id.profile_image);
        circleImageView.setOnClickListener(view -> {
            editor.putString("friendID", other);
            editor.apply();
            Intent myIntent = new Intent(context, ViewProfileActivity.class);
            startActivity(myIntent);
            return;
        });
        usernameFriend = findViewById(R.id.textViewUsernameFriend);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        imageViewSend = findViewById(R.id.imageViewSend);
        imageViewSend.setOnClickListener(view -> {
            Map<String, Object> docData = new HashMap<>();
            EditText editTextMessage = findViewById(R.id.editTextMessage);
            docData.put("sender", me);
            docData.put("receiver", other);
            docData.put("messages", editTextMessage.getText().toString());
            docData.put("timestamp", Timestamp.now());
            db.collection("messages").add(docData);
            getMyData();
        });
        getMyData();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getMyData() {
        if (me.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        if (other.equals(me)) {
            Toast.makeText(getApplicationContext(), R.string.visiting_myself, Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
        if (myImage != null && myUsername != null) {
            getFriendData();
            return;
        }
        DocumentReference docRef = db.collection("users").document(me);
        docRef.get().addOnCompleteListener(taskUser -> {
            if (taskUser.isSuccessful()) {
                DocumentSnapshot document = taskUser.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    myUsername = data.get("username").toString();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    if (myImage == null) {
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                            // Data for "images/island.jpg" is returns, use this as needed

                            Glide.with(circleImageView.getContext().getApplicationContext())
                                    .asBitmap()
                                    .load(bytes)
                                    .into((new CustomTarget<Bitmap>() {

                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            myImage = resource;
                                            getFriendData();
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    }));
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                getFriendData();
                            }
                        });
                    } else {
                        getFriendData();
                    }
                } else {
                    getFriendData();
                }
            } else {
                getFriendData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                getFriendData();
            }
        });
    }

    public void getFriendData() {
        if (otherUsername != null && otherImage != null) {
            getAllMessages();
            usernameFriend.setText(otherUsername);
            circleImageView.setImageBitmap(otherImage);
            return;
        }
        DocumentReference docRef = db.collection("users").document(other);
        docRef.get().addOnCompleteListener(taskUser -> {
            if (taskUser.isSuccessful()) {
                DocumentSnapshot document = taskUser.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    otherUsername = data.get("username").toString();
                    usernameFriend.setText(otherUsername);
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    if (otherImage == null) {
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                            // Data for "images/island.jpg" is returns, use this as needed
                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(bytes)
                                    .into((new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            otherImage = resource;
                                            circleImageView.setImageBitmap(otherImage);
                                            getAllMessages();
                                        }
                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    }));
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                circleImageView.setImageBitmap(otherImage);
                                getAllMessages();
                            }
                        });
                    } else {
                        circleImageView.setImageBitmap(otherImage);
                        getAllMessages();
                    }
                } else {
                    circleImageView.setImageBitmap(otherImage);
                    getAllMessages();
                }
            } else {
                circleImageView.setImageBitmap(otherImage);
                getAllMessages();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                circleImageView.setImageBitmap(otherImage);
                getAllMessages();
            }
        });
    }

    public void getAllMessages() {
        messages = new ArrayList<>();
        times = new ArrayList<>();
        sender = new ArrayList<>();
        ids = new ArrayList<>();
        db.collection("messages")
                .whereIn("receiver", Arrays.asList(me, other))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> map = document.getData();
                            if (map.get("sender").toString().equals(me) || map.get("sender").toString().equals(other)) {
                                messages.add(map.get("messages").toString());
                                Timestamp start_timestamp = (Timestamp) (map.get("timestamp"));
                                Date start_date = start_timestamp.toDate();
                                Calendar cldr_start = Calendar.getInstance();
                                cldr_start.setTime(start_date);
                                times.add(DateFormat.getDateTimeInstance().format(cldr_start.getTime()));
                                sender.add(map.get("sender").toString());
                                ids.add(document.getId());

                            }
                        }
                    }
                    Collections.reverse(messages);
                    Collections.reverse(times);
                    Collections.reverse(sender);
                    Collections.reverse(ids);
                    CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername, otherUsername, myImage, otherImage);
                    recyclerViewMessages.setAdapter(customAdapter);
                    recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                }
            }
                });
    }

}