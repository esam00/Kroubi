package com.essam.chatapp.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.profile.activity.MyProfileActivity;
import com.essam.chatapp.utils.SharedPrefrence;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView profileIv;
    private TextView userNameTv, statusTv;
    private Profile myProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        getProfileInfo();
    }

    private void getProfileInfo() {
        myProfile = SharedPrefrence.getInstance(this).getMyProfile();
        populateProfileData();
    }

    private void populateProfileData() {
        userNameTv.setText(myProfile.getUserName());
        statusTv.setText(myProfile.getStatus());
        Glide.with(this)
                .load(myProfile.getAvatar())
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(profileIv);
    }

    private void initViews() {
        ConstraintLayout profileHeaderLayout = findViewById(R.id.profile_header);
        profileIv = findViewById(R.id.profile_iv);
        userNameTv = findViewById(R.id.name_tv);
        statusTv = findViewById(R.id.statusTv);

        findViewById(R.id.account).setOnClickListener(this);
        findViewById(R.id.chats).setOnClickListener(this);
        findViewById(R.id.notifications).setOnClickListener(this);
        findViewById(R.id.storage).setOnClickListener(this);
        findViewById(R.id.help).setOnClickListener(this);
        profileHeaderLayout.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_header:
                openProfileWithTransition();
                break;
            case R.id.account:
            case R.id.chats:
            case R.id.notifications:
            case R.id.storage:
                Toast.makeText(this, " Just a design!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.help:
                openPayPal();
        }
    }

    private void openPayPal(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://paypal.me/esammosbah?locale.x=en_US"));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProfileInfo();
    }

    private void openProfileWithTransition() {
        Intent intent = new Intent(this, MyProfileActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, profileIv, getString(R.string.profile_transition));
        startActivity(intent, optionsCompat.toBundle());
    }

//    private void excludeFadeWhenTransition(){
//        Fade fade = new Fade();
//        View decor = getWindow().getDecorView();
//        fade.excludeTarget(decor.findViewById(R.id.action_bar_container),true);
//        fade.excludeTarget(decor.findViewById(android.R.id.statusBarBackground),true);
//        fade.excludeTarget(decor.findViewById(android.R.id.navigationBarBackground),true);
//
//        getWindow().setEnterTransition(fade);
//        getWindow().setExitTransition(fade);
//    }
}