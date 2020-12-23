package com.essam.chatapp.ui.profile.presenter;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.essam.chatapp.firebase.data.FirebaseManager;
import com.essam.chatapp.firebase.data.FirebaseStorageManager;
import com.essam.chatapp.firebase.data.StorageCallbacks;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.SharedPrefrence;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfilePresenter implements ProfileContract.Presenter, StorageCallbacks.ProfileCallBack {

    private FirebaseManager mFirebaseManager;
    private FirebaseStorageManager mFirebaseStorageManager;
    private ProfileContract.View mView;
    private ValueEventListener mProfileEventListener;
    private SharedPrefrence mPreference;

    public ProfilePresenter(ProfileContract.View view, SharedPrefrence preference) {
        mView = view;
        mPreference = preference;
        mFirebaseManager = FirebaseManager.getInstance();
        mFirebaseStorageManager = FirebaseStorageManager.getInstance();
    }

    @Override
    public void getProfileInfo() {
        mProfileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Profile profile = snapshot.getValue(Profile.class);
                    if (profile != null){
                        mView.onProfileDataLoadedOrChanged(profile);
                        mPreference.setMyProfile(profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mView.onLoadProfileInfoFailed();
            }
        };

        mFirebaseManager.listenForCurrentUserProfileChanges(mProfileEventListener);
    }

    @Override
    public void uploadProfileImage(Uri imageUri) {
        mView.onUploadingImage(true);
        mFirebaseStorageManager.uploadProfileImage(imageUri,mFirebaseManager.getMyUid(),this);
    }

    @Override
    public void updateProfile(Profile profile) {
        profile.setOnline(true);
        profile.setId(mFirebaseManager.getMyUid());
        profile.setPhone(mFirebaseManager.getMyPhone());

        if (profile.getStatus() == null){
            profile.setStatus("Hey there, I'm using Kroubi");
        }

        if (profile.getUserName() == null)
            profile.setUserName(mFirebaseManager.getMyPhone());

        if (profile.getAvatar() == null){
            profile.setAvatar("");
        }
        mFirebaseManager.updateUserProfile(profile);
        mPreference.setBooleanValue(Consts.PROFILE_COMPLETED, true);
    }

    @Override
    public void detachView() {
        mView= null;
        if (mProfileEventListener != null)
            mFirebaseManager.removeCurrentUserProfileListener(mProfileEventListener);
    }

    @Override
    public void onUploadProfileSuccess(String imageUrl) {
        mView.onUploadingImage(false);
        mView.onUploadProfileImageSuccess(imageUrl);
    }

    @Override
    public void onUploadProfileFailed() {
        mView.onUploadingImage(false);
        mView.onUploadProfileImageFailed();
    }
}
