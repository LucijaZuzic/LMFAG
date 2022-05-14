package com.example.lmfag.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lmfag.R;
import com.example.lmfag.fragments.MyProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.MyProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.MyProfileEventsPlayerFragment;
import com.example.lmfag.fragments.MyProfileFriendsFragment;
import com.example.lmfag.fragments.MyProfileInfoFragment;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.adapters.TabPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyProfileActivity extends MenuInterfaceActivity {
    private ViewPager2 viewPager;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        DrawerHelper.fillNavbarData(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        int x = preferences.getInt("selectedTab", 0);
        fillPager(x);
    }

    private void fillPager(int x) {
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(this,
                new MyProfileInfoFragment(), new MyProfileFriendsFragment(), new MyProfileAreasOfInterestFragment(),
                new MyProfileEventsOrganizerFragment(), new MyProfileEventsPlayerFragment());
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab);
        new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_person_24));
                        break;
                    case 1:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_groups_24));
                        break;
                    case 2:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_interests_24));
                        break;
                    case 3:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_emoji_events_24));
                        break;
                    case 4:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_calendar_today_24));
                        break;
                }
            }
        }).attach();

        viewPager.setCurrentItem(x);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selectedTab", 0);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}