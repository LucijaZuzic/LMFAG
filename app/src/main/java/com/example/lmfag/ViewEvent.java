package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewEvent extends AppCompatActivity {
    Context context = this;
    final Calendar cldr_start = Calendar.getInstance();

    final Calendar cldr_end = Calendar.getInstance();
    String event_type;
    ImageView imageViewChooseStartDate, imageViewChooseStartTime, imageViewChooseEndDate, imageViewChooseEndTime, apply;
    TextView textViewChooseStartDate, textViewChooseStartTime, textViewChooseEndDate, textViewChooseEndTime;
    double longitude = 45.23;
    double latitude = 45.36;
    String organizer;
    boolean public_event;
    Float participate_minimum, participate_maximum;
    Float minimum_level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        imageViewChooseStartDate = findViewById(R.id.imageViewChooseStartDate);
        textViewChooseStartDate = findViewById(R.id.textViewChooseStartDate);

        imageViewChooseStartTime = findViewById(R.id.imageViewChooseStartTime);
        textViewChooseStartTime = findViewById(R.id.textViewChooseStartTime);

        imageViewChooseEndDate = findViewById(R.id.imageViewChooseEndDate);
        textViewChooseEndDate = findViewById(R.id.textViewChooseEndDate);

        imageViewChooseEndTime = findViewById(R.id.imageViewChooseEndTime);
        textViewChooseEndTime = findViewById(R.id.textViewChooseEndTime);
        fillData();
        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> {
            subscribe();
        });
        ImageView edit = findViewById(R.id.imageViewEdit);
        edit.setOnClickListener(view -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String me = preferences.getString("userID", "");
            if (me.equals(organizer)) {
                Intent myIntent = new Intent(context, CreateEvent.class);
                context.startActivity(myIntent);
            } else {
                Snackbar.make(edit, R.string.organizer_edit, Snackbar.LENGTH_SHORT).show();
            }
        });

        ImageView imageViewRate = findViewById(R.id.imageViewRate);
        imageViewRate.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, RateEvent.class);
            context.startActivity(myIntent);
            /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String me = preferences.getString("userID", "");
            if (Calendar.getInstance().getTime().before(cldr_end.getTime())) {
                Snackbar.make(imageViewChooseStartTime, "Events can't be rated before it ends.", Snackbar.LENGTH_SHORT).show();
            } else {
                if (me.equals(organizer)) {
                    Snackbar.make(imageViewChooseStartTime, "Events can't be rated by organizer.", Snackbar.LENGTH_SHORT).show();
                } else {
                    String eventID = preferences.getString("eventID", "");
                    String userID = preferences.getString("userID", "");
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference docRef = db.collection("event_attending");
                    docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                Snackbar.make(imageViewChooseStartTime, "You can't rate an event if you don't participate.", Snackbar.LENGTH_SHORT).show();
                            } else {
                                boolean found = false;
                                for (QueryDocumentSnapshot doc: task.getResult()) {
                                    if (doc.getData().get("rated").toString().equals("true")) {
                                        Snackbar.make(imageViewChooseStartTime, "You can't rate an event twice.", Snackbar.LENGTH_SHORT).show();
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    Intent myIntent = new Intent(context, RateEvent.class);
                                    context.startActivity(myIntent);
                                }
                            }
                        }
                    });
                }
            }*/
        });
        // TODO RATE SEND
    }
    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(context, MyProfile.class);
        context.startActivity(myIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
        checkSubscribed();
    }
    void refresh() {
        Intent myIntent = new Intent(context, ViewEvent.class);
        context.startActivity(myIntent);
    }
    void checkSubscribed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference docRef = db.collection("event_attending");
        docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    ImageView apply = findViewById(R.id.imageViewApply);
                    apply.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_add_24));
                } else {
                    ImageView apply = findViewById(R.id.imageViewApply);
                    apply.setImageDrawable(getDrawable(R.drawable.ic_baseline_person_remove_24));
                }
            }
        });
    }
    void checkOtherDirection(CollectionReference docuRef, Map<String, Object> docData) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dr = db.collection("friends");
            dr.document(userID).get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful()) {
                    DocumentSnapshot document2 = task2.getResult();
                    if (document2.exists()) {
                        if (!document2.getData().get("friends").toString().contains(userID)) {
                            Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            docuRef.add(docData);
                            refresh();
                        }
                    } else {
                        Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(apply, "You need to be friends with the organizer to participate in a private event.", Snackbar.LENGTH_SHORT).show();
                }
            });
    }
    void checkFriends(CollectionReference docuRef, Map<String, Object> docData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dr = db.collection("friends");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = preferences.getString("userID", "");
        dr.document(organizer).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        if (!document.getData().get("friends").toString().contains(userID)) {
                            checkOtherDirection(docuRef, docData);
                        } else {
                            docuRef.add(docData);
                            refresh();
                        }
                    } else {
                        checkOtherDirection(docuRef, docData);
                    }
                } else {
                    checkOtherDirection(docuRef, docData);
                }
            });
    }
    void checkNumberOfParticipantsAdd(CollectionReference docuRef, Map<String, Object> docData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() < participate_maximum) {
                    db.collection("users").document(userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            if (document2.exists()) {
                                List<String> areas_of_interest = Arrays.asList(document2.getData().get("areas_of_interest").toString().split(", "));
                                List<String> points = Arrays.asList(document2.getData().get("points_levels").toString().split(", "));
                                if (areas_of_interest.contains(event_type)) {
                                    if (minimum_level > 0) {
                                        if (Float.parseFloat(points.get(areas_of_interest.indexOf(event_type))) < minimum_level * 1000) {
                                            Snackbar.make(switch_notify, "Your level is not high enough to participate.", Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            if (public_event) {
                                                docuRef.add(docData);
                                            } else {
                                                checkFriends(docuRef, docData);
                                            }
                                            refresh();
                                        }
                                    }
                                } else {
                                    if (minimum_level > 0) {
                                        Snackbar.make(switch_notify, "Your level is not high enough to participate.", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                } else {
                    Snackbar.make(switch_notify, "Too many participants.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
    void checkNumberOfParticipantsRemove(QueryDocumentSnapshot docuRef) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
        if (eventID.equals("")) {
            return;
        }
        if (userID.equals("")) {
            return;
        }
        CollectionReference dr = db.collection("event_attending");
        dr.whereEqualTo("event", eventID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() > participate_minimum) {
                    db.collection("event_attending").document(docuRef.getId()).delete();
                    refresh();
                } else {
                    Snackbar.make(switch_notify, "Not enough participants.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    void subscribe() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String eventID = preferences.getString("eventID", "");
            String userID = preferences.getString("userID", "");
            SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
            Map<String, Object> docData = new HashMap<>();
            docData.put("event", eventID);
            docData.put("user", userID);
            docData.put("notifications", switch_notify.isChecked());
            docData.put("rated", false);
            if (eventID.equals("")) {
                return;
            }
            if (userID.equals("")) {
                return;
            }
            CollectionReference docRef = db.collection("event_attending");
            docRef.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        checkNumberOfParticipantsAdd(docRef, docData);
                    } else {
                        for (QueryDocumentSnapshot doc: task.getResult()) {
                            checkNumberOfParticipantsRemove(doc);
                        }
                    }
                }
            });
        }


    void getOrganizerData(String name) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(name);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();

                    TextView myUsername = findViewById(R.id.textViewOrganizer);
                    myUsername.setText(data.get("username").toString());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        circleImageView.setImageBitmap(bmp);
                        findViewById(R.id.profile_image).setOnClickListener(view -> {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("friendID", name);
                            editor.apply();
                            Intent myIntent = new Intent(context, ViewProfile.class);
                            startActivity(myIntent);
                        });
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

    void fillData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String eventID = preferences.getString("eventID", "");
        String userID = preferences.getString("userID", "");
        if (eventID.equals("")) {
            return;
        }
        DocumentReference docRef = db.collection("events").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> docData = document.getData();
                    TextView eventName = findViewById(R.id.textViewEventName);
                    TextView description = findViewById(R.id.textViewEvenDescription);
                    TextView minimumlevel = findViewById(R.id.textViewMinimumLevel);
                    TextView sp = findViewById(R.id.textViewEventType);
                    ImageView switchpublic = findViewById(R.id.imageViewPublic);
                    ImageView switcout = findViewById(R.id.imageViewOutdoor);
                    TextView slider = findViewById(R.id.textViewNumberOfPlayers);
                    SwitchCompat switchorganizer = findViewById(R.id.switchOrganizerPlaying);
                    SwitchCompat switch_notify = findViewById(R.id.switchNotifications);
                    eventName.setText(docData.get("event_name").toString());
                    sp.setText(docData.get("event_type").toString());
                    description.setText(docData.get("event_description").toString());
                    minimumlevel.setText(docData.get("minimum_level").toString());
                    if (docData.get("public").toString().equals("true")) {
                        switchpublic.setImageDrawable(getDrawable(R.drawable.ic_baseline_lock_open_24));
                    } else {
                        switchpublic.setImageDrawable(getDrawable(R.drawable.ic_baseline_lock_24));
                    }
                    if (docData.get("outdoors").toString().equals("true")) {
                        switcout.setImageDrawable(getDrawable(R.drawable.ic_baseline_nature_people_24));
                    } else {
                        switcout.setImageDrawable(getDrawable(R.drawable.ic_baseline_attribution_24));
                    }
                    Float val1 = Float.parseFloat(docData.get("minimum_players").toString());
                    Float val2 = Float.parseFloat(docData.get("maximum_players").toString());
                    organizer = docData.get("organizer").toString();
                    event_type = docData.get("event_type").toString();
                    participate_minimum = val1;
                    participate_maximum = val2;
                    minimum_level = Float.parseFloat(docData.get("minimum_level").toString());
                    slider.setText(val1.toString() + "-" + val2.toString());
                    Timestamp start_timestamp = (Timestamp)(docData.get("datetime"));
                    Date start_date = start_timestamp.toDate();
                    cldr_start.setTime(start_date);
                    Timestamp end_timestamp = (Timestamp)(docData.get("ending"));
                    Date end_date = end_timestamp.toDate();
                    cldr_end.setTime(end_date);
                    textViewChooseStartDate.setText(DateFormat.getDateInstance().format(cldr_start.getTime()));
                    textViewChooseStartTime.setText(DateFormat.getTimeInstance().format(cldr_start.getTime()));
                    textViewChooseEndDate.setText(DateFormat.getDateInstance().format(cldr_end.getTime()));
                    textViewChooseEndTime.setText(DateFormat.getTimeInstance().format(cldr_end.getTime()));
                    public_event = docData.get("public").toString().equals("true");
                    GeoPoint locationpoint = (GeoPoint)(docData.get("location"));
                    latitude = locationpoint.getLatitude();
                    longitude = locationpoint.getLongitude();
                    TextView location = findViewById(R.id.textViewChooseLocation);
                    location.setText(latitude + ":" + longitude);
                    getOrganizerData(docData.get("organizer").toString());
                    CollectionReference docRef2 = db.collection("event_attending");
                    docRef2.whereEqualTo("event", eventID).whereEqualTo("user", userID).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            if(task2.getResult().size() == 0) {
                                switch_notify.setChecked(false);
                            } else {
                                for (QueryDocumentSnapshot document2: task2.getResult()) {
                                    Map<String, Object> docData2 = document2.getData();
                                    switch_notify.setChecked(docData2.get("notifications").toString().equals("true"));
                                }
                            }
                        } else {
                            switch_notify.setChecked(false);
                        }
                    });
                } else {
                }
            }
        });
    }
}