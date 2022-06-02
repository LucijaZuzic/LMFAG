package com.example.lmfag.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lmfag.R;
import com.example.lmfag.fragments.ViewProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.ViewProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.ViewProfileEventsPlayerFragment;
import com.example.lmfag.fragments.ViewProfileFriendsFragment;
import com.example.lmfag.fragments.ViewProfileInfoFragment;
import com.example.lmfag.utility.adapters.TabPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViewProfileActivity extends MenuInterfaceActivity {
    private TabPagerAdapter tabPagerAdapterViewProfile;
    private ViewPager2 viewPager;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        editor = preferences.edit();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
        fillUserData();
    }

    private void getOrganizerEvents() {
        List<String> organizer_events_array = new ArrayList<>();
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            db.collection("events").whereEqualTo("organizer", friendID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                organizer_events_array.add(document.getId());
                            }
                            Collections.sort(organizer_events_array);
                            String events_organizer_string = "";
                            for (String event: organizer_events_array) {
                                events_organizer_string += event + "_";
                            }
                            editor.putString("friendOrganizer", events_organizer_string.substring(0, events_organizer_string.length() - 1));
                            editor.apply();
                            fillPager();
                        } else {
                            editor.putString("friendOrganizer", "");
                            editor.apply();
                            fillPager();
                        }
                    } else {
                        editor.putString("friendOrganizer", "");
                        editor.apply();
                        fillPager();
                    }
                }
            });
        }
    }

    private void getSubscriberEvents() {
        List<String> player_events_array = new ArrayList<>();
        List<String> subscriber_events_array = new ArrayList<>();
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", friendID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String isSubscribed = document.getData().get("notifications").toString();
                                if (isSubscribed.equals("true")) {
                                    subscriber_events_array.add(document.getData().get("event").toString());
                                }
                                player_events_array.add(document.getData().get("event").toString());
                            }
                            Collections.sort(subscriber_events_array);
                            Collections.sort(player_events_array);
                            String events_player_string = "";
                            for (String event: player_events_array) {
                                events_player_string += event + "_";
                            }
                            String events_subscriber_string = "";
                            for (String event: subscriber_events_array) {
                                events_subscriber_string += event + "_";
                            }
                            editor.putString("friendPlayer", events_player_string.substring(0, events_player_string.length() - 1));
                            editor.apply();
                            editor.putString("friendSubscriber", events_subscriber_string.substring(0, events_subscriber_string.length() - 1));
                            editor.apply();
                            getOrganizerEvents();
                        } else {
                            editor.putString("friendPlayer", "");
                            editor.apply();
                            editor.putString("friendSubscriber", "");
                            editor.apply();
                            getOrganizerEvents();
                        }
                    } else {
                        editor.putString("friendPlayer", "");
                        editor.apply();
                        editor.putString("friendSubscriber", "");
                        editor.apply();
                        getOrganizerEvents();
                    }
                }
            });
        }
    }

    private void fillUserData() {
        String name = preferences.getString("friendID", "");
        if(name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    editor.putString("friendUsername", data.get("username").toString());
                    editor.apply();
                    editor.putString("friendLocation", data.get("location").toString());
                    editor.apply();
                    editor.putString("friendRankPoints", data.get("points_rank").toString());
                    editor.apply();
                    editor.putString("friendDescription", data.get("description").toString());
                    editor.apply();
                    String area_string = data.get("areas_of_interest").toString();
                    editor.putString("friend_areas_of_interest", area_string);
                    editor.apply();
                    String points_string = data.get("points_levels").toString();
                    editor.putString("friend_points_levels", points_string);
                    editor.apply();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
                        byte[] b = baos.toByteArray();
                        String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                        editor.putString("friendPicture", encoded);
                        editor.apply();
                        getSubscriberEvents();
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            editor.putString("friendPicture", "");
                            editor.apply();
                            getSubscriberEvents();
                        }
                    });
                } else {
                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                }
            }
        });
    }

    private void fillPager() {
        tabPagerAdapterViewProfile = new TabPagerAdapter(this,
                new ViewProfileInfoFragment(), new ViewProfileFriendsFragment(), new ViewProfileAreasOfInterestFragment(),
                new ViewProfileEventsOrganizerFragment(), new ViewProfileEventsPlayerFragment());
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapterViewProfile);

        TabLayout tabLayout = findViewById(R.id.tab);
        LinearLayout indeterminateBar = findViewById(R.id.indeterminateBar);
        new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                indeterminateBar.setVisibility(View.GONE);
                switch (position) {
                    case 0:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_person_24));
                        break;
                    case 1:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_groups_24));
                        break;
                    case 2:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_interests_24));
                        break;
                    case 3:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_emoji_events_24));
                        break;
                    case 4:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_calendar_today_24));
                        break;
                }
            }
        }).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}