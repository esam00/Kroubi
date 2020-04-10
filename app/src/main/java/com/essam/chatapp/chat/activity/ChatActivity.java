package com.essam.chatapp.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.essam.chatapp.R;
import com.essam.chatapp.chat.adapter.ChatAdapter;
import com.essam.chatapp.chat.model.Message;
import com.essam.chatapp.contacts.utils.ContactsHelper;
import com.essam.chatapp.conversations.model.Chat;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.utils.SharedPrefrence;
import com.essam.chatapp.utils.firebase.FirebaseHelper;
import com.essam.chatapp.photoEditor.PhotoEditorActivity;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.network.SocketListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.WebSocket;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    //views
    private RecyclerView recyclerView;
    private EditText messageEt;
    private LinearLayout filePickerLl, emptyLayout;

    private ChatAdapter adapter;
    private List<Message> listMessages;
    private List<View> onClickViews;

    //vars
    private String inputMessage;
    private String otherName, myName;
    private String chatID;
    private int otherUnseenCount;
    private String myUid, otherUid;
    private List<String> mediaUriList;
    private SharedPrefrence prefrence;
    private Uri picUri;
    private String currentFormatDate;

    //firebase
    private DatabaseReference appChatDb, appUserDb, mChatDb, otherSideUnseenChildDb;
    private ChildEventListener childEventListener;
    private ValueEventListener checkSeenEventListener;

    //web Socket
    private WebSocket webSocket;
    private String messageId;

    private final static String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        appChatDb = FirebaseHelper.getAppChatDbReference();
        appUserDb = FirebaseHelper.getAppUserDbReference();
        myUid = FirebaseHelper.userUid;

        initEventListeners();
        initViews();
        receiveIntents();
    }

    /**
     * there are two scenarios to open this chat activity :
     * 1- from home chats >> means this is not the first time and should expect to receive chat id
     * 2- from contacts activity >> if there is already chat with this user should also receive chat id
     * BUT if this is the first time >> should receive user uid and crete
     * chat when sending the first message
     */
    private void receiveIntents() {
        //
        Intent intent = getIntent();
        if (!intent.hasExtra(Consts.CHAT_ID) || intent.getStringExtra(Consts.CHAT_ID) == null) {
            // no chat id found >> look for user uid
            if (intent.hasExtra(Consts.USER_UID) && intent.getStringExtra(Consts.USER_UID) != null) {
                otherUid = intent.getStringExtra(Consts.USER_UID);
                fetchUserData();
            }
        } else {
            // there is chatId found
            chatID = intent.getStringExtra(Consts.CHAT_ID);
            Log.i(TAG, chatID);
            //get the reference to this chat id chat database >> chat/chatId
            mChatDb = appChatDb.child(chatID);
            // pass this reference to adapter in order to handle and update seen state
            adapter.setChatDp(mChatDb);
            getUserInfo();
            getMessages();
        }
    }

    private void initEventListeners() {
        // this listener is basically listening for a new message added to this conversation
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                   Message message = dataSnapshot.getValue(Message.class);
                   if(message!=null){
                       updateUi(message);
                       getLastUnseenCont();
                       resetMyUnseenCount();
                   }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // changing means messages has been seen so update it in adapter
                for (int i = 0; i < listMessages.size(); i++) {
                    if (listMessages.get(i).getMessageId().equals(s)) {
                        adapter.updateSeen(i);
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        checkSeenEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    otherUnseenCount = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void initViews() {
        recyclerView = findViewById(R.id.msg_rv);
        messageEt = findViewById(R.id.send_msg_et);
        ImageView sendIv = findViewById(R.id.send_iv);
        ImageView attachFileIv = findViewById(R.id.attach_file_iv);
        ImageView openCameraIv = findViewById(R.id.open_camera);
        filePickerLl = findViewById(R.id.ll_file_picker);
        ImageView captureImageIv = findViewById(R.id.open_camera_iv);
        ImageView openGalleryIv = findViewById(R.id.open_gallery_iv);
        emptyLayout = findViewById(R.id.empty_ll);
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
        onClickViews = new ArrayList<>();
        onClickViews.add(sendIv);
        onClickViews.add(attachFileIv);
        onClickViews.add(openCameraIv);
        onClickViews.add(captureImageIv);
        onClickViews.add(openGalleryIv);
        onClickViews.add(messageEt);
        addClickListeners();
    }

    /**
     * setOnclickListeners for all view in one line
     */
    private void addClickListeners() {
        for (View view : onClickViews) view.setOnClickListener(this);
    }

    private void getUserInfo() {
        DatabaseReference mySideDb = appUserDb.child(myUid).child(Consts.CHAT).child(chatID);
        mySideDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    otherName = dataSnapshot.child(Consts.USER_NAME).getValue().toString();
                    setTitle(ContactsHelper.getContactName(ChatActivity.this, otherName));
                    otherUid = dataSnapshot.child(Consts.CREATOR_ID).getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Fetching data of the other user which this conversation is with
     * We can get only public data like name, image , online state and so on..
     */
    private void fetchUserData() {
        emptyLayout.setVisibility(View.VISIBLE);
        //get user name and update actionBar title with this name
        DatabaseReference otherDb = appUserDb.child(otherUid);
        otherDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    otherName = dataSnapshot.child(Consts.NAME).getValue().toString();
                setTitle(ContactsHelper.getContactName(ChatActivity.this, otherName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference myDb = appUserDb.child(myUid);
        myDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    myName = dataSnapshot.child(Consts.NAME).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMessages() {
        if (mChatDb == null) return;

        emptyLayout.setVisibility(View.GONE);
        mChatDb.addChildEventListener(childEventListener);
    }

    private void preSendMessage() {
        //get message that has been input by user
        inputMessage = messageEt.getText().toString().trim();

        if (TextUtils.isEmpty(inputMessage)) {
            Toast.makeText(this, "Please type a message to be sent", Toast.LENGTH_SHORT).show();
        } else {
            checkState();
            messageEt.setText("");
        }

    }

    private void checkState() {

        if (chatID == null) {
            sendFirstMessage();
        } else {
            pushNewMessage();
        }
    }

    private void sendFirstMessage() {

        // this is the first message between these two users
        // create new chat item in both current user and other user
        // update chatID
        chatID = appChatDb.push().getKey();
        mChatDb = appChatDb.child(chatID);
        adapter.setChatDp(mChatDb);
        getMessages();
        pushNewMessage();
        pushNewChat();
    }

    private void pushNewMessage() {
        if (!mediaUriList.isEmpty()) {
         pushMediaMessages();
        } else {
            currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
            messageId = mChatDb.push().getKey();
            DatabaseReference newMessageDb = mChatDb.child(messageId);

            Message message = new Message(messageId,inputMessage,myUid,currentFormatDate,false);
            newMessageDb.setValue(message);
            updateLastMessage();
        }

    }

    private List<String> messageIdList;
    private int mediaUploaded;
    private void pushMediaMessages(){
        mediaUploaded = 0;
        messageIdList = new ArrayList<>();

        for (String mediaUri : mediaUriList) {
            messageId = mChatDb.push().getKey();
            messageIdList.add(messageId);

            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child(Consts.CHAT).child(chatID).child(messageId);
            UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
                            Message message = new Message(messageIdList.get(mediaUploaded),inputMessage,myUid,currentFormatDate,uri.toString(),false);
                            DatabaseReference newMessageDb = mChatDb.child(messageIdList.get(mediaUploaded));
                            newMessageDb.setValue(message);
                            updateLastMessage();
                            inputMessage = "";
                            mediaUploaded ++;
                            if(mediaUriList.size() == mediaUploaded){
                                mediaUriList.clear();
                            }
                        }
                    });
                }
            });
        }
    }

    public void updateUi(Message message) {
        listMessages.add(message);
        adapter.setMessagesData(listMessages);
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private void pushNewChat() {
        currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();

        DatabaseReference mySideDb = FirebaseHelper.getReferenceToThisChatOfCurrentUser(chatID);
        Chat mySideChat = new Chat(chatID,otherName,otherUid,inputMessage,currentFormatDate,0,System.currentTimeMillis());
        mySideDb.setValue(mySideChat);

        DatabaseReference otherSideDb = FirebaseHelper.getReferenceToThisChatOfOtherUser(otherUid,chatID);
        Chat otherSideChat = new Chat(chatID,myName,myUid,inputMessage,currentFormatDate,1,System.currentTimeMillis());
        otherSideDb.setValue(otherSideChat);

        getLastUnseenCont();
    }

    private void updateLastMessage() {
        currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
        if (TextUtils.isEmpty(inputMessage)) {
            if (!mediaUriList.isEmpty()) {
                inputMessage = "Photo";
            }
        }

        DatabaseReference mySideDb = FirebaseHelper.getReferenceToThisChatOfCurrentUser(chatID);
        mySideDb.child(Consts.MESSAGE).setValue(inputMessage);
        mySideDb.child(Consts.CREATED_AT).setValue(currentFormatDate);

        DatabaseReference otherSideDb = FirebaseHelper.getReferenceToThisChatOfOtherUser(otherUid,chatID);
        otherSideDb.child(Consts.MESSAGE).setValue(inputMessage);
        otherSideDb.child(Consts.CREATED_AT).setValue(currentFormatDate);
        otherSideDb.child(Consts.UNSEEN_COUNT).setValue(otherUnseenCount + 1);
    }

    private void resetMyUnseenCount() {
        DatabaseReference mySideDb = FirebaseHelper.getReferenceToThisChatOfCurrentUser(chatID);
        mySideDb.child(Consts.UNSEEN_COUNT).setValue(0);
    }

    private void getLastUnseenCont() {
        otherSideUnseenChildDb = FirebaseHelper.getReferenceToThisChatOfOtherUser(otherUid,chatID).child(Consts.UNSEEN_COUNT);
        otherSideUnseenChildDb.addValueEventListener(checkSeenEventListener);
    }

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
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getOutputMediaFile(1);
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

    private File getOutputMediaFile(int type) {
        String root = Environment.getExternalStorageDirectory().toString();
        File mediaStorageDir = new File(root, Consts.APP_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    Consts.APP_NAME + timeStamp + ".png");

            String imageName = Consts.APP_NAME + timeStamp + ".png";
        } else {
            return null;
        }
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

    private void openPhotoEditor(String imageUri, int requestCode) {
        Intent intent = new Intent(this, PhotoEditorActivity.class);
        intent.putExtra(Consts.EDIT_PHOTO, imageUri);
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, Consts.EDIT_PHOTO_REQUEST);
    }

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
                        checkState();
                    } else {
                        Log.i(TAG, "onActivityResult: no Image was retrived ");
                        mediaUriList.clear();
                        if (data.getIntExtra("requestCode", -1) == Consts.PICK_IMAGES_REQUEST)
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
    protected void onDestroy() {
        super.onDestroy();
        // remove firebase eventListener .. no need for them since activity is shutting down
        if (mChatDb != null)
            mChatDb.removeEventListener(childEventListener);

        if (otherSideUnseenChildDb != null)
            otherSideUnseenChildDb.removeEventListener(checkSeenEventListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
    public void onBackPressed() {
        if (filePickerLl.getVisibility() == View.VISIBLE) {
            animateFilePickerDown();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // make up button behave like back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFilePickerDialog() {
        // TODO: 2/17/2020 make a custom dialog to present filePiker layout with nice animation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View filePicker = inflater.inflate(R.layout.file_picker, null);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        builder.setView(inflater.inflate(R.layout.file_picker, null));

        builder.create();
        builder.show();

        YoYo.with(Techniques.Pulse)
                .duration(200)
                .playOn(filePicker);
    }

    private void instantiateWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.0.103:6001").build();
        SocketListener socketListener = new SocketListener(this);
        webSocket = client.newWebSocket(request, socketListener);

    }

    private void sendUsingWebSocket(Message message) {
        //convert message object into json string
        String jsonString = new Gson().toJson(message);
        webSocket.send(jsonString);
    }

}
