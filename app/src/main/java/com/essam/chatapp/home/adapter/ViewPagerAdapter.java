package com.essam.chatapp.home.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.essam.chatapp.R;
import com.essam.chatapp.calls.fragment.CallsFragment;
import com.essam.chatapp.conversations.fragment.ChatsFragment;
import com.essam.chatapp.status.fragment.StatusFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,Context context) {
        super(fm, behavior);
        mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
        }

        return new CallsFragment();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
        case 0:
            return mContext.getString(R.string.chats).toUpperCase();
        case 1 :
            return mContext.getString(R.string.status).toUpperCase();
        case 2 :
            return mContext.getString(R.string.calls).toUpperCase();
    }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        //used to reload chats fragment i don't know why but will search more about it
        return POSITION_NONE;
    }
}
