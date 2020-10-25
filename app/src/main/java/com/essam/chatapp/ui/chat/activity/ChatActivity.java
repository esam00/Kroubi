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
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.essam.chatapp.R;
import com.essam.chatapp.firebase.ChatHelper;
import com.essam.chatapp.ui.chat.adapter.ChatAdapter;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.User;
import com.essam.chatapp.models.Chat;
import com.essam.chatapp.ui.profile.UserProfileActivity;
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

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChatCallBacks {

    //views
    private RecyclerView recyclerView;
    private EditText messageEt;
    private LinearLayout filePickerLl, emptyLayout;
    private ChatAdapter adapter;
    private List<Message> listMessages;
    private TextView titleTv;

    //vars
    private String inputMessage;
    private List<String> mediaUriList;
    private SharedPrefrence prefrence;
    private Uri picUri;

    private ChatHelper mChatHelper = new ChatHelper(this);

    private final static String TAG = ChatActivity.class.getSimpleName();
    private User mOtherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        receiveIntents();
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


        mediaUriList = new ArrayList<>();
        prefrence = SharedPrefrence.getInstance(this);

        //recyclerView
        adapter = new ChatAdapter(this);
        listMessages = new ArrayList<>();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setMessagesData(listMessages);

        //onClickListener
        List<View> onClickViews = new ArrayList<>();
        onClickViews.add(sendIv);
        onClickViews.add(attachFileIv);
        onClickViews.add(openCameraIv);
        onClickViews.add(captureImageIv);
        onClickViews.add(openGalleryIv);
        onClickViews.add(backImageIcon);
        onClickViews.add(messageEt);
        onClickViews.add(titleTv);
        for (View view : onClickViews) view.setOnClickListener(this);
    }

    /**
     * there are two scenarios to open this chat activity :
     * 1- from home chats >> means this is not the first time
     * 2- from contacts activity >> then we need to check if there is a previous chat with this user
     */
    private void receiveIntents() {
        Intent intent = getIntent();
        if ((intent.hasExtra(Consts.USER))){
            mOtherUser = intent.getParcelableExtra(Consts.USER);
            assert mOtherUser != null;
            titleTv.setText(mOtherUser.getName());

            mChatHelper.checkForPreviousChatWith(mOtherUser);
        }
    }

    private void preSendMessage() {
        //get message that has been input by user
        inputMessage = messageEt.getText().toString().trim();

        if (TextUtils.isEmpty(inputMessage)) {
            Toast.makeText(this, "Please type a message to be sent", Toast.LENGTH_SHORT).show();
        } else {
            mChatHelper.sendMessage(inputMessage,mediaUriList);
            messageEt.setText("");
        }

    }

    // ----------------------------------- ChatCallBacks -----------------------------------------
    @Override
    public void onCheckPreviousConversationWithThisUser(boolean isFirstTime, Chat chat) {
        // TODO: 10/13/2020 fix this
//        this.isFirstTime = isFirstTime;
//
//        // pass this reference to adapter in order to handle and update seen state
//        adapter.setChatDp(mChatDb);
    }

    @Override
    public void onCheckFirstTimeChat(boolean isFirstTime) {
        if (!isFirstTime)
            emptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onNewMessageAdded(Message message) {
        updateUi(message);
    }

    @Override
    public void onMessageSeen(String messageId) {
        // changing means messages has been seen so update it in adapter
        for (int i = 0; i < listMessages.size(); i++) {
            if (listMessages.get(i).getMessageId().equals(messageId)) {
                adapter.updateSeen(i);
            }
        }
    }

    public void updateUi(Message message) {
        listMessages.add(message);
        adapter.setMessagesData(listMessages);
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    //---------------------------------- Attaches{} ---------------------------------------------

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

                prefrence.setValue(Consts.IMAGE_URI_CAMERA, picUri.toString());
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
        intent.putExtra(Consts.USER, mOtherUser);
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

    // ------------------------------------------- Overrides -----------------------------------

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
                            String uri = data.getData().toString();
                            openPhotoEditor(uri, Consts.PICK_IMAGES_REQUEST);
                            mediaUriList.add(uri);
//                            mediaUriList.add(uri);
                        } else {
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                mediaUriList.add(data.getClipData().getItemAt(i).toString());
                            }
                        }
                    }
                    break;

                // image has been captured from camera
                case Consts.CAPTURE_IMAGE_REQUEST:
                    if (picUri != null) {
                        picUri = Uri.parse(prefrence.getValue(Consts.IMAGE_URI_CAMERA));
                        mediaUriList = new ArrayList<>();
                        openPhotoEditor(picUri.toString(), Consts.CAPTURE_IMAGE_REQUEST);
                        mediaUriList.add(picUri.toString());
                    } else {
                        picUri = Uri.parse(prefrence.getValue(Consts.IMAGE_URI_CAMERA));
                        mediaUriList = new ArrayList<>();
                        openPhotoEditor(picUri.toString(), Consts.CAPTURE_IMAGE_REQUEST);
                        mediaUriList.add(picUri.toString());
                    }
                    break;

                // resulting photo and caption from photo editor
                case Consts.EDIT_PHOTO_REQUEST:
                    if (data != null && data.getExtras() != null && data.getStringExtra("message") != null) {
                        inputMessage = data.getStringExtra("message");
                        mChatHelper.sendMessage(inputMessage,mediaUriList);
                    } else {
                        Log.i(TAG, "onActivityResult: no Image was retrived ");
                        mediaUriList.clear();
                        if (data!=null && data.getIntExtra("requestCode", -1) == Consts.PICK_IMAGES_REQUEST)
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
            case R.id.user_name_tv:
                openProfile();
                break;

            case R.id.image_back:
                onBackPressed();
                break;

            case R.id.send_iv:
                preSendMessage();
                break;

            case R.id.send_msg_et:
                if (filePickerLl.getVisibility() == View.VISIBLE) {
                    animateFilePickerDown();
                }
                break;

            case R.id.attach_file_iv:
//                openFilePickerDialog();
                toggleFilePicker();
                break;

            case R.id.open_camera:
            case R.id.open_camera_iv:
                openCamera();
                break;

            case R.id.open_gallery_iv:
                openGalleryChooser();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
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
            mChatHelper.unSubScribeAllListeners();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mChatHelper.unSubScribeAllListeners();
        super.onDestroy();
    }

//    private void openFilePickerDialog() {
//        // TODO: 2/17/2020 make a custom dialog to present filePiker layout with nice animation
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Get the layout inflater
//        LayoutInflater inflater = getLayoutInflater();
//        View filePicker = inflater.inflate(R.layout.file_picker, null);
//
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the
//        // dialog layout
//        builder.setView(inflater.inflate(R.layout.file_picker, null));
//
//        builder.create();
//        builder.show();
//
//        YoYo.with(Techniques.Pulse)
//                .duration(200)
//                .playOn(filePicker);
//    }
}
