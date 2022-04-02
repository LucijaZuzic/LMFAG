package com.example.lmfag;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfile extends AppCompatActivity {
    boolean blocked = false;
    Context context = this;
    List<String> areas_array = new ArrayList<>();
    List<Double> points_array = new ArrayList<>();
    String old_password = "";
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        fillSpinner();
        addAreaOfInterest();
        removeAreaOfInterest();
        createProfile();
        getBack();
        showAreasOfInterest();
        changeProfilePicture();
    }
    @Override
    public void onStart() {
        super.onStart();
        fillUserData();
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
        ImageView floatingActionButtonAreaOfInterest = findViewById(R.id.floatingActionButtonAreaOfInterest);
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
        ImageView floatingActionButtonRemoveAreaOfInterest = findViewById(R.id.floatingActionButtonRemoveAreaOfInterest);
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
        ImageView floatingActionButtonRemoveAreaOfInterest = findViewById(R.id.floatingActionButtonRemoveAreaOfInterest);
        if (areas_array.contains(text)) {
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
        ImageView discard = findViewById(R.id.buttonDiscard);
        discard.setOnClickListener(view -> {
            if (blocked) {
                Snackbar.make(discard, R.string.go_back_upload, Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent myIntent = new Intent(context, MyProfile.class);
            startActivity(myIntent);
        });
    }

    void writeDB(Map<String, Object> docData) {
        ImageView apply = findViewById(R.id.buttonApply);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
        if(name.equalsIgnoreCase(""))
        {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            return;
        }
        db.collection("users")
                .document(name)
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    Snackbar.make(apply, R.string.write_success, Snackbar.LENGTH_SHORT).show();
                    Snackbar.make(apply, R.string.logged_in, Snackbar.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(context, MyProfile.class);
                    startActivity(myIntent);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }
    void createProfile() {
        ImageView apply = findViewById(R.id.buttonApply);
        CheckBox check = findViewById(R.id.checkbox);
        apply.setOnClickListener(view -> {
            EditText myUsername = findViewById(R.id.editTextUsername);
            TextView myLocation = findViewById(R.id.editTextMyLocation);
            TextView myDescription = findViewById(R.id.editTextMyDescription);
            EditText passwordEdit = findViewById(R.id.editTextPassword);
            Map<String, Object> docData = new HashMap<>();
            docData.put("username", myUsername.getText().toString());
            docData.put("location", myLocation.getText().toString());
            docData.put("description", myDescription.getText().toString());
            if (check.isChecked()) {
                try {
                    docData.put("password_hash", SecureHash.generateStrongPasswordHash(passwordEdit.getText().toString()));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            } else {
                docData.put("password_hash", old_password);
            }
            docData.put("points_rank", 0.0);
            docData.put("areas_of_interest", areas_array.toString());
            docData.put("points_levels", points_array.toString());

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String name = preferences.getString("userID", "");
            if(name.equalsIgnoreCase(""))
            {
                Intent myIntent = new Intent(context, MainActivity.class);
                startActivity(myIntent);
                return;
            }
            StorageReference imagesRef = storageRef.child("profile_pictures/" + name);

            if (uri != null) {
                UploadTask uploadTask = imagesRef.putFile(uri);
                blocked = true;
                Snackbar.make(myDescription, R.string.image_upload_started, Snackbar.LENGTH_SHORT).show();
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                }).addOnSuccessListener(taskSnapshot -> {
                    blocked = false;
                    Snackbar.make(myDescription, R.string.image_upload_finished, Snackbar.LENGTH_SHORT).show();
                    writeDB(docData);
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                });
            } else {
                writeDB(docData);
            }
        });
    }

    void fillUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        EditProfile ep = this;
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
                    EditText myUsername = findViewById(R.id.editTextUsername);
                    TextView myLocation = findViewById(R.id.editTextMyLocation);
                    TextView myDescription = findViewById(R.id.editTextMyDescription);
                    RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                    myUsername.setText(data.get("username").toString());
                    myLocation.setText(data.get("location").toString());
                    old_password = data.get("password_hash").toString();
                    String area_string = data.get("areas_of_interest").toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        areas_array = new ArrayList<>();
                        Collections.addAll(areas_array, area_string_array);
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Double.parseDouble(s));
                        }
                        CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, ep);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);
                    }
                    myDescription.setText(data.get("description").toString());
                    if (uri == null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                            // Data for "images/island.jpg" is returns, use this as needed
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            circleImageView.setImageBitmap(bmp);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    }
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    return;
                    //Log.d(TAG, "No such document");
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}