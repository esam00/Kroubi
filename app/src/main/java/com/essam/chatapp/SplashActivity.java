package com.essam.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.essam.chatapp.home.activity.HomeActivity;
import com.essam.chatapp.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // just give it one second to display splash screen
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(isUserLoggedIn()){
            Log.i(TAG, "isUserLoggedIn: YES");
            startActivity(new Intent(SplashActivity.this,HomeActivity.class));
        }
        else{
            Log.i(TAG, "isUserLoggedIn: No");
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }

    private boolean isUserLoggedIn(){
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser !=null;

    }

}
