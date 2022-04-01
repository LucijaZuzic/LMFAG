package com.example.lmfag;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class MainActivity extends AppCompatActivity {
    static String username = "";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView myBR = findViewById(R.id.imageViewRegister);
        ImageView myB = findViewById(R.id.imageViewLogin);
        EditText myET = findViewById(R.id.editTextPassword);
        EditText myU = findViewById(R.id.editTextUsername);
        Context context = this;
        myBR.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, CreateProfile.class);
            startActivity(myIntent);
        });
        myB.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference docRef = db.collection("users");
            docRef.whereEqualTo("username", myU.getText().toString()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        Snackbar.make(myB, "No user with username.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (task.getResult().size() > 1) {
                            Snackbar.make(myB, "Multiple users with username.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String pwd_hash = document.getData().get("password_hash").toString();
                                try {
                                    String my_hash = myET.getText().toString();
                                    if (SecureHash.validatePassword(my_hash, pwd_hash)) {
                                        Snackbar.make(myB, "Logged in!", Snackbar.LENGTH_SHORT).show();
                                        username = myU.getText().toString();
                                        Intent myIntent = new Intent(context, MyProfile.class);
                                        startActivity(myIntent);
                                    } else {
                                        Snackbar.make(myB, "Password incorrect.", Snackbar.LENGTH_SHORT).show();
                                    }
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        });
    }
}