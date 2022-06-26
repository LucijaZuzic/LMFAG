package com.example.lmfag.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lmfag.R;
import com.example.lmfag.utility.SecureHash;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Objects;

public class ChangePasswordActivity extends MenuInterfaceActivity {
    private Context context;
    private ImageView apply;
    private Map<String, Object> old_data;
    private EditText myUsername, passwordEdit, passwordEditRepeat;
    private CheckBox checkBoxUsername, checkBoxPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        context = this;
        ImageView discard = findViewById(R.id.imageViewDiscard);
        apply = findViewById(R.id.imageViewApply);
        myUsername = findViewById(R.id.editTextUsername);
        passwordEdit = findViewById(R.id.editTextPassword);
        passwordEditRepeat = findViewById(R.id.editTextPasswordRepeat);
        checkBoxUsername = findViewById(R.id.checkBoxUsername);
        checkBoxPassword = findViewById(R.id.checkBoxPassword);

        fillUserData();
        createProfile();
        discard.setOnClickListener(view -> onBackPressed());
    }

    private void createProfile() {
        String name = preferences.getString("userID", "");
        apply.setOnClickListener(view -> {
            if (checkBoxPassword.isChecked()) {
                if (passwordEdit.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.password_short, Toast.LENGTH_SHORT).show();
                } else {
                    if (passwordEditRepeat.getText().toString().equals(passwordEdit.getText().toString())) {
                        try {
                            String new_hash = SecureHash.generateStrongPasswordHash(passwordEdit.getText().toString());
                            old_data.remove("password_hash");
                            old_data.put("password_hash", new_hash);
                            db.collection("users")
                                    .document(name)
                                    .set(old_data);
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), R.string.password_success_change, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.password_match, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if (checkBoxUsername.isChecked()) {
                String text = myUsername.getText().toString();
                CollectionReference docRef = db.collection("users");
                docRef.whereEqualTo("username", text).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            Toast.makeText(getApplicationContext(), R.string.username_taken, Toast.LENGTH_SHORT).show();
                        } else {
                            editor.putString("userUsername", text);
                            editor.apply();

                            old_data.remove("username");
                            old_data.put("username", text);
                            db.collection("users")
                                    .document(name)
                                    .set(old_data);
                            Toast.makeText(getApplicationContext(), R.string.username_success_change, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void fillUserData() {
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
                    old_data = document.getData();
                    if (old_data != null) {
                        myUsername.setText(Objects.requireNonNull(old_data.get("username")).toString());
                    }
                } else {
                    Intent myIntent = new Intent(context, MainActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            }
        });
    }
}