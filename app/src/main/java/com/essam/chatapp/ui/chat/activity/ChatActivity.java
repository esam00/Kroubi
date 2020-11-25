package com.essam.chatapp.ui.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.chat.presenter.ChatContract;
import com.essam.chatapp.ui.chat.presenter.ChatPresenter;
import com.essam.chatapp.ui.chat.adapter.ChatAdapter;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.ui.profile.activity.UserProfileActivity;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.utils.SharedPrefrence;
import com.essam.chatapp.ui.photoEditor.PhotoEditorActivity;
import com.essam.chatapp.utils.Consts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,
        ChatContract.View, ChatAdapter.ChatListener {
    //views
    private RecyclerView recyclerView;
    private EditText messageEt;
    private LinearLayout filePickerLl;
    private LinearLayout emptyLayout;
    private ChatAdapter adapter;
    private List<Message> listMessages;
    private TextView titleTv, subTitleTv, loadingTv;
    private ImageView profileIv;

    //vars
    private List<Uri> mediaUriList;
    private SharedPrefrence preference;
    private Uri picUri;
    private boolean isTyping = false;

    private ChatContract.Presenter mPresenter;

    private final static String TAG = ChatActivity.class.getSimpleName();
    private Profile mOtherUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mPresenter = new ChatPresenter(this);
        initViews();
        getProfileFromIntent();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.msg_rv);
        messageEt = findViewById(R.id.send_msg_et);
        ImageView sendIv = findViewById(R.id.send_iv);
        ImageView attachFileIv = findViewById(R.id.attach_file_iv);
        ImageView openCameraIv = findViewById(R.id.open_camera);
        ImageView backImageIcon = findViewById(R.id.image_back);
        filePickerLl = findViewById(R.id.ll_file_picker);
        ImageView captureImageIv = findViewById(R.id.open_camera_iv);
        ImageView openGalleryIv = findViewById(R.id.open_gallery_iv);
        emptyLayout = findViewById(R.id.empty_ll);
        titleTv = findViewById(R.id.user_name_tv);
        subTitleTv = findViewById(R.id.subtitle_tv);
        profileIv = findViewById(R.id.profile_iv);
        LinearLayout userInfoLl = findViewById(R.id.user_info_ll);
        loadingTv = findViewById(R.id.loading_tv);

        mediaUriList = new ArrayList<>();
        preference = SharedPrefrence.getInstance(this);

        //recyclerView
        adapter = new ChatAdapter(this, this);
        listMessages = new ArrayList<>();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter.addAllMessages(listMessages);

        //onClickListener
        List<View> onClickViews = new ArrayList<>();
        onClickViews.add(sendIv);
        onClickViews.add(attachFileIv);
        onClickViews.add(openCameraIv);
        onClickViews.add(captureImageIv);
        onClickViews.add(openGalleryIv);
        onClickViews.add(backImageIcon);
        onClickViews.add(messageEt);
        onClickViews.add(userInfoLl);
        for (View view : onClickViews) view.setOnClickListener(this);

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (!isTyping) {
                        isTyping = true;
                        mPresenter.toggleIsTypingState(true);
                    }
                } else {
                    isTyping = false;
                    mPresenter.toggleIsTypingState(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * We could just pass the user id through an intent then request its info,
     * But this will give a bad user experience if network fails..
     * So we initially display user profile as we receive from intent then later
     * we can subscribe to listen for profile changes such as online state
     */
    private void getProfileFromIntent() {
        if ((getIntent().hasExtra(Consts.PROFILE))) {
            mOtherUserProfile = getIntent().getParcelableExtra(Consts.PROFILE);
            if (mOtherUserProfile != null) {
                // populate ui with other user profile
                onUpdateProfileInfo(mOtherUserProfile);

                // init chat room to be ready for doing the work
                mPresenter.initChatRoom(SharedPrefrence.getInstance(this).getMyProfile(), mOtherUserProfile);

                //Now we have a starting point of chat experience.. getChat history
                getChatHistory();
            }
        } else {
            Log.e(TAG, "getProfileFromIntent: NO PROFILE FOUND!! ");
            finish();
        }
    }

    private void getChatHistory() {
        if (!ProjectUtils.isNetworkConnected(this)) {
            ProjectUtils.showToast(this, getString(R.string.check_network));
            loadingTv.setText(R.string.check_network);
            return;
        }
        mPresenter.getChatHistory();
    }

    private void sendTextMessage() {
        //get message that has been input by user
        if (!ProjectUtils.isEditTextFilled(messageEt)) {
            Toast.makeText(this, R.string.type_message, Toast.LENGTH_SHORT).show();
        } else {
            String inputMessage = messageEt.getText().toString().trim();
            mPresenter.sendTextMessage(inputMessage);
            messageEt.setText("");
        }
    }

    private void sendMediaMessages(String inputMessage){
        if (inputMessage.isEmpty())
            inputMessage = getString(R.string.photo);

        mPresenter.sendMediaMessages(inputMessage, mediaUriList);
    }

    private void listenForOtherProfileChanges(){
        mPresenter.getUserProfileInfo(mOtherUserProfile.getId());
    }

    // ----------------------------------- Presenter CallBacks -----------------------------------------

    @Override
    public void onCheckFirstTimeChat(boolean isFirstTime) {
        if (isFirstTime){
            emptyLayout.setVisibility(View.VISIBLE);
            loadingTv.setVisibility(View.GONE);
            listenForOtherProfileChanges();
        }
        else{
            emptyLayout.setVisibility(View.GONE);
            loadingTv.setVisibility(View.VISIBLE);
            // Wait 1 second and then listen for profile changes
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listenForOtherProfileChanges();
                }
            },2000);
        }
    }

    @Override
    public void onNewMessageAdded(Message message) {
        if (loadingTv.getVisibility() == View.VISIBLE || emptyLayout.getVisibility() == View.VISIBLE ) {
            loadingTv.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        }
        listMessages.add(message);
        adapter.addAllMessages(listMessages);
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onMessageUpdated(Message message) {
        adapter.updateMessage(message);
    }

    @Override
    public void onToggleIsTyping(boolean isTyping) {
        if (isTyping)
            subTitleTv.setText(R.string.typing);
        else
            subTitleTv.setText(R.string.online);
    }

    @Override
    public void onUpdateProfileInfo(Profile profile) {
        titleTv.setText(mOtherUserProfile.getUserName());

        Glide.with(this).load(profile.getAvatar())
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(profileIv);

        if (profile.isOnline()) {
            subTitleTv.setVisibility(View.VISIBLE);
        } else {
            subTitleTv.setVisibility(View.GONE);
        }
    }

    /*---------------------------------- Attachments ---------------------------------------------*/

    /**
     * this method opens device's media to choose an image
     */
    private void openGalleryChooser() {
        if (ProjectUtils.hasPermissionInManifest(this, Consts.PICK_IMAGES_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            animateFilePickerDown();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, Consts.PICK_IMAGES_REQUEST);
        }
    }

    /**
     * this method opens device's camera to capture an image
     */
    private void openCamera() {
        if (ProjectUtils.hasPermissionInManifest(this, Consts.CAPTURE_IMAGE_REQUEST, Manifest.permission.CAMERA)) {
            try {
                // TODO: 4/26/2020 please handle camera the right way
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getOutputMediaFile();
                if (!file.exists()) {
                    try {
                        ProjectUtils.pauseProgressDialog();
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.asd", newFile);
                    picUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", file);
                } else {
                    picUri = Uri.fromFile(file); // create
                }

                preference.setValue(Consts.IMAGE_URI_CAMERA, picUri.toString());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri); // set the image file
                startActivityForResult(intent, Consts.CAPTURE_IMAGE_REQUEST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openPhotoEditor(String imageUri, int requestCode) {
        Intent intent = new Intent(this, PhotoEditorActivity.class);
        intent.putExtra(Consts.EDIT_PHOTO, imageUri);
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, Consts.EDIT_PHOTO_REQUEST);
    }

    private void openProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(Consts.USER, mOtherUserProfile);
        startActivity(intent);
    }

    private File getOutputMediaFile() {
        String root = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File mediaStorageDir = new File(root, Consts.APP_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                Consts.APP_NAME + timeStamp + ".png");

        String imageName = Consts.APP_NAME + timeStamp + ".png";
        return mediaFile;
    }

    private void toggleFilePicker() {
        if (filePickerLl.getVisibility() == View.VISIBLE) {
            animateFilePickerDown();
        } else {
            animateFilePickerUp();
        }
    }

    private void animateFilePickerUp() {
        filePickerLl.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeInUp)
                .duration(200)
                .playOn(filePickerLl);
    }

    private void animateFilePickerDown() {
        YoYo.with(Techniques.FadeOutDown)
                .duration(200)
                .playOn(filePickerLl);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                filePickerLl.setVisibility(View.GONE);
            }
        }, 200);

    }

    /* --------------------------------- Activity callbacks -----------------------------------*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // image has picked from gallery
                case Consts.PICK_IMAGES_REQUEST:
                    if (data != null && data.getData() != null) {
                        if (data.getClipData() == null) {
                            mediaUriList = new ArrayList<>();
                            Uri uri = data.getData();
                            openPhotoEditor(uri.toString(), Consts.PICK_IMAGES_REQUEST);
                            mediaUriList.add(uri);
                        } else {
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                mediaUriList.add(data.getClipData().getItemAt(i).getUri());
                            }
                        }
                    }
                    break;

                // image has been captured from camera
                case Consts.CAPTURE_IMAGE_REQUEST:
                    if (picUri != null) {
                        picUri = Uri.parse(preference.getValue(Consts.IMAGE_URI_CAMERA));
                        mediaUriList = new ArrayList<>();
                        openPhotoEditor(picUri.toString(), Consts.CAPTURE_IMAGE_REQUEST);
                        mediaUriList.add(picUri);
                    } else {
                        picUri = Uri.parse(preference.getValue(Consts.IMAGE_URI_CAMERA));
                        mediaUriList = new ArrayList<>();
                        openPhotoEditor(picUri.toString(), Consts.CAPTURE_IMAGE_REQUEST);
                        mediaUriList.add(picUri);
                    }
                    break;

                // resulting photo and caption from photo editor
                case Consts.EDIT_PHOTO_REQUEST:
                    if (data != null && data.getExtras() != null && data.getStringExtra("message") != null) {
                        String inputMessage = data.getStringExtra("message");
                        assert inputMessage != null;
                        sendMediaMessages(inputMessage);
                    } else {
                        Log.i(TAG, "onActivityResult: no Image was retrived ");
                        mediaUriList.clear();
                        if (data != null && data.getIntExtra("requestCode", -1) == Consts.PICK_IMAGES_REQUEST)
                            openGalleryChooser();
                    }

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
                    openCamera();
                    break;

                case Consts.PICK_IMAGES_REQUEST:
                    openGalleryChooser();
            }
        } else {
            Log.i(TAG, "onRequestPermissionsResult: capture image permission denied ");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_info_ll:
                openProfile();
                break;

            case R.id.image_back:
                onBackPressed();
                break;

            case R.id.send_iv:
                sendTextMessage();
                break;

            case R.id.send_msg_et:
                if (filePickerLl.getVisibility() == View.VISIBLE) {
                    animateFilePickerDown();
                }
                break;

            case R.id.attach_file_iv:
                openGalleryChooser();
//                openFilePickerDialog();
//                toggleFilePicker();
                break;

            case R.id.open_camera:
            case R.id.open_camera_iv:
                openCamera();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // make up button behave like back button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (filePickerLl.getVisibility() == View.VISIBLE) {
            animateFilePickerDown();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mPresenter.toggleOnlineState(false);
        mPresenter.toggleIsTypingState(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mPresenter.toggleOnlineState(true);
        super.onResume();
    }

    /*-----------------------------------Adapter Callbacks---------------------------------------*/
    @Override
    public void onUpdateComingMessageAsSeen(String messageId) {
        mPresenter.updateComingMessageAsSeen(messageId);
    }

//    private void openFilePickerDialog() {
//        // TODO: 2/17/2020 make a custom dialog to present filePiker layout with nice animation
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Get the layout inflater
//        LayoutInflater inflater = getLayoutInflater();
//        View filePicker = inflater.inflate(R.layout.layout_file_picker, null);
//
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the
//        // dialog layout
//        builder.setView(inflater.inflate(R.layout.layout_file_picker, null));
//
//        builder.create();
//        builder.show();
//
//        YoYo.with(Techniques.Pulse)
//                .duration(200)
//                .playOn(filePicker);
//    }
}
