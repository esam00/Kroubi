package com.essam.chatapp.ui.login.verification;

import com.google.firebase.auth.PhoneAuthCredential;

public class VerificationContract {
    interface Presenter{
        void getVerificationCode(String phoneNumber);

        void signInWithPhoneCredential(PhoneAuthCredential phoneAuthCredential);

        void detachView();
    }

    interface View{
        void onVerifyPhoneNumberFailed();

        void onInvalidVerificationCode();

        void onVerificationCodeSent(String code);

        void onLoginSuccess(String userName);
    }
}
