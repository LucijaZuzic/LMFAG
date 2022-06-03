package com.example.lmfag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.activities.ViewMessagesActivity;
import com.example.lmfag.utility.DrawerHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class   ViewProfileInfoFragment extends Fragment {
    private Context context;
    private Activity activity;
    private LinearLayout mainLayout;
    private ImageView message, friendRequest;
    private boolean areFriends, isSent;
    SharedPreferences preferences;
    FirebaseFirestore db;

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
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        db = FirebaseFirestore.getInstance();
        DrawerHelper.fillNavbarData(activity);
        fillUserData(view);
        mainLayout = view.findViewById(R.id.main_layout);
        message = view.findViewById(R.id.imageViewMyMesages);
        friendRequest = view.findViewById(R.id.imageViewSendFriendRequest);
        message.setOnClickListener(viewNew -> {
            Intent myIntent = new Intent(context, ViewMessagesActivity.class);
            context.startActivity(myIntent);
        });
        String sender = preferences.getString("userID", "");
        String receiver = preferences.getString("friendID", "");
        checkFriends(sender, receiver);
        friendRequest.setOnClickListener(viewNew -> {
            if (areFriends) {
                db.collection("friends").document(receiver).update("friends", FieldValue.arrayRemove(sender));
                db.collection("friends").document(sender).update("friends", FieldValue.arrayRemove(receiver));
                 Toast.makeText(context.getApplicationContext(), R.string.no_longer_friends, Toast.LENGTH_SHORT).show();
                areFriends = false;
            } else {
                writeToDb(sender, receiver);
                isSent = true;
            }
            hideOptions(sender, receiver);
        });
    }

    private void fillUserData(@NonNull View view) {
        String name = preferences.getString("friendID", "");
        if(name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        TextView myUsername = view.findViewById(R.id.textViewUsername);
        TextView myLocation = view.findViewById(R.id.textViewMyLocation);
        TextView myDescription = view.findViewById(R.id.textViewMyDescription);
        TextView myOrganizerRank = view.findViewById(R.id.textViewMyOrganizerRank);
        TextView myOrganizerRankPoints = view.findViewById(R.id.textViewRankPoints);
        myUsername.setText(preferences.getString("friendUsername", ""));
        myLocation.setText(preferences.getString("friendLocation", ""));
        Double points_rank = Double.parseDouble(preferences.getString("friendRankPoints", ""));
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
        myDescription.setText(Objects.requireNonNull(preferences.getString("friendDescription", "")));
        String encoded = preferences.getString("friendPicture", "");
        byte[] imageAsBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
        CircleImageView circleImageView = view.findViewById(R.id.profile_image);
        circleImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
    }

    private void writeToDb(String sender, String receiver) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("sender", sender);
        docData.put("receiver", receiver);
        db.collection("friend_requests")
        .add(docData)
        .addOnSuccessListener(aVoid -> {
            //Log.d(TAG, "DocumentSnapshot successfully written!");
             Toast.makeText(context.getApplicationContext(), R.string.friend_request_sent, Toast.LENGTH_SHORT).show();
            //MessageSender.sendMessage("friend_requests in topics && " + receiver + " in topics",
                    //getResources().getString(R.string.friend_request_sent),  getResources().getString(R.string.friend_request_sent));
        })
        .addOnFailureListener(e -> {
             Toast.makeText(context.getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();
            //Log.w(TAG, "Error writing document", e);
        });
    }

    private void hideOptions(String sender, String receiver) {
        if (isSent) {
            friendRequest.setVisibility(View.GONE);
        } else {
            if (areFriends) {
                friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_remove_24));
            } else {
                friendRequest.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_person_add_24));
            }
            friendRequest.setVisibility(View.VISIBLE);
        }
        mainLayout.setVisibility(View.VISIBLE);
    }

    private void checkSentReverse(String sender, String receiver) {
        db.collection("friend_requests").whereEqualTo("receiver", receiver).whereEqualTo("sender", sender)
        .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    isSent = true;
                    hideOptions(sender, receiver);
                } else {
                    hideOptions(sender, receiver);
                }
            } else {
                hideOptions(sender, receiver);
            }
        });
    }

    private void checkSent(String sender, String receiver) {
        db.collection("friend_requests").whereEqualTo("sender", receiver).whereEqualTo("receiver", sender)
        .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() > 0) {
                    isSent = true;
                    hideOptions(sender, receiver);
                } else {
                    checkSentReverse(sender, receiver);
                }
            } else {
                checkSentReverse(sender, receiver);
            }
        });
    }

    private void checkFriendsReverse(String sender, String receiver) {
        db.collection("friends").document(receiver).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(sender)) {
                        areFriends = true;
                        checkSent(sender, receiver);
                    } else {
                        checkSent(sender, receiver);
                    }
                } else {
                    checkSent(sender, receiver);
                }
            } else {
                checkSent(sender, receiver);
            }
        });
    }
    private void checkFriends(String sender, String receiver) {
        db.collection("friends").document(sender).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docuData = document.getData();
                    String friends = docuData.get("friends").toString();
                    if (friends.contains(receiver)) {
                        areFriends = true;
                        checkSent(sender, receiver);
                    } else {
                        checkFriendsReverse(sender, receiver);
                    }
                } else {
                    checkFriendsReverse(sender, receiver);
                }
            } else {
                checkFriendsReverse(sender, receiver);
            }
        });
    }
}