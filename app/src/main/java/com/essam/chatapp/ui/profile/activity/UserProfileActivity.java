package com.essam.chatapp.ui.profile.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;

public class UserProfileActivity extends AppCompatActivity {

    private Profile mProfile;
    private ImageView mProfileImage, mChatIv;
    private TextView mUserNameTv, mStatusTv, mPHoneTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initView();

        mProfile = getIntent().getParcelableExtra(Consts.PROFILE);
        if (mProfile != null){
            populateProfileInfo();
        }

    }

    private void initView() {
        mProfileImage = findViewById(R.id.profile_iv);
        mUserNameTv = findViewById(R.id.name_tv);
        mStatusTv = findViewById(R.id.status_tv);
        mPHoneTv = findViewById(R.id.phone_tv);
        mChatIv = findViewById(R.id.chat_iv);

        mChatIv.setOnClickListener(v -> {
            openChatActivity();
        });

        findViewById(R.id.image_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void populateProfileInfo() {
        Glide.with(this).load(mProfile.getAvatar()).placeholder(R.drawable.user_placeholder).into(mProfileImage);
        mUserNameTv.setText(mProfile.getUserName());
        mStatusTv.setText(mProfile.getStatus());
        mPHoneTv.setText(mProfile.getPhone());
    }

    private void openChatActivity(){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Consts.PROFILE, mProfile);
        startActivity(intent);
        finish();
    }
}