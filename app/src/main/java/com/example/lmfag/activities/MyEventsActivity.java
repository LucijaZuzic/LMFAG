package com.example.lmfag.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lmfag.R;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.adapters.TabPagerAdapterMyEvents;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyEventsActivity extends MenuInterfaceActivity {

    private com.example.lmfag.utility.adapters.TabPagerAdapterMyEvents TabPagerAdapterMyEvents;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        DrawerHelper.fillNavbarData(this);
        fillPager();
    }

    private void fillPager() {
        TabPagerAdapterMyEvents = new TabPagerAdapterMyEvents(this);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(TabPagerAdapterMyEvents);

        TabLayout tabLayout = findViewById(R.id.tab);
        new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_emoji_events_24));
                        break;
                    case 1:
                        tab.setIcon(getDrawable(R.drawable.ic_baseline_calendar_today_24));
                        break;
                }
            }
        }).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}