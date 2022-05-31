package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lmfag.R;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.SecureHash;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestAdd;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterestRemove;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChangePasswordActivity extends MenuInterfaceActivity {
    private Context context = this;
    private ImageView apply, discard;
    Map<String, Object> old_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        discard = findViewById(R.id.imageViewDiscard);
        apply = findViewById(R.id.imageViewApply);
        fillUserData();
        createProfile();
        getBack();
        DrawerHelper.fillNavbarData(this);
    }

    private void getBack() {
        discard.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, MyProfileActivity.class);
            startActivity(myIntent);
        });
    }

    private void createProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
        apply.setOnClickListener(view -> {
            EditText username = findViewById(R.id.editTextUsername);
            EditText passwordEdit = findViewById(R.id.editTextPassword);
            EditText passwordEditRepeat = findViewById(R.id.editTextPasswordRepeat);
            CheckBox checkBoxUsername = findViewById(R.id.checkBoxUsername);
            CheckBox checkBoxPassword = findViewById(R.id.checkBoxPassword);
            if (checkBoxPassword.isChecked()) {
                if (passwordEditRepeat.getText() == passwordEdit.getText()) {
                    try {
                        String new_hash = SecureHash.generateStrongPasswordHash(passwordEdit.getText().toString());
                        old_data.remove("password_hash");
                        old_data.put("password_hash", new_hash);
                        db.collection("users")
                                .document(name)
                                .set(old_data);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(checkBoxPassword, R.string.password_success_change, Snackbar.LENGTH_SHORT);
                } else {
                    Snackbar.make(passwordEditRepeat, R.string.password_match, Snackbar.LENGTH_SHORT);
                }
            }
            if (checkBoxUsername.isChecked()) {
                old_data.remove("username");
                old_data.put("username", username.getText().toString());
                db.collection("users")
                        .document(name)
                        .set(old_data);
                Snackbar.make(checkBoxPassword, R.string.username_success_change, Snackbar.LENGTH_SHORT);
            }
        });
    }

    private void fillUserData() {
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
                    old_data = data;
                    EditText myUsername = findViewById(R.id.editTextUsername);
                    myUsername.setText(data.get("username").toString());

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