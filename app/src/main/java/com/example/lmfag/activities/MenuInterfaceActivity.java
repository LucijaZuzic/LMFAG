package com.example.lmfag.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.lmfag.R;
import com.example.lmfag.activities.MainActivity;

public class MenuInterfaceActivity extends AppCompatActivity {
    private boolean flag = false;

    public void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userID", "");
        editor.apply();
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = preferences.getString("theme", "");

        if (!theme.equals("night"))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            menu.getItem(0).setIcon(R.drawable.ic_baseline_nights_stay_24);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            menu.getItem(0).setIcon(R.drawable.ic_baseline_wb_sunny_24);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.menu_open:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (flag) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                flag = !flag;
                return true;
            case R.id.dayNightSwitch:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String theme = preferences.getString("theme", "");

                SharedPreferences.Editor editor = preferences.edit();
                if (!theme.equals("night"))
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putString("theme", "night");
                    editor.apply();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putString("theme", "day");
                    editor.apply();
                }
                recreate();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
