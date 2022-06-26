package com.example.lmfag.utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.lmfag.R;
import com.example.lmfag.activities.ChangePasswordActivity;
import com.example.lmfag.activities.CreateEventActivity;
import com.example.lmfag.activities.EditProfileActivity;
import com.example.lmfag.activities.EventsNearbyActivity;
import com.example.lmfag.activities.FindEventsActivity;
import com.example.lmfag.activities.FindFriendsActivity;
import com.example.lmfag.activities.FriendRequestsActivity;
import com.example.lmfag.activities.MainActivity;
import com.example.lmfag.activities.MyEventsActivity;
import com.example.lmfag.activities.MyMessagesActivity;
import com.example.lmfag.activities.MyProfileActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
            Intent myIntent = new Intent(context, CreateEventActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.edit_profile) {
            Intent myIntent = new Intent(context, EditProfileActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.change_password) {
            Intent myIntent = new Intent(context, ChangePasswordActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.find_friends) {
            Intent myIntent = new Intent(context, FindFriendsActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.friend_requests) {
            Intent myIntent = new Intent(context, FriendRequestsActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.find_events) {
            Intent myIntent = new Intent(context, FindEventsActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.my_messages) {
            Intent myIntent = new Intent(context, MyMessagesActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.my_events) {
            Intent myIntent = new Intent(context, MyEventsActivity.class);
            context.startActivity(myIntent);
        } else if (id == R.id.events_nearby) {
            Intent myIntent = new Intent(context, EventsNearbyActivity.class);
            context.startActivity(myIntent);
        }
        drawer.closeDrawer(GravityCompat.START);
    }


    public static void fillNavbarData(Activity context) {
        NavigationView navigationView = context.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem, context);
                    return true;
                });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        String name = preferences.getString("userID", "");
        String username = preferences.getString("userUsername", "");

        /* Preferences String encoded = preferences.getString("userPicture", "");
        byte[] imageAsBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);*/

        if (name.equals("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            context.startActivity(myIntent);
            return;
        }

        TextView myUsername = context.findViewById(R.id.textViewUsername_nav);
        CardView backProfile = context.findViewById(R.id.goBackToProfile);
        CircleImageView circleImageView = context.findViewById(R.id.profile_image_nav);

        if (backProfile != null) {
            backProfile.setOnClickListener(view -> {
                Intent myIntent = new Intent(context, MyProfileActivity.class);
                context.startActivity(myIntent);
            });
        }

        if (myUsername != null && !username.equals("")) {
            myUsername.setText(username);
        }

        /* Preferences if (circleImageView != null && !encoded.equals("")) {
            Glide.with(context.getApplicationContext()).asBitmap().load(imageAsBytes).placeholder(R.drawable.ic_baseline_person_24).into(circleImageView);
        }

        if (myUsername != null && !username.equals("") && circleImageView != null && !encoded.equals("")) {
            return;
        }*/

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    if (username.equals("")) {
                        editor.putString("userUsername", Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());
                        editor.apply();
                    }

                    if (myUsername != null) {
                        myUsername.setText(Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());
                    }

                    //if (encoded.equals("")) {
                    String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        if (circleImageView != null) {
                            imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(Objects.requireNonNull(circleImageView).getContext().getApplicationContext())
                                            .asBitmap()
                                            .placeholder(R.drawable.ic_baseline_person_24)
                                            .load(bytes).into(circleImageView)
                                    /*.into((new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                            resource.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                                            byte[] b = byteArrayOutputStream.toByteArray();
                                            String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                                            editor.putString("userPicture", encoded);
                                            editor.apply();
                                            circleImageView.setImageBitmap(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    }))*/).addOnFailureListener(exception -> {
                                // Handle any errors
                            });
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        }
                    }
                    /* Preferences } else {
                        Intent myIntent = new Intent(context, MainActivity.class);
                        context.startActivity(myIntent);
                        //Log.d(TAG, "No such document");
                    }*/
                }
            }
        });
    }
}
