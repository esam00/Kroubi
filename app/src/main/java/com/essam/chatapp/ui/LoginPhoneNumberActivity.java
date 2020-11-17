package com.essam.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.verification.VerificationActivity;
import com.essam.chatapp.utils.Consts;

import com.essam.chatapp.utils.ProjectUtils;
import com.hbb20.CountryCodePicker;

public class LoginPhoneNumberActivity extends AppCompatActivity{
    //view
    private EditText mPhoneNumberEditText;
    private CountryCodePicker mCountryCodePicker;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);
//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions)
        initViews();
    }

    private void initViews() {
        mPhoneNumberEditText = findViewById(R.id.et_phone_number);
        mCountryCodePicker = findViewById(R.id.ccp);
        Button sendCodeButton = findViewById(R.id.btn_send_code);
        mCountryCodePicker.registerCarrierNumberEditText(mPhoneNumberEditText);

        //set click listeners
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhoneNumberAccepted()){
                    openVerificationActivity();
                }
            }
        });
    }

    private void openVerificationActivity() {
        if (ProjectUtils.isEmulator()){
            mPhoneNumber = "+1" + mPhoneNumber;
        }else {
            mPhoneNumber = mCountryCodePicker.getFullNumberWithPlus();
        }

        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra(Consts.PHONE, mPhoneNumber);
        startActivity(intent);
    }

    private boolean isPhoneNumberAccepted(){
        mPhoneNumber = mPhoneNumberEditText.getText().toString();
        if (mPhoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.verify_phone_number, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ProjectUtils.isPhoneNumberValid(mPhoneNumber)){
            mPhoneNumberEditText.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_login_error_gb));
            Toast.makeText(LoginPhoneNumberActivity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
