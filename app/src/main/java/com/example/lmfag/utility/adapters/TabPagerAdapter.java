package com.example.lmfag.utility.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
