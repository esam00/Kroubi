package com.essam.chatapp.ui.home.activity;

import androidx.annotation.NonNull;

import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.models.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class HomePresenter implements HomeContract.Presenter {
    private FirebaseManager mFirebaseManager;
    private HomeContract.View mView;
    private ValueEventListener mProfileEventListener;

    public HomePresenter(HomeContract.View view) {
        mView = view;
        mFirebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public void getProfileInfo() {
        mProfileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile !=null){
                    mView.onLoadProfileSuccess(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mView.onLoadProfileFailed(error.getMessage());
            }
        };
        mFirebaseManager.getCurrentUserProfile(mProfileEventListener);
    }

    @Override
    public void detachView() {
        mFirebaseManager.removeCurrentUserProfileListener(mProfileEventListener);
        mView = null;
    }
}
