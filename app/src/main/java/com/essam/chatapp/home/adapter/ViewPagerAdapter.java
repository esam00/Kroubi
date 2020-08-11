package com.essam.chatapp.home.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.essam.chatapp.calls.fragment.CallsFragment;
import com.essam.chatapp.conversations.FragmentObserver;
import com.essam.chatapp.conversations.fragment.ChatsFragment;
import com.essam.chatapp.status.fragment.StatusFragment;

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
                Fragment chatFragment = new ChatsFragment();
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
        return 3;
    }

    public void updateFragments() {
        mObservers.notifyObservers();
    }
}
