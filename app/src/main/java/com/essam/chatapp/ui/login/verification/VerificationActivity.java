package com.essam.chatapp.ui.login.verification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.home.activity.HomeActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.SharedPrefrence;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

import cn.iwgang.countdownview.CountdownView;

public class VerificationActivity extends AppCompatActivity implements VerificationContract.View  {

    private TextView headerTv, phoneTv, resendCodeTv;
    private OtpView mOtpView;
    private CountdownView mCountdownView;
    private SharedPrefrence preference;
    private String mOtpCode , mEnteredCode;
    private ProgressBar mProgressBar;
    private VerificationContract.Presenter mPresenter;
    private String mPhoneToValidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mPresenter = new VerificationPresenter(this);
        preference = SharedPrefrence.getInstance(this);

        initViews();
        receivePhoneNumber();
    }

    private void receivePhoneNumber() {
        Intent intent = getIntent();
        if (intent.hasExtra(Consts.PHONE)){
            mPhoneToValidate = intent.getStringExtra(Consts.PHONE);
            phoneTv.setText(mPhoneToValidate);
            headerTv.setText(getString(R.string.verify, mPhoneToValidate));

            mPresenter.getVerificationCode(mPhoneToValidate);
            handleCountDown(false);
        }
    }

    private void initViews() {
        headerTv = findViewById(R.id.verifyHeaderTv);
        phoneTv = findViewById(R.id.phone_tv);
        mOtpView = findViewById(R.id.otp_view);
        resendCodeTv = findViewById(R.id.resend_code_text_view);
        mCountdownView = findViewById(R.id.countdownView);
        mProgressBar = findViewById(R.id.progress_bar);

        mOtpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                mEnteredCode = otp;
                mOtpView.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                signInWithPhoneCredential();
            }
        });

        mCountdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                handleCountDown(true);
            }
        });

        resendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.getVerificationCode(mPhoneToValidate);
                handleCountDown(false);
            }
        });
    }

    /**
     * When receiving verification code, we are to make PhoneAuthCredential object that combines the
     * code that was sent and the code that entered by user>> then sign in with this Credential
     * using firebase
     */
    private void signInWithPhoneCredential() {
        if (mEnteredCode.isEmpty()) {
            Toast.makeText(this, R.string.verify_sign_in_code, Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mOtpCode, mEnteredCode);
        mPresenter.signInWithPhoneCredential(credential);
    }

    private void handleCountDown(boolean enableResend){
        if (enableResend){
            resendCodeTv.setEnabled(true);
            resendCodeTv.setTextColor(getResources().getColor(R.color.colorAccent));
            resendCodeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_resend_enabled,0,0,0);

        }else {
            resendCodeTv.setEnabled(false);
            resendCodeTv.setTextColor(getResources().getColor(R.color.light_gray));
            resendCodeTv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_resend_disabled,0,0,0);
            mCountdownView.start(TimeUnit.SECONDS.toMillis(60));
        }
    }

    @Override
    public void onVerifyPhoneNumberFailed() {
        Toast.makeText(this, R.string.verify_phone_failed, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onInvalidVerificationCode() {
        mOtpView.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, R.string.invalid_verification_code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVerificationCodeSent(String code) {
        mOtpCode = code;
    }

    @Override
    public void onLoginSuccess(String userName) {
        preference.setValue(Consts.USER_NAME, userName);
        Toast.makeText(this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}