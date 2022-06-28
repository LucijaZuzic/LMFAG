package com.example.lmfag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterMessages;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMessagesActivity extends MenuInterfaceActivity {
    private ViewMessagesActivity context;
    private RecyclerView recyclerViewMessages;
    private List<String> messages = new ArrayList<>();
    private List<String> times = new ArrayList<>();
    private List<String> sender = new ArrayList<>();
    private List<String> ids = new ArrayList<>();
    private String me, other, myUsername, otherUsername;
    //private Bitmap myImage, otherImage;
    private CircleImageView circleImageView;
    private TextView usernameFriend;
    private TextView noResults;
    private StorageReference storageRef;
    private Handler handlerForAlarm;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);
        context = this;
        me = preferences.getString("userID", "");
        other = preferences.getString("friendID", "");
        storageRef = storage.getReference();
        circleImageView = findViewById(R.id.profile_image);
        noResults = findViewById(R.id.noResults);
        circleImageView.setOnClickListener(view -> {
            editor.putString("friendID", other);
            editor.apply();
            Intent myIntent = new Intent(context, ViewProfileActivity.class);
            startActivity(myIntent);
            finish();
        });
        usernameFriend = findViewById(R.id.textViewUsernameFriend);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        ImageView imageViewSend = findViewById(R.id.imageViewSend);
        imageViewSend.setOnClickListener(view -> {
            Map<String, Object> docData = new HashMap<>();
            EditText editTextMessage = findViewById(R.id.editTextMessage);
            docData.put("sender", me);
            docData.put("receiver", other);
            docData.put("messages", editTextMessage.getText().toString());
            docData.put("timestamp", Timestamp.now());
            db.collection("messages").add(docData);
            editTextMessage.setText("");
            editTextMessage.requestFocus();
            getMyData();
        });
        getMyData();
        countDownStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMyData();
    }

    public void getMyData() {
        if (me.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        if (other.equals(me)) {
            Toast.makeText(getApplicationContext(), R.string.visiting_myself, Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        DocumentReference docRef = db.collection("users").document(me);
        docRef.get().addOnCompleteListener(taskUser -> {
            if (taskUser.isSuccessful()) {
                DocumentSnapshot document = taskUser.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    myUsername = Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString();

                }
            }
            getFriendData();
        }).addOnFailureListener(taskUser -> getFriendData());
    }

    public void getFriendData() {
        DocumentReference docRef = db.collection("users").document(other);
        docRef.get().addOnCompleteListener(taskUser -> {
            if (taskUser.isSuccessful()) {
                DocumentSnapshot document = taskUser.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    otherUsername = Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString();
                    usernameFriend.setText(otherUsername);
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes ->
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(bytes)
                                        .into(circleImageView));
                    }
                }
            }
            getAllMessages();
        }).addOnFailureListener(taskUser -> getAllMessages());
    }

    public void getAllMessages() {
        List<String> old_messages = new ArrayList<>(messages);
        messages = new ArrayList<>();
        times = new ArrayList<>();
        sender = new ArrayList<>();
        ids = new ArrayList<>();
        db.collection("messages")
                .whereIn("receiver", Arrays.asList(me, other))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> map = document.getData();
                                if (Objects.requireNonNull(map.get("sender")).toString().equals(me) || Objects.requireNonNull(map.get("sender")).toString().equals(other)) {
                                    messages.add(Objects.requireNonNull(map.get("messages")).toString());
                                    Timestamp start_timestamp = (Timestamp) (map.get("timestamp"));
                                    Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                                    Calendar cldr_start = Calendar.getInstance();
                                    cldr_start.setTime(start_date);
                                    times.add(DateFormat.getDateTimeInstance().format(cldr_start.getTime()));
                                    sender.add(Objects.requireNonNull(map.get("sender")).toString());
                                    ids.add(document.getId());
                                }
                            }
                        }
                        Collections.reverse(messages);
                        boolean changed = false;
                        if (messages.size() != old_messages.size()) {
                            changed = true;
                        } else {
                            for (int i = messages.size() - 1; i >= 0; i--) {
                                if (!messages.get(i).equals(old_messages.get(i))) {
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        if (changed) {
                            Collections.reverse(times);
                            Collections.reverse(sender);
                            Collections.reverse(ids);
                            CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername, otherUsername);
                            if (messages.size() > 0) {
                                noResults.setVisibility(View.GONE);
                            } else {
                                noResults.setVisibility(View.VISIBLE);
                            }
                            recyclerViewMessages.setAdapter(customAdapter);

                            recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                        }
                    }
                });
    }

    public void countDownStart() {
        handlerForAlarm = new Handler();
        runnable = () -> {
            handlerForAlarm.postDelayed(runnable, 10000);
            try {
                getMyData();
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
}