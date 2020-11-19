package com.essam.chatapp.ui.home.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.essam.chatapp.ui.home.fragments.calls.fragment.CallsFragment;
import com.essam.chatapp.ui.home.fragments.chat.FragmentObserver;
import com.essam.chatapp.ui.home.fragments.chat.HomeChatFragment;
import com.essam.chatapp.ui.home.fragments.status.fragment.StatusFragment;

import java.util.Observable;
import java.util.Observer;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private Observable mObservers = new FragmentObserver();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                mObservers.deleteObservers(); // Clear existing observers.
                Fragment chatFragment = new HomeChatFragment();
                mObservers.addObserver((Observer) chatFragment);
                return chatFragment;
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
        }

        return new CallsFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void updateFragments() {
        mObservers.notifyObservers();
    }
}
