package com.example.lmfag;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfile extends AppCompatActivity {

    boolean blocked = false;
    Context context = this;
    List<String> areas_array = new ArrayList<>();
    List<Double> points_array = new ArrayList<>();
    private String selecteditem;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        fillSpinner();
        addAreaOfInterest();
        removeAreaOfInterest();
        createProfile();
        getBack();
        showAreasOfInterest();
        changeProfilePicture();
        Spinner sp = findViewById(R.id.sp);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                selecteditem = adapter.getItemAtPosition(i).toString();
                ImageView iv = findViewById(R.id.imageViewEventType);
                iv.setImageDrawable(getDrawable(EventTypeToDrawable.getEventTypeToDrawable(selecteditem)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {

            }
        });
    }

    void changeProfilePicture() {
        CircleImageView circleImageView = findViewById(R.id.profile_image);
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            circleImageView.setImageBitmap(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        circleImageView.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            someActivityResultLauncher.launch(photoPickerIntent);
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
    void fillSpinner() {
        Spinner sp = findViewById(R.id.sp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    void addAreaOfInterest() {
        ImageView floatingActionButtonAreaOfInterest = findViewById(R.id.imageViewButtonAreaOfInterest);
        floatingActionButtonAreaOfInterest.setOnClickListener(view -> {
            Spinner sp = findViewById(R.id.sp);
            String text = sp.getSelectedItem().toString();
            if (!areas_array.contains(text)) {
                areas_array.add(text);
                points_array.add(0.0);
                RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
                recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);
            } else {
                Snackbar.make(floatingActionButtonAreaOfInterest, R.string.area_of_interest_already_added, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    void removeAreaOfInterest() {
        ImageView floatingActionButtonRemoveAreaOfInterest = findViewById(R.id.imageViewRemoveAreaOfInterest);
        floatingActionButtonRemoveAreaOfInterest.setOnClickListener(view -> {
            Spinner sp = findViewById(R.id.sp);
            String text = sp.getSelectedItem().toString();
            if (areas_array.contains(text)) {
                points_array.remove(areas_array.indexOf(text));
                areas_array.remove(areas_array.indexOf(text));
                RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
                recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);
            } else {
                Snackbar.make(floatingActionButtonRemoveAreaOfInterest, R.string.area_of_interest_not_present, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    void removeAreaOfInterest(String text) {
        ImageView floatingActionButtonRemoveAreaOfInterest = findViewById(R.id.imageViewRemoveAreaOfInterest);
        if (areas_array.contains(text) && areas_array.contains(text)) {
            points_array.remove(areas_array.indexOf(text));
            areas_array.remove(areas_array.indexOf(text));
            RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
            CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);
        } else {
            Snackbar.make(floatingActionButtonRemoveAreaOfInterest, R.string.area_of_interest_not_present, Snackbar.LENGTH_SHORT).show();
        }
    }
    void getBack() {
        ImageView discard = findViewById(R.id.imageViewDiscard);
        discard.setOnClickListener(view -> {
            if (blocked) {
                Snackbar.make(discard, R.string.go_back_upload, Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
        });
    }


    void writeDB(Map<String, Object> docData) {
        ImageView apply = findViewById(R.id.imageViewApply);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .add(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    Snackbar.make(apply, R.string.write_success, Snackbar.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Snackbar.make(apply, R.string.logged_in, Snackbar.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userID", aVoid.getId());
                    editor.apply();
                    if (uri != null) {
                        blocked = true;
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + aVoid.getId());
                        UploadTask uploadTask = imagesRef.putFile(uri);
                        Snackbar.make(apply, R.string.image_upload_started, Snackbar.LENGTH_SHORT).show();
                        uploadTask.addOnFailureListener(exception -> {
                            // Handle unsuccessful uploads
                        }).addOnSuccessListener(taskSnapshot -> {
                            blocked = false;
                            Snackbar.make(apply, R.string.image_upload_finished, Snackbar.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(context, MyProfile.class);
                            startActivity(myIntent);
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }

    void createProfile() {
        ImageView apply = findViewById(R.id.imageViewApply);
        apply.setOnClickListener(view -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            TextView myUsername = findViewById(R.id.editTextUsername);
            String text = myUsername.getText().toString();
            CollectionReference docRef = db.collection("users");
            docRef.whereEqualTo("username", text).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            Snackbar.make(apply, R.string.username_taken, Snackbar.LENGTH_SHORT).show();
                        } else {
                            TextView myUsername = findViewById(R.id.editTextUsername);
                            TextView myLocation = findViewById(R.id.editTextMyLocation);
                            TextView myDescription = findViewById(R.id.editTextMyDescription);
                            EditText passwordEdit = findViewById(R.id.editTextPassword);
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("username", myUsername.getText().toString());
                            docData.put("location", myLocation.getText().toString());
                            docData.put("description", myDescription.getText().toString());
                            if (passwordEdit.getText().toString().length() == 0) {
                                Snackbar.make(apply, R.string.password_short, Snackbar.LENGTH_SHORT).show();
                            } else {
                                try {
                                    docData.put("password_hash", SecureHash.generateStrongPasswordHash(passwordEdit.getText().toString()));
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                                docData.put("points_rank", 0.0);
                                docData.put("areas_of_interest", areas_array);
                                docData.put("points_levels", points_array);
                                writeDB(docData);
                            }
                        }
                    }
                }
            });
        });
    }

}