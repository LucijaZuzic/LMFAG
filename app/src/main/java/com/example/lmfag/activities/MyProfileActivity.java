package com.example.lmfag.activities;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;



import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.lmfag.R;
import com.example.lmfag.utility.adapters.CustomAdapterAreaOfInterest;
import com.example.lmfag.utility.adapters.CustomAdapterFriends;
import com.example.lmfag.utility.DrawerHelper;
import com.example.lmfag.utility.adapters.TabPagerAdapter;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends MenuInterfaceActivity {
    private Context context = this;
    private RecyclerView recyclerViewFriends;
    private TabPagerAdapter tabPagerAdapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        DrawerHelper.fillNavbarData(this);
        fillPager();
    }

    private void fillPager() {
        tabPagerAdapter = new TabPagerAdapter(this);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab);
        new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("My info");
                        break;
                    case 1:
                        tab.setText("My friends");
                        break;
                    case 2:
                        tab.setText("My AOI");
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