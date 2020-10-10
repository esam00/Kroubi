package com.essam.chatapp.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.profile.MyProfileActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout profileHeaderLayout;
    private ImageView profileIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
    }

    private void initViews() {
        profileHeaderLayout = findViewById(R.id.profile_header);
        profileIv = findViewById(R.id.profile_iv);

        profileHeaderLayout.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_header){
            openProfileWithTransition();
        }
    }

    private void openProfileWithTransition() {
        Intent intent = new Intent(this, MyProfileActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this,profileIv, getString(R.string.profile_transition));
        startActivity(intent,optionsCompat.toBundle());
    }

    private void excludeFadeWhenTransition(){
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container),true);
        fade.excludeTarget(decor.findViewById(android.R.id.statusBarBackground),true);
        fade.excludeTarget(decor.findViewById(android.R.id.navigationBarBackground),true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}