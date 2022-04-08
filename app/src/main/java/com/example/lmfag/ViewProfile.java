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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfile extends MenuInterface {
    Context context = this;
    ImageView friendRequest;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerViewFriends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        DrawerHelper.fillNavbarData(this);
        showFriends();
        showAreasOfInterest();
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        friendRequest = findViewById(R.id.imageViewSendFriendRequest);
        friendRequest.setOnClickListener(view -> sendFriendRequest());
        ImageView message = findViewById(R.id.imageViewMyMesages);
        message.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, ViewMessages.class);
            context.startActivity(myIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillUserData();
        getFriends();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sender = preferences.getString("userID", "");
        String receiver = preferences.getString("friendID", "");
        checkFriendsOneDir(sender, receiver, false);
    }

    void refresh() {
        Intent myIntent = new Intent(context, ViewProfile.class);
        context.startActivity(myIntent);
    }
    void showFriends() {
        LinearLayout ll_friends_show = findViewById(R.id.linearLayoutShowFriends);
        RecyclerView ll_friends = findViewById(R.id.recyclerViewFriends);
        ImageView iv_friends = findViewById(R.id.imageViewExpandFriends);
        ll_friends_show.setOnClickListener(view -> {
            if (ll_friends.getVisibility() == View.GONE) {
                ll_friends.setVisibility(View.VISIBLE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_friends.setVisibility(View.GONE);
                iv_friends.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    void showAreasOfInterest() {
        LinearLayout ll_areas_show = findViewById(R.id.linearLayoutShowAreasOfInterest);
        RecyclerView ll_areas = findViewById(R.id.recyclerViewAreasOfInterest);
        ImageView iv_areas = findViewById(R.id.imageViewExpandAreasOfInterest);
        ll_areas_show.setOnClickListener(view -> {
            if (ll_areas.getVisibility() == View.GONE) {
                ll_areas.setVisibility(View.VISIBLE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                ll_areas.setVisibility(View.GONE);
                iv_areas.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        });
    }

    void writeToDb(String sender, String receiver) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("sender", sender);
        docData.put("receiver", receiver);
        db.collection("friend_requests")
        .add(docData)
        .addOnSuccessListener(aVoid -> {
            //Log.d(TAG, "DocumentSnapshot successfully written!");
            Snackbar.make(friendRequest, R.string.friend_request_sent, Snackbar.LENGTH_SHORT).show();
        })
        .addOnFailureListener(e -> {
            Snackbar.make(friendRequest, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
            //Log.w(TAG, "Error writing document", e);
        });
    }

    void checkFriendsTwoDir(String sender, String receiver, boolean send) {
        db.collection("friends").document(sender).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(receiver)) {
                        //Snackbar.make(friendRequest, R.string.already_friends, Snackbar.LENGTH_SHORT).show();
                        if (send) {
                            db.collection("friends").document(receiver).update("friends", FieldValue.arrayRemove(sender));
                            db.collection("friends").document(sender).update("friends", FieldValue.arrayRemove(receiver));
                            Snackbar.make(friendRequest, R.string.no_longer_friends, Snackbar.LENGTH_SHORT).show();
                            refresh();
                        } else {
                            friendRequest = findViewById(R.id.imageViewSendFriendRequest);
                            friendRequest.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_remove_24));
                        }
                    } else {
                        if (send) {
                            writeToDb(sender, receiver);
                        } else {
                            friendRequest = findViewById(R.id.imageViewSendFriendRequest);
                            friendRequest.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_add_24));
                        }
                    }
                } else {
                    if (send) {
                        writeToDb(sender, receiver);
                    } else {
                        friendRequest = findViewById(R.id.imageViewSendFriendRequest);
                        friendRequest.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_add_24));
                    }
                }
            }
        });
    }

    void checkFriendsOneDir(String sender, String receiver, boolean send) {
        db.collection("friends").document(receiver).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(sender)) {
                        //Snackbar.make(friendRequest, R.string.already_friends, Snackbar.LENGTH_SHORT).show();
                        if (send) {
                            db.collection("friends").document(receiver).update("friends", FieldValue.arrayRemove(sender));
                            db.collection("friends").document(sender).update("friends", FieldValue.arrayRemove(receiver));
                            Snackbar.make(friendRequest, R.string.no_longer_friends, Snackbar.LENGTH_SHORT).show();
                            refresh();
                        } else {
                            friendRequest = findViewById(R.id.imageViewSendFriendRequest);
                            friendRequest.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_remove_24));
                        }
                    } else {
                        checkFriendsTwoDir(sender, receiver, send);
                    }
                } else {
                    checkFriendsTwoDir(sender, receiver, send);
                }
            }
        });
    }
    void checkSentTwoDir(String sender, String receiver, boolean send) {
        db.collection("friend_requests").whereEqualTo("sender", receiver).whereEqualTo("receiver", sender)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            if (send) {
                                Snackbar.make(friendRequest, R.string.already_sent, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            checkFriendsOneDir(sender, receiver, send);
                        }
                    }
                });
    }
    void checkSentOneDir(String sender, String receiver, boolean send) {
        db.collection("friend_requests").whereEqualTo("sender", sender).whereEqualTo("receiver", receiver)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            if (send) {
                                Snackbar.make(friendRequest, R.string.already_sent, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            checkSentTwoDir(sender, receiver, send);
                        }
                    }
                });
    }

    void sendFriendRequest() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sender = preferences.getString("userID", "");
        String receiver = preferences.getString("friendID", "");
        if (sender.equals(receiver)) {
            Snackbar.make(friendRequest, R.string.friends_with_self, Snackbar.LENGTH_SHORT).show();
            return;
        }
        checkSentOneDir(sender, receiver, true);
    }

    void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("friendID", "");
        String me = preferences.getString("userID", "");
        if (name.equals(me)) {
            Snackbar.make(friendRequest, R.string.visiting_myself, Snackbar.LENGTH_SHORT).show();
            Intent myIntent = new Intent(context, MyProfile.class);
            startActivity(myIntent);
            return;
        }
        if (name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    TextView myUsername = findViewById(R.id.textViewUsername);
                    TextView myLocation = findViewById(R.id.textViewMyLocation);
                    TextView myDescription = findViewById(R.id.textViewMyDescription);
                    TextView myOrganizerRank = findViewById(R.id.textViewMyOrganizerRank);
                    TextView myOrganizerRankPoints = findViewById(R.id.textViewRankPoints);
                    RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                    myUsername.setText(data.get("username").toString());
                    myLocation.setText(data.get("location").toString());
                    Double points_rank = Double.parseDouble(data.get("points_rank").toString());
                    Integer rank = (int) (Math.floor(points_rank / 1000));
                    String text_rank = Integer.toString(rank);
                    Double upper_bound = Math.ceil(points_rank / 1000) * 1000;
                    if (upper_bound.equals(0.0)) {
                        upper_bound = 1000.0;
                    }
                    String text_rank_points = points_rank + "/" + upper_bound;
                    ProgressBar progressBar = findViewById(R.id.determinateBar);
                    progressBar.setProgress((int)((points_rank - (upper_bound - 1000)) / 10));
                    myOrganizerRank.setText(text_rank);
                    myOrganizerRankPoints.setText(text_rank_points);
                    String area_string = data.get("areas_of_interest").toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        List<String> areas_array = new ArrayList<>(Arrays.asList(area_string_array));
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        List<Double> points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Double.parseDouble(s));
                        }
                        CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
                    }
                    myDescription.setText(Objects.requireNonNull(data.get("description")).toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
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
    void getFriends() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("friendID", "");
        db.collection("friends")
                .document(name)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    String friends_string = data.get("friends").toString();
                    if (friends_string.length() > 2) {
                        String[] friends_string_array = friends_string.substring(1, friends_string.length() - 1).split(", ");
                        List<String> friends_array = new ArrayList<>(Arrays.asList(friends_string_array));
                        CustomAdapterFriends customAdapterAreaOfInterest = new CustomAdapterFriends(friends_array, context, preferences);
                        recyclerViewFriends.setAdapter(customAdapterAreaOfInterest);
                    }
                }
            }
        });
    }
}