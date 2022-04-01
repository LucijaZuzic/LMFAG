package com.example.lmfag;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfile extends AppCompatActivity {
    boolean blocked = false;
    Context context = this;
    List<String> areas_array = new ArrayList<String>();
    List<Double> points_array = new ArrayList<Double>();
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        fillSpinner();
        fillUserData();
        addAreaOfInterest();
        removeAreaOfInterest();
        createProfile();
        getBack();
        showAreasOfInterest();
        changeProfilePicture();
    }

    void changeProfilePicture() {
        CircleImageView circleImageView = findViewById(R.id.profile_image);
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();

                            String realPath = ImageFilePath.getPath(EditProfile.this, data.getData());
//                          realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                CircleImageView imageView = findViewById(R.id.profile_image);
                                imageView.setImageBitmap(bitmap);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
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
                areas_array.remove(areas_array.indexOf(text));
                points_array.remove(areas_array.indexOf(text));
                RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
            } else {
                Snackbar.make(floatingActionButtonRemoveAreaOfInterest, R.string.area_of_interest_not_present, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    void getBack() {
        TextView discard = findViewById(R.id.buttonDiscard);
        if (blocked == true) {
            Snackbar.make(discard, R.string.go_back_upload, Snackbar.LENGTH_SHORT).show();
        }
        discard.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, MainActivity.class);
            startActivity(myIntent);
        });
    }
    void createProfile() {
        TextView apply = findViewById(R.id.buttonApply);
        apply.setOnClickListener(view -> {
            TextView myUsername = findViewById(R.id.editTextUsername);
            TextView myLocation = findViewById(R.id.editTextMyLocation);
            TextView myDescription = findViewById(R.id.editTextMyDescription);
            Map<String, Object> docData = new HashMap<>();
            docData.put("username", myUsername.getText().toString());
            docData.put("location", myLocation.getText().toString());
            docData.put("description", myDescription.getText().toString());
            docData.put("points_rank", 0.0);
            docData.put("areas_of_interest", areas_array.toString());
            docData.put("points_levels", points_array.toString());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(user.getUid())
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Log.d(TAG, "DocumentSnapshot successfully written!");
                            Snackbar.make(apply, R.string.write_success, Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(apply, R.string.write_failed, Snackbar.LENGTH_SHORT).show();
                            //Log.w(TAG, "Error writing document", e);
                        }
                    });
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("profile_pictures/" + user.getUid());
            UploadTask uploadTask = imagesRef.putFile(uri);
            blocked = true;
            Snackbar.make(myDescription, R.string.image_upload_started, Snackbar.LENGTH_SHORT).show();
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    blocked = false;
                    Snackbar.make(myDescription, R.string.image_upload_finished, Snackbar.LENGTH_SHORT).show();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
        });
    }

    void fillUserData() {
        LinearLayout ll = findViewById(R.id.main_layout);
        ll.setVisibility(View.INVISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();

                        TextView myUsername = findViewById(R.id.editTextUsername);
                        TextView myLocation = findViewById(R.id.editTextMyLocation);
                        TextView myDescription = findViewById(R.id.editTextMyDescription);
                        RecyclerView recyclerViewAreasOfInterest = findViewById(R.id.recyclerViewAreasOfInterest);
                        myUsername.setText(data.get("username").toString());
                        myLocation.setText(data.get("location").toString());
                        String area_string = data.get("areas_of_interest").toString();
                        String[] area_string_array = area_string.substring(1,area_string.length() - 1).split(", ");
                        areas_array = new ArrayList<String>();
                        for (int i = 0; i < area_string_array.length; i++) {
                            areas_array.add(area_string_array[i]);
                        }
                        String points_string = data.get("points_levels").toString();
                        String[] points_string_array = points_string.substring(1,points_string.length() - 1).split(", ");
                        points_array = new ArrayList<Double>();
                        for (int i = 0; i < points_string_array.length; i++) {
                            points_array.add(Double.parseDouble(points_string_array[i]));
                        }
                        CustomAdapterAreaOfInterest customAdapterAreaOfInterest = new CustomAdapterAreaOfInterest(areas_array, points_array);
                        recyclerViewAreasOfInterest.setAdapter(customAdapterAreaOfInterest);
                        myDescription.setText(data.get("description").toString());
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        StorageReference imagesRef = storageRef.child("profile_pictures/" + user.getUid());
                        final long ONE_MEGABYTE = 1024 * 1024;
                        CircleImageView circleImageView = findViewById(R.id.profile_image);
                        imagesRef.getBytes(7 * ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                circleImageView.setImageBitmap(bmp);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        ll.setVisibility(View.VISIBLE);
    }
}