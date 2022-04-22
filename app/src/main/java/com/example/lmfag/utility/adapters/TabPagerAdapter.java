package com.example.lmfag.utility.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lmfag.fragments.MyProfileAreasOfInterestFragment;
import com.example.lmfag.fragments.MyProfileFriendsFragment;
import com.example.lmfag.fragments.MyProfileInfoFragment;

public class TabPagerAdapter extends FragmentStateAdapter {

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
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
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
