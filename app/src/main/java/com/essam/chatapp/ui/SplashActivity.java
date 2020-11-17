package com.essam.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.ui.home.activity.HomeActivity;
import com.essam.chatapp.ui.intro.OnBoardingActivity;
import com.google.firebase.FirebaseApp;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
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

        boolean isUserLoggedIn = FirebaseManager.getInstance().isUserLoggedIn();
        if(isUserLoggedIn){
            Log.i(TAG, "isUserLoggedIn: YES");
            startActivity(new Intent(SplashActivity.this,HomeActivity.class));
        }
        else{
            Log.i(TAG, "isUserLoggedIn: No");
            startActivity(new Intent(SplashActivity.this, OnBoardingActivity.class));
        }
        finish();
    }
}
