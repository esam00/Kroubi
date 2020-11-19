package com.essam.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.ui.home.activity.HomeActivity;
import com.essam.chatapp.ui.on_boarding.OnBoardingActivity;
import com.essam.chatapp.ui.profile.activity.CompleteProfileActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.SharedPrefrence;
import com.google.firebase.FirebaseApp;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    // 1- User is already logged in :
    //      * if profile not completed >> Go to complete profile
    //      * else  >> Go to Home
    // 2- User not logged in   >> Go to OnBoarding
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // just give it one second to display splash screen
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(FirebaseManager.getInstance().isUserLoggedIn()){
            Log.i(TAG, "isUserLoggedIn: YES");
            if (SharedPrefrence.getInstance(this).getBooleanValue(Consts.PROFILE_COMPLETED)){
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            }else {
                startActivity(new Intent(SplashActivity.this, CompleteProfileActivity.class));
            }
        }
        else{
            Log.i(TAG, "isUserLoggedIn: No");
            startActivity(new Intent(SplashActivity.this, OnBoardingActivity.class));
        }
        finish();
    }
}
