package com.essam.chatapp.ui.home.activity;

import com.essam.chatapp.models.Profile;

public class HomeContract {
    interface Presenter{
        void getProfileInfo();
        void detachView();
    }

    interface View{
        void onLoadProfileSuccess(Profile profile);
        void onLoadProfileFailed(String msg);
    }
}
