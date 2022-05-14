package com.example.lmfag.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.lmfag.R;
import com.example.lmfag.receivers.ConnectionChangeReceiver;
import com.example.lmfag.utility.SecureHash;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean receiverRegistered = preferences.getBoolean("receiverRegistered", false);
        if (!receiverRegistered) {
            ConnectionChangeReceiver connectionChangeReceiver = new ConnectionChangeReceiver();
            getApplication().registerReceiver(connectionChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            preferences.edit().putBoolean("receiverRegistered", true).apply();
        }

        onStart(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        // Do nothing to prevent going back to previous activity
    }

    protected void onStart(Bundle savedInstanceState) {
        Context context = this;
        ImageView myBR = findViewById(R.id.imageViewRegister);
        ImageView myB = findViewById(R.id.imageViewLogin);
        EditText myET = findViewById(R.id.editTextPassword);
        EditText myU = findViewById(R.id.editTextUsername);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = preferences.getString("userID", "");

        View.OnClickListener confirmLoginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String text = myU.getText().toString();
                CollectionReference docRef = db.collection("users");
                docRef.whereEqualTo("username", text).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                Snackbar.make(myB, R.string.no_user_username, Snackbar.LENGTH_SHORT).show();
                            } else {
                                if (task.getResult().size() > 1) {
                                    Snackbar.make(myB, R.string.multiple_username, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String pwd_hash = document.getData().get("password_hash").toString();
                                        try {
                                            String my_hash = myET.getText().toString();
                                            boolean hack = true;
                                            if (SecureHash.validatePassword(my_hash, pwd_hash) || hack) {
                                                Snackbar.make(myB, R.string.logged_in, Snackbar.LENGTH_SHORT).show();
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("userID", document.getId());
                                                editor.apply();
                                                Intent myIntent = new Intent(context, MyProfileActivity.class);
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
                        }
                    }
                });
            }
        };

        ConnectivityManager connectionService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectionService.getAllNetworkInfo();
        boolean haveConnection = false;
        for (NetworkInfo info : networkInfos) {
            haveConnection = haveConnection || info.isConnected();
        }

        CardView noConnView = findViewById(R.id.no_conn_card_view);
        LinearLayout.LayoutParams cardViewParams = (LinearLayout.LayoutParams) noConnView.getLayoutParams();
        LinearLayoutCompat loginLayout = findViewById(R.id.login_ui);
        LinearLayout.LayoutParams loginLayoutParams = (LinearLayout.LayoutParams) loginLayout.getLayoutParams();

        if (!haveConnection) {
            cardViewParams.weight = 0.2f;
            noConnView.setLayoutParams(cardViewParams);

            loginLayoutParams.weight = 0.8f;
            loginLayout.setLayoutParams(loginLayoutParams);
        } else {
            cardViewParams.weight = 0.0f;
            noConnView.setLayoutParams(cardViewParams);

            loginLayoutParams.weight = 1.f;
            loginLayout.setLayoutParams(loginLayoutParams);

            myET.setOnClickListener(confirmLoginButtonListener);
            myBR.setOnClickListener(view -> {
                Intent myIntent = new Intent(context, CreateProfileActivity.class);
                startActivity(myIntent);
                finish();
            });

            if(!name.equalsIgnoreCase("")) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("users").document(name);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Intent myIntent = new Intent(context, MyProfileActivity.class);
                            startActivity(myIntent);
                            finish();
                            return;
                        }
                    }
                });
            }

            myB.setOnClickListener(confirmLoginButtonListener);
        }
    }
}