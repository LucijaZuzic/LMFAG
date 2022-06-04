package com.example.lmfag.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lmfag.R;
import com.example.lmfag.utility.SecureHash;
import com.example.lmfag.utility.TransformBitmap;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestAdd;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestRemove;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileActivity extends MenuInterfaceActivity {
    private boolean blocked = false;
    private Context context;
    private ImageView apply;
    private CircleImageView circleImageView;
    private LinearLayout openableCard;
    private List<String> areas_array = new ArrayList<>();
    private List<String> areas_not_present_array;
    private RecyclerView recyclerViewAreasOfInterest, recyclerViewAreasOfInterestNew;
    private List<Double> points_array = new ArrayList<>();
    private String old_password = "";
    private Bitmap bitmap;
    private boolean rotated = false;
    private EditText myUsername;
    private TextView myLocation;
    private TextView myDescription;
    private EditText passwordEdit;
    private CheckBox check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = this;

        
        areas_not_present_array = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));

        recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
        CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

        recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
        CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
        recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);

        myUsername = findViewById(R.id.editTextUsername);
        myLocation = findViewById(R.id.editTextMyLocation);
        myDescription = findViewById(R.id.editTextMyDescription);
        passwordEdit = findViewById(R.id.editTextPassword);
        ImageView discard = findViewById(R.id.imageViewDiscard);
        apply = findViewById(R.id.imageViewApply);
        FloatingActionButton openCard = findViewById(R.id.floatingActionButtonAddAreaOfInterest);
        ImageView closeCard = findViewById(R.id.closeCard);
        openableCard = findViewById(R.id.openableCard);
        check = findViewById(R.id.checkbox);
        openCard.setOnClickListener(view -> openableCard.setVisibility(View.VISIBLE));
        closeCard.setOnClickListener(view -> openableCard.setVisibility(View.GONE));
        circleImageView = findViewById(R.id.profile_image);
        ImageView rotateLeft = findViewById(R.id.profile_image_rotate_left);
        rotateLeft.setOnClickListener(view -> {
            bitmap = TransformBitmap.RotateNegative90(bitmap);
            circleImageView.setImageBitmap(bitmap);
            rotated = true;
        });
        ImageView profileRotate = findViewById(R.id.profile_image_rotate);
        profileRotate.setOnClickListener(view -> {
            bitmap = TransformBitmap.RotateBy90(bitmap);
            circleImageView.setImageBitmap(bitmap);
            rotated = true;
        });
        ImageView flipHorizontal = findViewById(R.id.profile_image_flip_horizontal);
        flipHorizontal.setOnClickListener(view -> {
            bitmap = TransformBitmap.flipHorizontal(bitmap);
            circleImageView.setImageBitmap(bitmap);
            rotated = true;
        });
        ImageView flipVertical = findViewById(R.id.profile_image_flip_vertical);
        flipVertical.setOnClickListener(view -> {
            bitmap = TransformBitmap.flipVertical(bitmap);
            circleImageView.setImageBitmap(bitmap);
            rotated = true;
        });
        fillUserData();
        createProfile();
        discard.setOnClickListener(view -> onBackPressed());
        changeProfilePicture();
    }

    private void changeProfilePicture() {
        ActivityResultLauncher<Intent> photoPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = Objects.requireNonNull(data).getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            bitmap = TransformBitmap.fixRotation(bitmap);
                            circleImageView.setImageBitmap(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        circleImageView.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPicker.launch(photoPickerIntent);
        });
    }

    public void addAreaOfInterest(String text) {
        if (!areas_array.contains(text)) {
            areas_array.add(text);
            points_array.add(0.0);

            areas_not_present_array = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
            for (String newArea : areas_array) {
                areas_not_present_array.remove(newArea);
            }
            java.util.Collections.sort(areas_not_present_array);

            CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

            CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
            recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);
        } else {
            Toast.makeText(getApplicationContext(), R.string.area_of_interest_already_added, Toast.LENGTH_SHORT).show();
        }
    }

    public void removeAreaOfInterest(String text) {
        if (areas_array.contains(text)) {
            int remove_index = areas_array.indexOf(text);
            points_array.remove(remove_index);
            areas_array.remove(remove_index);

            areas_not_present_array = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
            for (String newArea : areas_array) {
                areas_not_present_array.remove(newArea);
            }
            java.util.Collections.sort(areas_not_present_array);

            CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

            CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
            recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);
        } else {
            Toast.makeText(getApplicationContext(), R.string.area_of_interest_not_present, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String name = preferences.getString("userID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        if (blocked) {
            Toast.makeText(getApplicationContext(), R.string.go_back_upload, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent myIntent = new Intent(context, MyProfileActivity.class);
        startActivity(myIntent);
        finish();
    }

    private void writeDB(Map<String, Object> docData) {

        String name = preferences.getString("userID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        db.collection("users")
                .document(name)
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                    Toast.makeText(getApplicationContext(), R.string.write_success, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), R.string.logged_in, Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(context, MyProfileActivity.class);
                    startActivity(myIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), R.string.write_failed, Toast.LENGTH_SHORT).show();
                    //Log.w(TAG, "Error writing document", e);
                });
    }

    private void createProfile() {
        apply.setOnClickListener(view -> {
            Map<String, Object> docData = new HashMap<>();
            docData.put("username", myUsername.getText().toString());
            docData.put("location", myLocation.getText().toString());
            docData.put("description", myDescription.getText().toString());
            if (check.isChecked()) {
                try {
                    docData.put("password_hash", SecureHash.generateStrongPasswordHash(passwordEdit.getText().toString()));
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            } else {
                docData.put("password_hash", old_password);
            }
            docData.put("points_rank", 0.0);
            docData.put("areas_of_interest", areas_array);
            docData.put("points_levels", points_array);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            String name = preferences.getString("userID", "");
            if (name.equalsIgnoreCase("")) {
                Intent myIntent = new Intent(context, MainActivity.class);
                startActivity(myIntent);
                finish();
                return;
            }
            StorageReference imagesRef = storageRef.child("profile_pictures/" + name);

            if (bitmap != null || rotated) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                byte[] imageDataTransformed = byteArrayOutputStream.toByteArray();
                UploadTask uploadTask = imagesRef.putBytes(imageDataTransformed);
                blocked = true;
                Toast.makeText(getApplicationContext(), R.string.image_upload_started, Toast.LENGTH_SHORT).show();
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                }).addOnSuccessListener(taskSnapshot -> {
                    blocked = false;
                    Toast.makeText(getApplicationContext(), R.string.image_upload_finished, Toast.LENGTH_SHORT).show();
                    writeDB(docData);
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                });
            } else {
                writeDB(docData);
            }
        });
    }

    private void fillUserData() {
        EditProfileActivity ep = this;
        String name = preferences.getString("userID", "");
        if (name.equalsIgnoreCase("")) {
            Intent myIntent = new Intent(context, MainActivity.class);
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
                    myUsername.setText(Objects.requireNonNull(Objects.requireNonNull(data).get("username")).toString());
                    myLocation.setText(Objects.requireNonNull(data.get("location")).toString());
                    old_password = Objects.requireNonNull(data.get("password_hash")).toString();
                    String area_string = Objects.requireNonNull(data.get("areas_of_interest")).toString();
                    if (area_string.length() > 2) {
                        String[] area_string_array = area_string.substring(1, area_string.length() - 1).split(", ");
                        areas_array = new ArrayList<>();
                        areas_not_present_array = new ArrayList<>();
                        Collections.addAll(areas_array, area_string_array);
                        for (String newArea : areas_array) {
                            areas_not_present_array.remove(newArea);
                        }
                        java.util.Collections.sort(areas_not_present_array);
                        String points_string = Objects.requireNonNull(data.get("points_levels")).toString();
                        String[] points_string_array = points_string.substring(1, points_string.length() - 1).split(", ");
                        points_array = new ArrayList<>();
                        for (String s : points_string_array) {
                            points_array.add(Double.parseDouble(s));
                        }
                        CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, ep);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);
                    }
                    myDescription.setText(Objects.requireNonNull(data.get("description")).toString());
                    if (bitmap == null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + name);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(bytes -> Glide.with(circleImageView.getContext().getApplicationContext())
                                .asBitmap()
                                .load(bytes)
                                .into((new CustomTarget<Bitmap>() {

                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        bitmap = resource;
                                        if (circleImageView != null) {
                                            circleImageView.setImageBitmap(resource);
                                        }
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                }))).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    }
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    finish();
                    //Log.d(TAG, "No such document");
                }
            }
        });
    }
}