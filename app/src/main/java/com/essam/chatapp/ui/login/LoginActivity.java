package com.essam.chatapp.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.home.activity.HomeActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.SharedPrefrence;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, LoginCallbacks {
    //view
    private EditText mPhoneNumberEditText, mVerificationCodeEditText;
    private ProgressBar mPhoneProgressBar, mCodeProgress;
    private ConstraintLayout mPhoneEntryLayout, mCodeEntryLayout;

    //vars
    private String mVerificationCode;
    private SharedPrefrence preference;

    private LoginPresenter mLoginPresenter = new LoginPresenter(this);
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        initViews();
    }

    private void initViews() {
        //initialize views
        mPhoneNumberEditText = findViewById(R.id.et_phone_number);
        mVerificationCodeEditText = findViewById(R.id.et_verification_code);
        mPhoneProgressBar = findViewById(R.id.send_code_progress);
        mCodeProgress = findViewById(R.id.submit_code_progress);
        Button sendCodeButton = findViewById(R.id.btn_send_code);
        Button verifyButton = findViewById(R.id.btn_submit_code);
        mPhoneEntryLayout = findViewById(R.id.layout_phone_entry);
        mCodeEntryLayout = findViewById(R.id.layout_verify_code_entry);
        preference = SharedPrefrence.getInstance(this);

        //set click listeners
        verifyButton.setOnClickListener(this);
        sendCodeButton.setOnClickListener(this);
    }

    /**
     * The first scenario of {user signing in} is to request a verification code to be sent to
     * the phone number provided by user
     */
    private void getVerificationCode() {
        String phoneNumber = mPhoneNumberEditText.getText().toString();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.verify_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        mPhoneProgressBar.setVisibility(View.VISIBLE);
        mLoginPresenter.getVerificationCode(phoneNumber);
        // TODO: 10/25/2020 Create a countDown timer for resending verification code
    }

    /**
     * When receiving verification code, we are to make PhoneAuthCredential object that combines the
     * code that was sent and the code that entered by user>> then sign in with this Credential
     * using firebase
     */
    private void signInWithPhoneCredential() {
        String enteredCode = mVerificationCodeEditText.getText().toString();
        if (enteredCode.isEmpty()) {
            Toast.makeText(this, R.string.verify_sign_in_code, Toast.LENGTH_SHORT).show();
            return;
        }

        mCodeProgress.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationCode, enteredCode);
        mLoginPresenter.signInWithPhoneCredential(credential);
    }

    private void swapLayout(){
        mPhoneEntryLayout.setVisibility(View.GONE);
        mCodeEntryLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_code:
                getVerificationCode();
                break;
            case R.id.btn_submit_code:
                signInWithPhoneCredential();
                break;
        }
    }

    @Override
    public void onInvalidPhoneNumber() {
        mPhoneProgressBar.setVisibility(View.INVISIBLE);
        mPhoneNumberEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_login_error_gb));
        mPhoneNumberEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_phone_red,0,0,0);
        Toast.makeText(LoginActivity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInvalidVerificationCode() {
        mCodeProgress.setVisibility(View.INVISIBLE);
        mVerificationCodeEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_login_error_gb));
        Toast.makeText(LoginActivity.this, R.string.invalid_verification_code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVerificationCodeSent(String code) {
        // check for credentials manually
        mVerificationCode = code;
        swapLayout();
    }

    @Override
    public void onLoginSuccess(String userName) {
        preference.setValue(Consts.USER_NAME, userName);
        Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        Log.i(TAG, "onLoginSuccess: " + R.string.msg_login_success);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoginPresenter.detachView();
    }
}
