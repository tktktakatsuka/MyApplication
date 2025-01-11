package com.tktkcompany.kakoRaceKeiba.ui.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class TabPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> tabTitles) {
        super(fragmentActivity);
        this.tabTitles = tabTitles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TabFragment.newInstance(tabTitles.get(position));
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }
}
