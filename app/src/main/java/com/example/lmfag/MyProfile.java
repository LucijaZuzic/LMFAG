package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

public class MyProfile extends AppCompatActivity {

    Context context = this;
    boolean flag = false;
    RecyclerView recyclerViewFriends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        showFriends();
        showAreasOfInterest();
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        fillUserData();
        getFriends();
    }
    public void selectDrawerItem(MenuItem menuItem) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        int id = menuItem.getItemId();
        if (id == R.id.create_event) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("eventID", "");
            editor.apply();
            Intent myIntent = new Intent(context, CreateEvent.class);
            startActivity(myIntent);
        } else if (id == R.id.edit_profile) {
            Intent myIntent = new Intent(context, EditProfile.class);
            startActivity(myIntent);
        } else if (id == R.id.find_friends) {
            Intent myIntent = new Intent(context, FindFriends.class);
            startActivity(myIntent);
        } else if (id == R.id.friend_requests) {
            Intent myIntent = new Intent(context, FriendRequests.class);
            startActivity(myIntent);
        } else if (id == R.id.find_events) {
            Intent myIntent = new Intent(context, FindEvents.class);
            startActivity(myIntent);
        } else if (id == R.id.my_messages) {
            Intent myIntent = new Intent(context, MyMessages.class);
            startActivity(myIntent);
        } else if (id == R.id.my_events) {
            Intent myIntent = new Intent(context, MyEvents.class);
            startActivity(myIntent);
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.menu_open:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (flag) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                flag = !flag;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userID", "");
        editor.apply();
        Intent myIntent = new Intent(context, MainActivity.class);
        startActivity(myIntent);
    }
    void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
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

    void getFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
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