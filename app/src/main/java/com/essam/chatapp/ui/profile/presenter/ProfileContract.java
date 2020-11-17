package com.essam.chatapp.ui.profile.presenter;

import android.net.Uri;

import com.essam.chatapp.models.Profile;

public class ProfileContract {

    public interface Presenter{
        void getProfileInfo();

        void uploadProfileImage(Uri imageUri);

        void updateProfile(Profile profile);

        void detachView();
    }

    public interface View{
        void onProfileDataLoadedOrChanged(Profile profile);

        void onLoadProfileInfoFailed();

        void onUploadingImage(boolean isUploading);

        void onUploadProfileImageSuccess(String imageUrl);

        void onUploadProfileImageFailed();
    }
}
