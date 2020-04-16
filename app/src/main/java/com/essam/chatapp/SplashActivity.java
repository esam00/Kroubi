package com.essam.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.essam.chatapp.home.activity.HomeActivity;
import com.essam.chatapp.login.LoginActivity;
import com.essam.chatapp.utils.firebase.FirebaseHelper;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(FirebaseHelper.isUserLoggedIn()){
            Log.i(TAG, "isUserLoggedIn: YES");
            startActivity(new Intent(SplashActivity.this,HomeActivity.class));
        }
        else{
            Log.i(TAG, "isUserLoggedIn: No");
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }

}
