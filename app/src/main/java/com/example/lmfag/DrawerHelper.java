package com.example.lmfag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.CreateEvent;
import com.example.lmfag.CustomAdapterAreaOfInterest;
import com.example.lmfag.EditProfile;
import com.example.lmfag.EventsNearby;
import com.example.lmfag.FindEvents;
import com.example.lmfag.FindFriends;
import com.example.lmfag.FriendRequests;
import com.example.lmfag.MainActivity;
import com.example.lmfag.MyEvents;
import com.example.lmfag.MyMessages;
import com.example.lmfag.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DrawerHelper {

    public static void selectDrawerItem(MenuItem menuItem, Activity context) {
        DrawerLayout drawer = context.findViewById(R.id.drawer_layout);
        int id = menuItem.getItemId();
        if (id == R.id.create_event) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("eventID", "");
            editor.apply();
            Intent myIntent = new Intent(context, CreateEvent.class);
            context.startActivity(myIntent);
        } else if (id == R.id.edit_profile) {
            Intent myIntent = new Intent(context, EditProfile.class);
            context.startActivity(myIntent);
        } else if (id == R.id.find_friends) {
            Intent myIntent = new Intent(context, FindFriends.class);
            context.startActivity(myIntent);
        } else if (id == R.id.friend_requests) {
            Intent myIntent = new Intent(context, FriendRequests.class);
            context.startActivity(myIntent);
        } else if (id == R.id.find_events) {
            Intent myIntent = new Intent(context, FindEvents.class);
            context.startActivity(myIntent);
        } else if (id == R.id.my_messages) {
            Intent myIntent = new Intent(context, MyMessages.class);
            context.startActivity(myIntent);
        } else if (id == R.id.my_events) {
            Intent myIntent = new Intent(context, MyEvents.class);
            context.startActivity(myIntent);
        } else if (id == R.id.events_nearby) {
            Intent myIntent = new Intent(context, EventsNearby.class);
            context.startActivity(myIntent);
        }
        drawer.closeDrawer(GravityCompat.START);
    }


    static void fillNavbarData(Activity context) {
        NavigationView navigationView = context.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem, context);
                        return true;
                    }
                });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String name = preferences.getString("userID", "");
        if(name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(context, MainActivity.class);
            context.startActivity(myIntent);
            return;
        }
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    TextView myUsername = context.findViewById(R.id.textViewUsername_nav);
                    myUsername.setText(data.get("username").toString());


                    CardView backProfile = context.findViewById(R.id.goBackToProfile);
                    backProfile.setOnClickListener(view -> {
                        Intent myIntent = new Intent(context, MyProfile.class);
                        context.startActivity(myIntent);
                        return;
                    });

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = context.findViewById(R.id.profile_image_nav);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    context.startActivity(myIntent);
                    //Log.d(TAG, "No such document");
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}
