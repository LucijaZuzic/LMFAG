package com.example.lmfag.utility.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lmfag.fragments.MyProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.MyProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.MyProfileEventsPlayerFragment;
import com.example.lmfag.fragments.MyProfileEventsSubscriberFragment;
import com.example.lmfag.fragments.MyProfileFriendsFragment;
import com.example.lmfag.fragments.MyProfileInfoFragment;

public class TabPagerAdapterMyProfile extends FragmentStateAdapter {

    public TabPagerAdapterMyProfile(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        System.out.println("*****" + position);
        switch (position) {
            case 0:
                fragment = new MyProfileInfoFragment();
                break;
            case 1:
                fragment = new MyProfileFriendsFragment();
                break;
            case 2:
                fragment = new MyProfileAreasOfInterestFragment();
                break;
            case 3:
                fragment = new MyProfileEventsOrganizerFragment();
                break;
            case 4:
                fragment = new MyProfileEventsPlayerFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
