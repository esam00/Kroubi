package com.essam.chatapp.ui.profile.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.home.activity.HomeActivity;
import com.essam.chatapp.ui.profile.presenter.ProfileContract;
import com.essam.chatapp.ui.profile.presenter.ProfilePresenter;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.utils.SharedPrefrence;


import de.hdodenhof.circleimageview.CircleImageView;

public class CompleteProfileActivity extends AppCompatActivity implements ProfileContract.View {

    private CircleImageView mProfileIv;
    private EditText nameEt;
    private Profile myProfile;
    private ProgressBar mProgressBar;

    private ProfileContract.Presenter mPresenter;
    private static final String TAG = "CompleteProfileActivity";
    private Button mSubmitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        mPresenter = new ProfilePresenter(this, SharedPrefrence.getInstance(this));
        initViews();

        // get current user profile info and subscribe for upcoming changes
        mPresenter.getProfileInfo();
    }

    private void initViews() {
        mProfileIv = findViewById(R.id.profile_iv);
        nameEt = findViewById(R.id.user_name_et);
        mProgressBar = findViewById(R.id.progress_bar);
        mSubmitBtn = findViewById(R.id.submitProfileBtn);

        mProfileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryChooser();
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProjectUtils.isNetworkConnected(CompleteProfileActivity.this))
                    updateProfile();
                else
                    Toast.makeText(CompleteProfileActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGalleryChooser() {
        // check for permission first
        if (ProjectUtils.hasPermissionInManifest(this,
                Consts.PICK_IMAGES_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, Consts.PICK_IMAGES_REQUEST);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (ProjectUtils.isNetworkConnected(this))
            mPresenter.uploadProfileImage(imageUri);
        else
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
    }

    private void updateProfile() {
        if (!ProjectUtils.isEditTextFilled(nameEt)) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            nameEt.requestFocus();
            return;
        }
        myProfile.setUserName(nameEt.getText().toString());
        mPresenter.updateProfile(myProfile);
        gotoHome();
    }

    private void setViewsEnabled(boolean isEnabled) {
        mProfileIv.setEnabled(isEnabled);
        mSubmitBtn.setEnabled(isEnabled);
    }

    private void gotoHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void updateUiProfileImage(String url) {
        Glide.with(this).load(url)
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(mProfileIv);
    }

    /*--------------------------------------Activity Callbacks-----------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // image has picked from gallery
                case Consts.PICK_IMAGES_REQUEST:
                    if (data != null && data.getData() != null) {
                        uploadProfileImage(data.getData());
                    }
                    break;

                // image has been captured from camera
                case Consts.CAPTURE_IMAGE_REQUEST:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult: capture image permission granted ");
            switch (requestCode) {

                case Consts.CAPTURE_IMAGE_REQUEST:
//                    openCamera();
                    break;

                case Consts.PICK_IMAGES_REQUEST:
                    openGalleryChooser();
            }
        } else {
            Log.i(TAG, "onRequestPermissionsResult: capture image permission denied ");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    /*--------------------------------------Presenter Callbacks-----------------------------------*/

    @Override
    public void onProfileDataLoadedOrChanged(Profile profile) {
        myProfile = profile;
        updateUiProfileImage(myProfile.getAvatar());
    }

    @Override
    public void onLoadProfileInfoFailed() {
        Toast.makeText(this, R.string.network_error_msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadingImage(boolean isUploading) {
        if (isUploading) {
            mProgressBar.setVisibility(View.VISIBLE);
            setViewsEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            setViewsEnabled(true);
        }
    }

    @Override
    public void onUploadProfileImageSuccess(String imageUrl) {
        myProfile.setAvatar(imageUrl);
        updateUiProfileImage(imageUrl);
        Toast.makeText(this, R.string.updated_successfully, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadProfileImageFailed() {
        Toast.makeText(this, R.string.upload_profile_failed, Toast.LENGTH_SHORT).show();
    }
}