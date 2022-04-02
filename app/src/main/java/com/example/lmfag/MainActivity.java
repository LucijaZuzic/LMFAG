package com.example.lmfag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class MainActivity extends AppCompatActivity {
    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
        Context context = this;
        if(!name.equalsIgnoreCase(""))
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(name);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Intent myIntent = new Intent(context, MyProfile.class);
                        startActivity(myIntent);
                        return;
                    }
                }
            });
        }
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        ImageView myBR = findViewById(R.id.imageViewRegister);
        ImageView myB = findViewById(R.id.imageViewLogin);
        EditText myET = findViewById(R.id.editTextPassword);
        EditText myU = findViewById(R.id.editTextUsername);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");
        if(!name.equalsIgnoreCase(""))
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(name);
            docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Intent myIntent = new Intent(context, MyProfile.class);
                                startActivity(myIntent);
                                return;
                            }
                        }
                    });
        }
        myBR.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, CreateProfile.class);
            startActivity(myIntent);
        });
        myB.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String text = myU.getText().toString();
            CollectionReference docRef = db.collection("users");
            docRef.whereEqualTo("username", text).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0 ){
                            Snackbar.make(myB, R.string.no_user_username, Snackbar.LENGTH_SHORT).show();
                        } else {
                            if (task.getResult().size() > 1 ){
                                Snackbar.make(myB, R.string.multiple_username, Snackbar.LENGTH_SHORT).show();
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String pwd_hash = document.getData().get("password_hash").toString();
                                    try {
                                        String my_hash = myET.getText().toString();
                                        if (SecureHash.validatePassword(my_hash, pwd_hash)) {
                                            Snackbar.make(myB, R.string.logged_in, Snackbar.LENGTH_SHORT).show();
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("userID", document.getId());
                                            editor.apply();
                                            Intent myIntent = new Intent(context, MyProfile.class);
                                            startActivity(myIntent);
                                        } else {
                                            Snackbar.make(myB, R.string.password_incorrect, Snackbar.LENGTH_SHORT).show();
                                        }
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else {
                    }
                }
            });
        });
    }
}