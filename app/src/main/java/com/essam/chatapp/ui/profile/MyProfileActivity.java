package com.essam.chatapp.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.MenuItem;
import android.view.View;

import com.essam.chatapp.R;

public class MyProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void excludeFadeWhenTransition(){
        View decor = getWindow().getDecorView();
        android.transition.Transition transition = new Transition() {
            @Override
            public void captureStartValues(TransitionValues transitionValues) {

            }

            @Override
            public void captureEndValues(TransitionValues transitionValues) {

            }
        };
        transition.excludeChildren(decor.findViewById(R.id.action_bar_container),true);
        transition.excludeChildren(decor.findViewById(android.R.id.statusBarBackground),true);
        transition.excludeChildren(decor.findViewById(android.R.id.navigationBarBackground),true);

        getWindow().setEnterTransition(transition);
        getWindow().setExitTransition(transition);
    }}