package com.example.lmfag.utility.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lmfag.fragments.ViewProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.ViewProfileEventsOrganizerFragment;
import com.example.lmfag.fragments.ViewProfileEventsPlayerFragment;
import com.example.lmfag.fragments.ViewProfileFriendsFragment;
import com.example.lmfag.fragments.ViewProfileInfoFragment;

public class TabPagerAdapter extends FragmentStateAdapter {
    private Fragment[] fragmentsList;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity, Fragment... fragments) {
        super(fragmentActivity);
        fragmentsList = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentsList[position];
    }

    @Override
    public int getItemCount() {
        return fragmentsList.length;
    }
}
