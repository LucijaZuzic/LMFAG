package com.example.lmfag.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.example.lmfag.R;
import com.example.lmfag.utility.AlarmScheduler;
import com.example.lmfag.utility.SecureHash;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;


public class MainActivity extends BaseActivity {
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Request permission dialog
        ActivityResultLauncher<String[]> alarmPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            /* Do something Boolean scheduleExactAlarm = GetOrDefault.getOrDefault(result, Manifest.permission.SCHEDULE_EXACT_ALARM, false);
                            if (scheduleExactAlarm) {
                                // Alarm access granted.

                            }*/
                        }
                );
        // ...

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmPermissionRequest.launch(new String[]{
                    Manifest.permission.SCHEDULE_EXACT_ALARM,
            });
        }

        onStart(savedInstanceState);
    }

    protected void onStart(Bundle savedInstanceState) {
        Context context = this;
        ImageView imageViewRegister = findViewById(R.id.imageViewRegister);
        ImageView imageViewLogin = findViewById(R.id.imageViewLogin);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        EditText editTextUsername = findViewById(R.id.editTextUsername);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        String name = preferences.getString("userID", "");

        View.OnClickListener confirmLoginButtonListener = view -> {
            editor.putString("userUsername", "");
            editor.apply();
            editor.putString("friendOrganizer", "");
            editor.apply();
            editor.putString("friendOrganizerTimestamp", "");
            editor.apply();
            editor.putString("friendPlayer", "");
            editor.apply();
            editor.putString("friendPlayerTimestamp", "");
            editor.apply();
            editor.putString("userOrganizer", "");
            editor.apply();
            editor.putString("userOrganizerTimestamp", "");
            editor.apply();
            editor.putString("userPlayer", "");
            editor.apply();
            editor.putString("userPlayerTimestamp", "");
            editor.apply();
            editor.putString("userSubscriber", "");
            editor.apply();
            editor.putString("userSubscriberTimestamp", "");
            editor.apply();
            editor.putString("userUnrated", "");
            editor.apply();
            editor.putString("userUnratedTimestamp", "");
            editor.apply();


            editor.putString("userLocation", "");
            editor.apply();
            editor.putString("userRankPoints", "");
            editor.apply();
            editor.putString("userDescription", "");
            editor.apply();
            editor.putString("user_areas_of_interest", "");
            editor.apply();
            editor.putString("user_points_levels", "");
            editor.apply();

            editor.putString("friendLocation", "");
            editor.apply();
            editor.putString("friendRankPoints", "");
            editor.apply();
            editor.putString("friendDescription", "");
            editor.apply();
            editor.putString("friend_areas_of_interest", "");
            editor.apply();
            editor.putString("friend_points_levels", "");
            editor.apply();

            editor.putInt("selectedTab", 0);
            editor.apply();
            editor.putString("eventID", "");
            editor.apply();
            editor.putString("friendID", "");
            editor.apply();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String username = editTextUsername.getText().toString();
            CollectionReference docRef = db.collection("users");
            docRef.whereEqualTo("username", username).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        Toast.makeText(getApplicationContext(), R.string.no_user_username, Toast.LENGTH_SHORT).show();
                    } else {
                        if (task.getResult().size() > 1) {
                            Toast.makeText(getApplicationContext(), R.string.multiple_username, Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String pwd_hash = Objects.requireNonNull(document.getData().get("password_hash")).toString();
                                try {
                                    if (editTextPassword.getText() == null) {
                                        Toast.makeText(getApplicationContext(), R.string.password_incorrect, Toast.LENGTH_SHORT).show();
                                    } else {
                                        String my_value_to_hash = editTextPassword.getText().toString();
                                        if (SecureHash.validatePassword(my_value_to_hash, pwd_hash)) {
                                            Toast.makeText(getApplicationContext(), R.string.logged_in, Toast.LENGTH_SHORT).show();
                                            editor.putString("userID", document.getId());
                                            editor.apply();
                                            AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                                            AlarmScheduler.getAllReceivedFriendRequests(getApplicationContext());
                                            Intent myIntent = new Intent(context, MyProfileActivity.class);
                                            startActivity(myIntent);
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.password_incorrect, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        };

        ConnectivityManager connectionService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] network_information = connectionService.getAllNetworkInfo();
        boolean haveConnection = false;
        for (NetworkInfo info : network_information) {
            haveConnection = haveConnection || info.isConnected();
        }

        CardView noConnView = findViewById(R.id.no_conn_card_view);
        LinearLayoutCompat loginLayout = findViewById(R.id.login_ui);

        if (!haveConnection) {
            noConnView.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);

        } else {
            noConnView.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            imageViewLogin.setOnClickListener(confirmLoginButtonListener);
            imageViewRegister.setOnClickListener(view -> {
                Intent myIntent = new Intent(context, CreateProfileActivity.class);
                startActivity(myIntent);
                finish();
            });

            if (!name.equals("")) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("users").document(name);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                            AlarmScheduler.getAllReceivedFriendRequests(getApplicationContext());
                            Intent myIntent = new Intent(context, MyProfileActivity.class);
                            startActivity(myIntent);
                            finish();
                        }
                    }
                });
            }
        }
    }
}