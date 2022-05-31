package com.example.lmfag.activities;

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

import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestAdd;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestRemove;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.EventTypeToDrawable;
import com.example.lmfag.R;
import com.example.lmfag.utility.SecureHash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfileActivity extends AppCompatActivity {

    private TextView myUsername;
    private TextView myLocation;
    private TextView myDescription;
    private EditText passwordEdit;

    private FirebaseFirestore db;

    private boolean blocked = false;
    private Context context = this;

    private List<String> areas_array = new ArrayList<>();
    private List<String> areas_not_present_array;

    private List<Double> points_array = new ArrayList<>();
    private Uri uri;

    private ImageView apply, discard, closeCard;
    private FloatingActionButton openCard;

    private LinearLayout openableCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        areas_not_present_array = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.event_types)));

        RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
        CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

        RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
        CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
        recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);

        db = FirebaseFirestore.getInstance();
        myUsername = findViewById(R.id.editTextUsername);
        myLocation = findViewById(R.id.editTextMyLocation);
        myDescription = findViewById(R.id.editTextMyDescription);
        passwordEdit = findViewById(R.id.editTextPassword);
        discard = findViewById(R.id.imageViewDiscard);
        apply = findViewById(R.id.imageViewApply);
        openCard = findViewById(R.id.floatingActionButtonAddAreaOfInterest);
        closeCard = findViewById(R.id.closeCard);
        openableCard = findViewById(R.id.openableCard);
        openCard.setOnClickListener(view -> {
            openableCard.setVisibility(View.VISIBLE);
        });
        closeCard.setOnClickListener(view -> {
            openableCard.setVisibility(View.GONE);
        });
        createProfile();
        getBack();
        changeProfilePicture();
    }

    private void changeProfilePicture() {
        CircleImageView circleImageView = findViewById(R.id.profile_image);
        ActivityResultLauncher<Intent> photoPicker = registerForActivityResult(
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
            photoPicker.launch(photoPickerIntent);
        });
    }

    public void addAreaOfInterest(String text) {
        if (!areas_array.contains(text)) {
            areas_array.add(text);
            points_array.add(0.0);

            areas_not_present_array = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.event_types)));
            for (String newArea: areas_array) {
                areas_not_present_array.remove(newArea);
            }
            java.util.Collections.sort(areas_not_present_array);

            RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
            CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

            RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
            CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
            recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);
        } else {
            Snackbar.make(discard, R.string.area_of_interest_already_added, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void removeAreaOfInterest(String text) {
        if (areas_array.contains(text)) {
            points_array.remove(areas_array.indexOf(text));
            areas_array.remove(areas_array.indexOf(text));

            areas_not_present_array = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.event_types)));
            for (String newArea: areas_array) {
                areas_not_present_array.remove(newArea);
            }
            java.util.Collections.sort(areas_not_present_array);

            RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
            CustomAdapterAreaOfInterestRemove customAdapterAreaOfInterestRemove = new CustomAdapterAreaOfInterestRemove(areas_array, points_array, this);
            recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterestRemove);

            RecyclerView recyclerViewAreasOfInterestNew = findViewById(R.id.recyclerViewAreasOfInterestNew);
            CustomAdapterAreaOfInterestAdd customAdapterAreaOfInterestAdd = new CustomAdapterAreaOfInterestAdd(areas_not_present_array, this);
            recyclerViewAreasOfInterestNew.setAdapter(customAdapterAreaOfInterestAdd);
        } else {
            Snackbar.make(discard, R.string.area_of_interest_not_present, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void getBack() {
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
                            Intent myIntent = new Intent(context, MyProfileActivity.class);
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

        apply.setOnClickListener(view -> {

            String text = myUsername.getText().toString();
            CollectionReference docRef = db.collection("users");
            docRef.whereEqualTo("username", text).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            Snackbar.make(apply, R.string.username_taken, Snackbar.LENGTH_SHORT).show();
                        } else {
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