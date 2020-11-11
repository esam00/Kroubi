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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.profile.MyProfileActivity;
import com.essam.chatapp.utils.Consts;

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
        Intent intent = getIntent();
        if(intent.hasExtra(Consts.PROFILE)){
            myProfile = intent.getParcelableExtra(Consts.PROFILE);
            populateProfileData();
        }
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
        switch (v.getId()){
            case R.id.profile_header:
                openProfileWithTransition();
                break;
            case R.id.account:
            case R.id.chats:
            case R.id.notifications:
            case R.id.storage:
            case R.id.help:
                Toast.makeText(this, " Just a design!", Toast.LENGTH_SHORT).show();
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