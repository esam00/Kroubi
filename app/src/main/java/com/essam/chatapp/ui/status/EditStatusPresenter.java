package com.essam.chatapp.ui.status;

import com.essam.chatapp.firebase.FirebaseManager;

public class EditStatusPresenter implements EditStatusContract.Presenter {

    private FirebaseManager mFirebaseManager;
    private EditStatusContract.View mView;

    public EditStatusPresenter(FirebaseManager firebaseManager, EditStatusContract.View view) {
        mFirebaseManager = firebaseManager;
        mView = view;
    }

    @Override
    public void updateCurrentStatus(String status) {
        mFirebaseManager.updateUserStatus(status);
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
