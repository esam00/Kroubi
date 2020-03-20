package com.essam.chatapp.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.essam.chatapp.R;
import com.essam.chatapp.home.activity.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //view
    private EditText phoneNumberEditText, verificationCodeEditText;
    private Button loginButton, verifyButton;
    private ProgressBar progressBar;
    private RelativeLayout phoneRelativeLayout;

    //firebase
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;

    //vars
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        initCallBacks();
    }

    private void initViews(){
        //initialize views
        phoneNumberEditText = findViewById(R.id.et_phone_number);
        verificationCodeEditText = findViewById(R.id.et_verification_code);
        progressBar = findViewById(R.id.progress);
        loginButton = findViewById(R.id.btn_login);
        verifyButton = findViewById(R.id.btn_verify);
        phoneRelativeLayout = findViewById(R.id.rl_phone_entry);

        //set click listener
        loginButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
    }

    private void initCallBacks(){
        // callback for phone authentication
        // this call back basically overrides three methods
        // 1- onVerificationCompleted : this means verification automatically done and no need to enter verify code
        // 2- onCodeSent : returns a string verification code to users phone number so we have to compare this code with the code that entered by user
        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //check automatically for credentials
                signInWithPhoneCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(code, forceResendingToken);
                // check for credentials manually
                phoneNumberEditText.setVisibility(View.GONE);
                verificationCodeEditText.setVisibility(View.VISIBLE);
                mVerificationId = code;
                verifyButton.setText("Verify Code");
                phoneRelativeLayout.setVisibility(View.GONE);
            }
        };
    }

    /**
     * when calling this methods means that verification code has been sent , so this method will check
     * that edit text is not empty and then create PhoneAuthCredential object to use this credential for signing in
     * @param enteredCode content of verification edit text
     */
    private void verifyPhoneNumberWithCode(String enteredCode){
        if(enteredCode.isEmpty()){
            Toast.makeText(this, "please enter verification Code", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,enteredCode);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if(user!=null){
                        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    Map<String,Object> userMap = new HashMap<>();
                                    userMap.put("phone",user.getPhoneNumber());
                                    userMap.put("name",user.getPhoneNumber());
                                    mUserDb.updateChildren(userMap);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    userLoggedIn();
                }
            }
        });
    }

    private void userLoggedIn() {
        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user!=null){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void startPhoneNumberVerification() {
        String phoneNumber= phoneNumberEditText.getText().toString();
        if(phoneNumber.isEmpty()){
            Toast.makeText(this, "Please enter your phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                break;
            case R.id.btn_verify:
                String enteredCode = verificationCodeEditText.getText().toString();

                if(mVerificationId!=null){
                    verifyPhoneNumberWithCode(enteredCode);
                }else {
                    startPhoneNumberVerification();
                }
        }
    }
}
