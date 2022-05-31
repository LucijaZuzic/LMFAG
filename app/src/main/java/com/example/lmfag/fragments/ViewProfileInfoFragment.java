package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.example.lmfag.activities.ViewProfileActivity;
import com.example.lmfag.utility.DrawerHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileInfoFragment extends Fragment {
    private Context context;
    private Activity activity;
    private ImageView message, friendRequest;
    private View viewForAll;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerHelper.fillNavbarData(activity);
        fillUserData(view);
        message = view.findViewById(R.id.imageViewMyMesages);
        friendRequest = view.findViewById(R.id.imageViewSendFriendRequest);
        viewForAll = view;
        message.setOnClickListener(viewForAll -> {
            Intent myIntent = new Intent(context, ViewMessagesActivity.class);
            context.startActivity(myIntent);
        });
        this.sendFriendRequest(false);
        friendRequest.setOnClickListener(viewForAll -> {
            this.sendFriendRequest(true);
        });
    }

    private void fillUserData(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String name = preferences.getString("friendID", "");
        if(name.equalsIgnoreCase(""))
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

                    TextView myUsername = view.findViewById(R.id.textViewUsername);
                    TextView myLocation = view.findViewById(R.id.textViewMyLocation);
                    TextView myDescription = view.findViewById(R.id.textViewMyDescription);
                    TextView myOrganizerRank = view.findViewById(R.id.textViewMyOrganizerRank);
                    TextView myOrganizerRankPoints = view.findViewById(R.id.textViewRankPoints);
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
                    ProgressBar progressBar = view.findViewById(R.id.determinateBar);
                    progressBar.setProgress((int)((points_rank - (upper_bound - 1000)) / 10));
                    myOrganizerRank.setText(text_rank);
                    myOrganizerRankPoints.setText(text_rank_points);
                    myDescription.setText(Objects.requireNonNull(data.get("description")).toString());
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = view.findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                }
            }
        });
    }
    private void writeToDb(String sender, String receiver) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    private void checkFriendsTwoDir(String sender, String receiver, boolean no_request_pending, boolean try_to_change_status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friends").document(sender).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(receiver)) {
                        friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_remove_24));
                        if (try_to_change_status && no_request_pending) {
                            db.collection("friends").document(receiver).update("friends", FieldValue.arrayRemove(sender));
                            db.collection("friends").document(sender).update("friends", FieldValue.arrayRemove(receiver));
                            Snackbar.make(friendRequest, R.string.no_longer_friends, Snackbar.LENGTH_SHORT).show();
                            refresh();
                        }
                    } else {
                        friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_add_24));
                        if (try_to_change_status && no_request_pending) {
                            writeToDb(sender, receiver);
                        }
                    }
                } else {
                    friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_add_24));
                    if (try_to_change_status && no_request_pending) {
                        writeToDb(sender, receiver);
                    }
                }
            }
        });
    }

    private void checkFriendsOneDir(String sender, String receiver, boolean no_request_pending, boolean try_to_change_status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friends").document(receiver).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(sender)) { 
                        friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_remove_24)); 
                        if (try_to_change_status && no_request_pending) {
                            db.collection("friends").document(receiver).update("friends", FieldValue.arrayRemove(sender));
                            db.collection("friends").document(sender).update("friends", FieldValue.arrayRemove(receiver));
                            Snackbar.make(friendRequest, R.string.no_longer_friends, Snackbar.LENGTH_SHORT).show();
                            refresh();
                        }
                    } else {
                        friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_add_24));
                        checkFriendsTwoDir(sender, receiver, no_request_pending, try_to_change_status);
                    }
                } else {
                    friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_add_24));
                    checkFriendsTwoDir(sender, receiver, no_request_pending, try_to_change_status);
                }
            }
        });
    }

    private void checkSentTwoDir(String sender, String receiver, boolean no_request_pending, boolean try_to_change_status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friend_requests").whereEqualTo("sender", receiver).whereEqualTo("receiver", sender)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) { 
                    friendRequest.setVisibility(View.GONE);
                    Snackbar.make(friendRequest, R.string.already_sent, Snackbar.LENGTH_SHORT).show();
                    checkFriendsOneDir(sender, receiver, false, try_to_change_status);
                } else {
                    friendRequest.setVisibility(View.VISIBLE);
                    checkFriendsOneDir(sender, receiver, no_request_pending, try_to_change_status);
                }
            }
        });
    }

    private void checkSentOneDir(String sender, String receiver, boolean no_request_pending, boolean try_to_change_status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friend_requests").whereEqualTo("sender", sender).whereEqualTo("receiver", receiver)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    friendRequest.setVisibility(View.GONE);
                    Snackbar.make(friendRequest, R.string.already_sent, Snackbar.LENGTH_SHORT).show();
                    checkSentTwoDir(sender, receiver, false, try_to_change_status);
                } else {
                    friendRequest.setVisibility(View.VISIBLE);
                    checkSentTwoDir(sender, receiver, no_request_pending, try_to_change_status);
                }
            }
        });
    }

    private void sendFriendRequest(boolean try_to_change_status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String sender = preferences.getString("userID", "");
        String receiver = preferences.getString("friendID", "");
        if (sender.equals(receiver)) {
            friendRequest.setVisibility(View.GONE);
            Snackbar.make(friendRequest, R.string.friends_with_self, Snackbar.LENGTH_SHORT).show();
            return;
        }
        checkSentOneDir(sender, receiver, true, try_to_change_status);
    }
    private void refresh() {
        Intent myIntent = new Intent(context, ViewProfileActivity.class);
        context.startActivity(myIntent); 
    }


}