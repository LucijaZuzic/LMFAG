package com.example.lmfag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lmfag.R;
import com.example.lmfag.fragments.ViewProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.ViewProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.ViewProfileEventsPlayerFragment;
import com.example.lmfag.fragments.ViewProfileFriendsFragment;
import com.example.lmfag.fragments.ViewProfileInfoFragment;
import com.example.lmfag.utility.adapters.TabPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewProfileActivity extends MenuInterfaceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        fillUserData();
    }

    private void getOrganizerEvents() {
        List<String> organizer_events_array = new ArrayList<>();
        List<Integer> organizer_events_timestamps = new ArrayList<>();
        String friendID = preferences.getString("friendID", "");
        if (!friendID.equals("")) {
            db.collection("events").whereEqualTo("organizer", friendID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            organizer_events_array.add(document.getId());
                            Calendar cldr_start = Calendar.getInstance();
                            Timestamp start_timestamp = (Timestamp) (document.getData().get("datetime"));
                            Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                            cldr_start.setTime(start_date);
                            Calendar cldr_end = Calendar.getInstance();
                            Timestamp end_timestamp = (Timestamp) (document.getData().get("ending"));
                            Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                            cldr_end.setTime(end_date);
                            organizer_events_timestamps.add(checkTimestamp(cldr_start, cldr_end));
                        }

                        StringBuilder events_organizer_stringBuilder = new StringBuilder();
                        StringBuilder events_organizer_timestamp_stringBuilder = new StringBuilder();
                        for (String event : organizer_events_array) {
                            events_organizer_stringBuilder.append(event).append("_");
                        }
                        for (Integer timestamp : organizer_events_timestamps) {
                            events_organizer_timestamp_stringBuilder.append(timestamp).append("_");
                        }
                        String events_organizer_string = events_organizer_stringBuilder.toString();
                        String events_organizer_timestamp_string = events_organizer_timestamp_stringBuilder.toString();
                        if (events_organizer_string.length() > 0) {
                            editor.putString("friendOrganizer", events_organizer_string.substring(0, events_organizer_string.length() - 1));
                            editor.apply();
                            editor.putString("friendOrganizerTimestamp", events_organizer_timestamp_string.substring(0, events_organizer_timestamp_string.length() - 1));
                        } else {
                            editor.putString("friendOrganizer", "");
                            editor.apply();
                            editor.putString("friendOrganizerTimestamp", "");
                        }
                    } else {
                        editor.putString("friendOrganizer", "");
                        editor.apply();
                        editor.putString("friendOrganizerTimestamp", "");
                    }
                } else {
                    editor.putString("friendOrganizer", "");
                    editor.apply();
                    editor.putString("friendOrganizerTimestamp", "");
                }
                editor.apply();
                fillPager();
            });
        }
    }
    private int checkTimestamp(Calendar cldr_start, Calendar cldr_end) {
        if (cldr_start.getTime().after(Calendar.getInstance().getTime()) && cldr_end.getTime().after(Calendar.getInstance().getTime())) {
            return 0;
        }
        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) && cldr_end.getTime().after(Calendar.getInstance().getTime())) {
            return 1;
        }
        if (cldr_start.getTime().before(Calendar.getInstance().getTime()) && cldr_end.getTime().before(Calendar.getInstance().getTime())) {
            return 2;
        }
        return -1;
    }
    private void getSubscriberEvents() {
        List<String> player_events_array = new ArrayList<>();
        List<String> subscriber_events_array = new ArrayList<>();
        List<Integer> player_events_timestamps = new ArrayList<>();
        List<Integer> subscriber_events_timestamps = new ArrayList<>();
        String friendID = preferences.getString("friendID", "");
        AtomicInteger processed = new AtomicInteger();
        if (!friendID.equals("")) {
            db.collection("event_attending").whereEqualTo("user", friendID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String isSubscribed = Objects.requireNonNull(document.getData().get("notifications")).toString();
                            db.collection("events").document(document.getData().get("event").toString()).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document1 = task1.getResult();
                                    Calendar cldr_start = Calendar.getInstance();
                                    Timestamp start_timestamp = (Timestamp) (document1.getData().get("datetime"));
                                    Date start_date = Objects.requireNonNull(start_timestamp).toDate();
                                    cldr_start.setTime(start_date);
                                    Calendar cldr_end = Calendar.getInstance();
                                    Timestamp end_timestamp = (Timestamp) (document1.getData().get("ending"));
                                    Date end_date = Objects.requireNonNull(end_timestamp).toDate();
                                    cldr_end.setTime(end_date);
                                    int timestamp = checkTimestamp(cldr_start, cldr_end);

                                    if (isSubscribed.equals("true")) {
                                        subscriber_events_array.add(Objects.requireNonNull(document.getData().get("event")).toString());
                                        subscriber_events_timestamps.add(timestamp);
                                    }
                                    String isAttending = Objects.requireNonNull(document.getData().get("attending")).toString();
                                    if (isAttending.equals("true")) {
                                        player_events_array.add(Objects.requireNonNull(document.getData().get("event")).toString());
                                        player_events_timestamps.add(timestamp);
                                    }
                                    processed.getAndIncrement();
                                    if (processed.get() == task.getResult().size()) {
                                        StringBuilder events_player_stringBuilder = new StringBuilder();
                                        StringBuilder events_player_timestamp_stringBuilder = new StringBuilder();
                                        for (String event : player_events_array) {
                                            events_player_stringBuilder.append(event).append("_");
                                        }
                                        for (Integer timestampTMP : player_events_timestamps) {
                                            events_player_timestamp_stringBuilder.append(timestampTMP).append("_");
                                        }
                                        String events_player_string = events_player_stringBuilder.toString();
                                        String events_player_timestamp_string = events_player_timestamp_stringBuilder.toString();
                                        StringBuilder events_subscriber_stringBuilder = new StringBuilder();
                                        StringBuilder events_subscriber_timestamp_stringBuilder = new StringBuilder();
                                        for (String event : subscriber_events_array) {
                                            events_subscriber_stringBuilder.append(event).append("_");
                                        }
                                        for (Integer timestampTMP : subscriber_events_timestamps) {
                                            events_subscriber_timestamp_stringBuilder.append(timestampTMP).append("_");
                                        }
                                        String events_subscriber_string = events_subscriber_stringBuilder.toString();
                                        String events_subscriber_timestamp_string = events_subscriber_timestamp_stringBuilder.toString();
                                        if (events_player_string.length() > 0) {
                                            editor.putString("friendPlayer", events_player_string.substring(0, events_player_string.length() - 1));
                                            editor.apply();
                                            editor.putString("friendPlayerTimestamp", events_player_timestamp_string.substring(0, events_player_timestamp_string.length() - 1));
                                        } else {
                                            editor.putString("friendPlayer", "");
                                            editor.apply();
                                            editor.putString("friendPlayerTimestamp", "");
                                        }
                                        editor.apply();
                                        if (events_subscriber_string.length() > 0) {
                                            editor.putString("friendSubscriber", events_subscriber_string.substring(0, events_subscriber_string.length() - 1));
                                            editor.apply();
                                            editor.putString("friendSubscriberTimestamp", events_subscriber_timestamp_string.substring(0, events_subscriber_timestamp_string.length() - 1));
                                        } else {
                                            editor.putString("friendSubscriber", "");
                                            editor.apply();
                                            editor.putString("friendSubscriberTimestamp", "");
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        editor.putString("friendPlayer", "");
                        editor.apply();
                        editor.putString("friendPlayerTimestamp", "");
                        editor.apply();
                        editor.putString("friendSubscriber", "");
                        editor.apply();
                        editor.putString("friendSubscriberTimestamp", "");
                    }
                } else {
                    editor.putString("friendPlayer", "");
                    editor.apply();
                    editor.putString("friendPlayerTimestamp", "");
                    editor.apply();
                    editor.putString("friendSubscriber", "");
                    editor.apply();
                    editor.putString("friendSubscriberTimestamp", "");
                }
                editor.apply();
                getOrganizerEvents();
            });
        }
    }

    private void fillUserData() {
        String name = preferences.getString("friendID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    editor.putString("friendUsername", Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());
                    editor.apply();
                    editor.putString("friendLocation", Objects.requireNonNull(data.get("location")).toString());
                    editor.apply();
                    editor.putString("friendRankPoints", Objects.requireNonNull(data.get("points_rank")).toString());
                    editor.apply();
                    editor.putString("friendDescription", Objects.requireNonNull(data.get("description")).toString());
                    editor.apply();
                    String area_string = Objects.requireNonNull(data.get("areas_of_interest")).toString();
                    editor.putString("friend_areas_of_interest", area_string);
                    editor.apply();
                    String points_string = Objects.requireNonNull(data.get("points_levels")).toString();
                    editor.putString("friend_points_levels", points_string);
                    editor.apply();
                    /* Preferences String imageView = preferences.getString("showImage", "true");
                    if (imageView.equals("true")) {
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(bytes).into()
                                /*.into((new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        resource.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                        byte[] b = byteArrayOutputStream.toByteArray();
                                        String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                                        editor.putString("friendPicture", encoded);
                                        editor.apply();
                                        getSubscriberEvents();
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                }))).addOnFailureListener(exception -> {
                            // Handle any errors
                            editor.putString("friendPicture", "");
                            editor.apply();
                            getSubscriberEvents();
                        });
                    } else {
                        editor.putString("friendPicture", "");
                        editor.apply();
                        getSubscriberEvents();
                    }*/
                    getSubscriberEvents();
                } else {
                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            }
        });
    }

    private void fillPager() {
        TabPagerAdapter tabPagerAdapterViewProfile = new TabPagerAdapter(this,
                new ViewProfileInfoFragment(), new ViewProfileFriendsFragment(), new ViewProfileAreasOfInterestFragment(),
                new ViewProfileEventsOrganizerFragment(), new ViewProfileEventsPlayerFragment());
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapterViewProfile);
        viewPager.setSaveEnabled(false);

        TabLayout tabLayout = findViewById(R.id.tab);
        LinearLayout indeterminateBar = findViewById(R.id.indeterminateBar);
        new TabLayoutMediator(tabLayout, viewPager, true, true, (tab, position) -> {
            indeterminateBar.setVisibility(View.GONE);
            switch (position) {
                case 0:
                    tab.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_person_24));
                    break;
                case 1:
                    tab.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_groups_24));
                    break;
                case 2:
                    tab.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_interests_24));
                    break;
                case 3:
                    tab.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_emoji_events_24));
                    break;
                case 4:
                    tab.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_calendar_today_24));
                    break;
            }
        }).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}