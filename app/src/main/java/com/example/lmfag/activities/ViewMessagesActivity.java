package com.example.lmfag.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lmfag.R;
import com.example.lmfag.utility.MySwipe;
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
    private Bitmap myImage, otherImage;
    private CircleImageView circleImageView;
    private TextView usernameFriend;
    private TextView noResults;
    private StorageReference storageRef;

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
        ImageView imageViewSend = findViewById(R.id.imageViewSend);
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

                    myUsername = Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    String imageView = preferences.getString("showImage", "true");
                    if (myImage == null && imageView.equals("true")) {
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(circleImageView.getContext().getApplicationContext())
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
                                }))).addOnFailureListener(exception -> {
                            // Handle any errors
                            getFriendData();
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
        }).addOnFailureListener(exception -> {
            // Handle any errors
            getFriendData();
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

                    otherUsername = Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString();
                    usernameFriend.setText(otherUsername);
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + document.getId());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    String imageView = preferences.getString("showImage", "true");
                    if (otherImage == null && imageView.equals("true")) {
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(getApplicationContext())
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
                                }))).addOnFailureListener(exception -> {
                            // Handle any errors
                            circleImageView.setImageBitmap(otherImage);
                            getAllMessages();
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
        }).addOnFailureListener(exception -> {
            // Handle any errors
            circleImageView.setImageBitmap(otherImage);
            getAllMessages();
        });
    }

    private void messageDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    getAllMessages();

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.refresh_messages).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();

    }

    public void getAllMessages() {
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
                        Collections.reverse(times);
                        Collections.reverse(sender);
                        Collections.reverse(ids);
                        CustomAdapterMessages customAdapter = new CustomAdapterMessages(messages, times, sender, ids, me, context, myUsername, otherUsername, myImage, otherImage);
                        if (messages.size() > 0) {
                            noResults.setVisibility(View.GONE);
                        } else {
                            noResults.setVisibility(View.VISIBLE);
                        }
                        recyclerViewMessages.setAdapter(customAdapter);
                        /* Testing addOnScrollListener, had an error recyclerViewMessages.addOnScrollListener(new RecyclerView.OnScrollListener()  {
                            @Override
                            public void onScrolled (RecyclerView recyclerView, int dx, int dy) {
                            // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
                            View view = (View) recyclerViewMessages.getChildAt( recyclerViewMessages.getAdapter().getItemCount()-1);

                            // Calculate the scrollDiff
                            int diff = (view.getBottom()-(recyclerViewMessages.getHeight()+recyclerViewMessages.getScrollY()));

                            // if diff is zero, then the bottom has been reached
                            if( diff == 0 )
                            {
                                // notify that we have reached the bottom
                                messageDialog();
                            }
                            }
                        });*/
                        recyclerViewMessages.setOnTouchListener(new MySwipe(context) {
                            public void onSwipeTop() {
                            }

                            public void onSwipeRight() {
                                messageDialog();
                            }

                            public void onSwipeLeft() {
                                messageDialog();
                            }

                            public void onSwipeBottom() {
                            }
                        });
                        recyclerViewMessages.scrollToPosition(customAdapter.getItemCount() - 1);
                    }
                });
    }

}