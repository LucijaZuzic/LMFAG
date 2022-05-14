package com.example.lmfag.utility.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lmfag.fragments.ViewProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.ViewProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.ViewProfileEventsPlayerFragment;
import com.example.lmfag.fragments.ViewProfileEventsSubscriberFragment;
import com.example.lmfag.fragments.ViewProfileFriendsFragment;
import com.example.lmfag.fragments.ViewProfileInfoFragment;

public class TabPagerAdapterViewProfile extends FragmentStateAdapter {

    public TabPagerAdapterViewProfile(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        System.out.println("*****" + position);
        switch (position) {
            case 0:
                fragment = new ViewProfileInfoFragment();
                break;
            case 1:
                fragment = new ViewProfileFriendsFragment();
                break;
            case 2:
                fragment = new ViewProfileAreasOfInterestFragment();
                break; 
            case 3:
                fragment = new ViewProfileEventsOrganizerFragment();
                break;
            case 4:
                fragment = new ViewProfileEventsPlayerFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
