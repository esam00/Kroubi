package com.essam.chatapp.ui.verification;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.essam.chatapp.firebase.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class VerificationPresenter implements VerificationContract.Presenter {
    private VerificationContract.View mView;
    private FirebaseManager mFirebaseManager;

    private static final String TAG = VerificationPresenter.class.getSimpleName();

    public VerificationPresenter(VerificationContract.View view) {
        mView = view;
        mFirebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public void getVerificationCode(String phoneNumber) {
        // this call back basically overrides three methods
        // 1- onVerificationCompleted : this means verification automatically done and no need to enter verify code
        // 2- onCodeSent : returns a string verification code to users phone number so we have to
        // compare this code with the code that entered by user
        //check automatically for credentials
        PhoneAuthProvider.OnVerificationStateChangedCallbacks loginPhoneCallBack =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //check automatically for credentials
                signInWithPhoneCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: " + e.toString());
                mView.onVerifyPhoneNumberFailed();
            }

            @Override
            public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(code, forceResendingToken);
                mView.onVerificationCodeSent(code);
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,
                60,
                TimeUnit.SECONDS,
                (Activity) mView,
                loginPhoneCallBack);
    }

    @Override
    public void signInWithPhoneCredential(PhoneAuthCredential phoneAuthCredential) {
        mFirebaseManager.getFirebaseAuth().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Tell firebase manager that user has been successfully logged in
                    mFirebaseManager.updateUserAuthState(FirebaseManager.UserAuthState.LOGGED_IN);

                    // check if this is a new user or already registered user
                    checkIfUserExistInDataBase();

                    // update view
                    mView.onLoginSuccess();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mView.onInvalidVerificationCode();
            }
        });
    }

    private void checkIfUserExistInDataBase() {
        mFirebaseManager.checkIfUserExistInDataBase(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // If not found in database >> just push new user with basic info [Uid, phone]
                    addNewUserToDatabase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addNewUserToDatabase() {
        mFirebaseManager.addNewUserToDataBase();
    }

    @Override
    public void detachView(){
        mView = null;
    }
}
