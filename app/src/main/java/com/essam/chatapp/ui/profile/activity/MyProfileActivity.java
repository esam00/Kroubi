package com.essam.chatapp.ui.profile.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.profile.activity.edit_status.EditStatusActivity;
import com.essam.chatapp.ui.profile.presenter.ProfileContract;
import com.essam.chatapp.ui.profile.presenter.ProfilePresenter;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.utils.SharedPrefrence;

public class MyProfileActivity extends AppCompatActivity implements ProfileContract.View, View.OnClickListener {
    private ImageView profileIv;
    private ImageView editProfileIcon;
    private ImageView editNameIcon;
    private ImageView confirmEditNameIcon;
    private ConstraintLayout editNameContainer;
    private EditText editNameEt;
    private TextView userNameTv, statusTv, phoneTv;
    private Profile myProfile;
    private ProgressBar mProgressBar;

    private static final String TAG = "MyProfileActivity";
    private ProfileContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initViews();

        // display the cached profile data first to give the user a fast experience then listen
        // for remote data source changes
        myProfile = SharedPrefrence.getInstance(this).getMyProfile();
        populateProfileInfo();

        mPresenter = new ProfilePresenter(this, SharedPrefrence.getInstance(this));
        getProfileInfo();

    }

    private void initViews(){
        profileIv = findViewById(R.id.profile_iv);
        editProfileIcon = findViewById(R.id.edit_profile_iv);
        userNameTv = findViewById(R.id.user_name_tv);
        statusTv = findViewById(R.id.about_tv);
        phoneTv = findViewById(R.id.phone_tv);
        editNameIcon = findViewById(R.id.editNameIv);
        ImageView editAboutIcon = findViewById(R.id.editAboutIv);
        confirmEditNameIcon = findViewById(R.id.confirmEditNameIv);
        editNameEt = findViewById(R.id.edit_name_et);
        editNameContainer = findViewById(R.id.edit_name_cl);
        mProgressBar = findViewById(R.id.progress_bar);

        editNameIcon.setOnClickListener(this);
        editAboutIcon.setOnClickListener(this);
        confirmEditNameIcon.setOnClickListener(this);
        editProfileIcon.setOnClickListener(this);
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

    private void openEditStatusActivity(){
        Intent intent = new Intent(this, EditStatusActivity.class);
        intent.putExtra(Consts.STATUS, myProfile.getStatus());
        startActivity(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (ProjectUtils.isNetworkConnected(this))
            mPresenter.uploadProfileImage(imageUri);
        else
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
    }

    private void getProfileInfo(){
       if (ProjectUtils.isNetworkConnected(this)) {
           mPresenter.getProfileInfo();
       }
    }

    private void updateProfileInfo(){
        mPresenter.updateProfile(myProfile);
    }

    private void populateProfileInfo(){
        userNameTv.setText(myProfile.getUserName());

        statusTv.setText(myProfile.getStatus());

        phoneTv.setText(myProfile.getPhone());

        Glide.with(this)
                .load(myProfile.getAvatar())
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(profileIv);
    }

    private void handleEditNameLayout(boolean editing) {
        editNameIcon.setEnabled(!editing);
        if (editing){
            editNameContainer.setVisibility(View.VISIBLE);
            editNameEt.requestFocus();
            editNameEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0){
                        confirmEditNameIcon.setImageResource(R.drawable.ic_check);
                    }else {
                        confirmEditNameIcon.setImageResource(R.drawable.ic_close);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }else {
            editNameEt.setText("");
            editNameContainer.setVisibility(View.GONE);
        }
    }

    private void handleConfirmEditName(){
        if (ProjectUtils.isEditTextFilled(editNameEt)){
            userNameTv.setText(editNameEt.getText().toString());
            myProfile.setUserName(editNameEt.getText().toString());
            updateProfileInfo();
        }

        handleEditNameLayout(false);
    }

    /* ---------------------------------- Activity Callbacks -----------------------------------*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editNameIv:
                handleEditNameLayout(true);
                break;

            case R.id.confirmEditNameIv:
                handleConfirmEditName();
                break;

            case R.id.edit_profile_iv:
                openGalleryChooser();
                break;

            case R.id.editAboutIv:
                openEditStatusActivity();
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
    }

    /* ------------------------------------ Presenter callbacks --------------------------------*/

    @Override
    public void onProfileDataLoadedOrChanged(Profile profile) {
        myProfile = profile;
        populateProfileInfo();
    }

    @Override
    public void onLoadProfileInfoFailed() {
        Toast.makeText(this, R.string.network_error_msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadingImage(boolean isUploading) {
        if (isUploading) {
            mProgressBar.setVisibility(View.VISIBLE);
            editProfileIcon.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            editProfileIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUploadProfileImageSuccess(String imageUrl) {
        myProfile.setAvatar(imageUrl);
        updateProfileInfo();
        Toast.makeText(this, R.string.updated_successfully, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadProfileImageFailed() {
        Toast.makeText(this, R.string.network_error_msg, Toast.LENGTH_SHORT).show();
    }
}