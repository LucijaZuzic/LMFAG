package com.example.lmfag.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.lmfag.R;
import com.example.lmfag.utility.AlarmScheduler;
import com.example.lmfag.utility.DrawerHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MenuInterfaceActivity extends AppCompatActivity {
    private boolean flag = false;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    public StorageReference storageRef = storage.getReference();

    private Handler handlerForAlarm;

    @Override
    protected void onResume() {
        countDownAlarmStart();
        super.onResume();
    }

    public void countDownAlarmStart() {
        handlerForAlarm = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handlerForAlarm.postDelayed(this, 10000);
                try {
                    AlarmScheduler.getAllSubscriberEvents(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handlerForAlarm.postDelayed(runnable, 10000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
    }

    public void logout() {
        editor.putString("userID", "");
        editor.apply();
        editor.putString("userUsername", "");
        editor.apply();
        editor.putString("userPicture", "");
        editor.apply();
        AlarmScheduler.cancelAllAlarms(this.getApplicationContext());
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            startActivity(new Intent(this, MainActivity.class));
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        DrawerHelper.fillNavbarData(this);

        String user = preferences.getString("userID", "");
        if (user.equals("")) {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }

        String theme = preferences.getString("theme", "");

        if (!theme.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            menu.getItem(0).setIcon(R.drawable.ic_baseline_nights_stay_24);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            menu.getItem(0).setIcon(R.drawable.ic_baseline_wb_sunny_24);
        }
        String imageShow = preferences.getString("showImage", "");
        if (imageShow.equals("true")) {
            menu.getItem(1).setIcon(R.drawable.ic_baseline_image_24);
        } else {
            menu.getItem(1).setIcon(R.drawable.ic_baseline_image_not_supported_24);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int resourceID = item.getItemId();
        final int logoutID = R.id.logout;
        final int menu_openID = R.id.menu_open;
        final int dayNightSwitchID = R.id.dayNightSwitch;
        final int imageSwitch = R.id.image_show;
        switch (resourceID) {
            case logoutID:
                logout();
                return true;
            case menu_openID:
                DrawerHelper.fillNavbarData(this);
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                
                if (flag) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                flag = !flag;
                return true;
            case dayNightSwitchID:
                String theme = preferences.getString("theme", "");

                if (!theme.equals("night")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putString("theme", "night");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putString("theme", "day");
                }
                editor.apply();
                recreate();
            case imageSwitch:
                String imageShow = preferences.getString("showImage", "");
                if (imageShow.equals("true")) {
                    editor.putString("showImage", "false");
                } else {
                    editor.putString("showImage", "true");
                }
                if (imageShow.equals("false")) {
                    item.setIcon(R.drawable.ic_baseline_image_24);
                } else {
                    item.setIcon(R.drawable.ic_baseline_image_not_supported_24);
                }
                editor.apply();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
