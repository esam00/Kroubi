package com.essam.chatapp.ui.status;

public class EditStatusContract {
    interface Presenter{

        void updateCurrentStatus(String status);

        void detachView();
    }

    interface View{

    }
}
