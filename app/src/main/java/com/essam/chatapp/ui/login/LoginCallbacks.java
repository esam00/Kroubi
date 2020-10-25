package com.essam.chatapp.ui.login;

public interface LoginCallbacks {

    void onInvalidPhoneNumber();

    void onInvalidVerificationCode();

    void onVerificationCodeSent(String code);

    void onLoginSuccess(String userName);
}
