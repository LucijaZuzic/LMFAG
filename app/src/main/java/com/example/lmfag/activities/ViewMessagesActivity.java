package com.example.lmfag.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterMessages;
import com.example.lmfag.utility.DrawerHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);
        DrawerHelper.fillNavbarData(this);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        getFriendData();
        getAllMessages();
        ImageView iv = findViewById(R.id.imageViewSend);
        iv.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> docData = new HashMap<>();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String sender = preferences.getString("userID", "");
            String receiver = preferences.getString("friendID", "");
            EditText editTextMessage = findViewById(R.id.editTextMessage);
            docData.put("sender", sender);
            docData.put("receiver", receiver);
            docData.put("messages", editTextMessage.getText().toString());
            docData.put("timestamp", Timestamp.now());
            db.collection("messages").add(docData);
            getFriendData();
            getAllMessages();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        getFriendData();
        getAllMessages();
    }

    public void getFriendData( ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("friendID", "");
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    TextView myUsername = findViewById(R.id.textViewUsernameFriend);
                    myUsername.setText(data.get("username").toString());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                        findViewById(R.id.profile_image).setOnClickListener(view -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID", name);
                            editor.apply();
                            Intent myIntent = new Intent(context, ViewProfileActivity.class);
                            startActivity(myIntent);
                        });
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    //Log.d(TAG, "No such document");
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public void getAllMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> messages = new ArrayList<>();
        List<String> times = new ArrayList<>();
        List<String> sender = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String me = preferences.getString("userID", "");
        String other = preferences.getString("friendID", "");
        if (me.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        if (other.equals(me)) {
            Snackbar.make(recyclerViewMessages, R.string.visiting_myself, Snackbar.LENGTH_SHORT).show();
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            return;
        }
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
                    final Bitmap[] myImage = new Bitmap[1];
                    final Bitmap[] otherImage = new Bitmap[1];
                    final String[] myUsername = new String[1];
                    final String[] otherUsername = new String[1];
                    String otherUser = "";
                    for (String senderOther: sender) {
                        if (!senderOther.equals(me)) {
                            otherUser = senderOther;
                            break;
                        }
                    }
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("users").document(me);
                    String finalOtherUser = otherUser;
                    docRef.get().addOnCompleteListener(taskUser -> {
                        if (taskUser.isSuccessful()) {
                            DocumentSnapshot document = taskUser.getResult();
                            if (document.exists()) {
                                Map<String, Object> data = document.getData();
                                myUsername[0] = data.get("username").toString();
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                                final long ONE_MEGABYTE = 1024 * 1024;
                                imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    myImage[0] = bmp;
                                    DocumentReference docRefOther = db.collection("users").document(finalOtherUser);
                                    docRefOther.get().addOnCompleteListener(taskNew -> {
                                        if (taskNew.isSuccessful()) {
                                            DocumentSnapshot documentNew = taskNew.getResult();
                                            if (documentNew.exists()) {
                                                Map<String, Object> dataNew = documentNew.getData();
                                                otherUsername[0] = dataNew.get("username").toString();
                                                StorageReference imagesRefNew = storageRef.child("profile_pictures/" + documentNew.getId());
                                                imagesRefNew.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytesNew -> {
                                                    // Data for "images/island.jpg" is returns, use this as needed
                                                    Bitmap bmpNew = BitmapFactory.decodeByteArray(bytesNew, 0, bytesNew.length);
                                                    otherImage[0] = bmpNew;
                                                    CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername[0], otherUsername[0], myImage[0], otherImage[0]);
                                                    recyclerViewMessages.setAdapter(customAdapter);
                                                    recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Handle any errors
                                                        CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername[0], otherUsername[0], myImage[0], otherImage[0]);
                                                        recyclerViewMessages.setAdapter(customAdapter);
                                                        recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                                                    }
                                                });
                                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                            } else {

                                            }
                                        } else {
                                            //Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    });
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Data for "images/island.jpg" is returns, use this as needed
                                        DocumentReference docRefOther = db.collection("users").document(finalOtherUser);
                                        docRefOther.get().addOnCompleteListener(taskNew -> {
                                            if (taskNew.isSuccessful()) {
                                                DocumentSnapshot documentNew = taskNew.getResult();
                                                if (documentNew.exists()) {
                                                    Map<String, Object> dataNew = documentNew.getData();
                                                    otherUsername[0] = dataNew.get("username").toString();
                                                    StorageReference imagesRefNew = storageRef.child("profile_pictures/" + documentNew.getId());
                                                    imagesRefNew.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytesNew -> {
                                                        // Data for "images/island.jpg" is returns, use this as needed
                                                        Bitmap bmpNew = BitmapFactory.decodeByteArray(bytesNew, 0, bytesNew.length);
                                                        otherImage[0] = bmpNew;
                                                        CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername[0], otherUsername[0], myImage[0], otherImage[0]);
                                                        recyclerViewMessages.setAdapter(customAdapter);
                                                        recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            // Handle any errors
                                                            CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername[0], otherUsername[0], myImage[0], otherImage[0]);
                                                            recyclerViewMessages.setAdapter(customAdapter);
                                                            recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                                                        }
                                                    });
                                                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                } else {

                                                }
                                            } else {
                                                //Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        });
                                    }
                                });
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {

                            }
                        } else {
                            //Log.d(TAG, "get failed with ", task.getException());
                        }
                    });

                } else {
                    Object ngs = task.getException();
                    String sd = ngs.toString();
                    Log.d("ERROR", "get failed with ", task.getException());

                }
            }
        });
    }
}