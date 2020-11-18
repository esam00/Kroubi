package com.essam.chatapp.ui.profile.activity.edit_status;

public class EditStatusContract {
    interface Presenter{

        void updateCurrentStatus(String status);

        void detachView();
    }

    interface View{

    }
}
