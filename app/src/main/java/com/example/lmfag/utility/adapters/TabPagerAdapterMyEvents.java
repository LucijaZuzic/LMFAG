package com.example.lmfag.utility.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lmfag.fragments.MyProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.MyProfileEventsPlayerFragment;

public class TabPagerAdapterMyEvents extends FragmentStateAdapter {

    public TabPagerAdapterMyEvents(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        System.out.println("*****" + position);
        switch (position) {
            case 0:
                fragment = new MyProfileEventsOrganizerFragment();
                break;
            case 1:
                fragment = new MyProfileEventsPlayerFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

