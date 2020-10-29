package com.essam.chatapp.firebase;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.ui.chat.activity.ChatCallBacks;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.User;
import com.essam.chatapp.models.Chat;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {
    //firebase
    private DatabaseReference otherSideUnseenChildDb;
    private ChildEventListener newMessageEventListener;
    private ValueEventListener checkSeenEventListener;
    private DatabaseReference mChatDb; //App/chat/chatId/
    private DatabaseReference mySideDb, otherSideDb;

    // vars
    private String messageId;
    private String inputMessage;
    private String chatID;
    private User otherUser;
    private String currentFormatDate;
    private int otherUnseenCount;
    private List<String> messageIdList;
    private int mediaUploaded;
    private boolean isFirstTime = true;

    private static final String TAG = ChatManager.class.getSimpleName();

    //Constructor
    private ChatCallBacks mCallBacks;
    private FirebaseManager mManager;
    public ChatManager(ChatCallBacks callBacks) {
        mCallBacks = callBacks;
        mManager = FirebaseManager.getInstance();
    }

    public void checkForPreviousChatWith(final User otherUser) {
        this.otherUser = otherUser;
        mManager.checkChatHistoryForCurrentUser(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat != null && otherUser.getUid().equals(chat.getUserUid())) {
                            isFirstTime = false;
                            chatID = chat.getChatId();
                            //get the reference to this chat id chat database >> app/chat/chatId
                            mChatDb = mManager.getReferenceToSpecificAppChat(chatID);
                            mySideDb = mManager.getReferenceToSpecificUserChat(chatID);
                            otherSideDb = mManager.getReferenceToSpecificUserChat(otherUser.getUid(), chatID);

                            mCallBacks.onCheckFirstTimeChat(false);

                            getAllMessages();
                        }
                    }
                } else {
                    isFirstTime = true;
                    mCallBacks.onCheckFirstTimeChat(true);
                    Log.i(TAG, "User has no previous chat with this user yet! ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getAllMessages() {
        // this listener is basically listening for a new message added to this conversation
        newMessageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        isFirstTime = false;
                        mCallBacks.onCheckFirstTimeChat(false);
                        mCallBacks.onNewMessageAdded(message);
                        getLastUnseenCont();
                        resetMyUnseenCount();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mCallBacks.onMessageSeen(s);
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
        mChatDb.addChildEventListener(newMessageEventListener);
    }

    public void sendMessage(String inputMessage, List<String>mMediaUriList) {
        this.inputMessage = inputMessage;
        if (isFirstTime) {
            sendFirstMessage(mMediaUriList);
        } else {
            pushNewMessage(mMediaUriList);
        }
    }

    public void updateComingMessageAsSeen(String messageId){
        mChatDb.child(messageId).child(Consts.SEEN).setValue(true);
        otherSideDb.child(Consts.SEEN).setValue(true);
    }

    private void sendFirstMessage(List<String>mMediaUriList) {
        // this is the first message between these two users
        chatID = mManager.pushNewTopLevelChat(); // app/chat/chatId
        if (chatID != null) {
            mChatDb = mManager.getReferenceToSpecificAppChat(chatID);
            mySideDb = mManager.getReferenceToSpecificUserChat(chatID);
            otherSideDb = mManager.getReferenceToSpecificUserChat(otherUser.getUid(), chatID);

            getAllMessages();
            // create new chat item in both current user and other user
            pushNewMessage(mMediaUriList);

            pushNewChat(inputMessage);
        }
    }

    private void pushNewChat(String inputMessage) {
        currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
        Chat mySideChat = new Chat(chatID,
                otherUser.getUid(),
                otherUser.getPhone(),
                otherUser.getImage(),
                inputMessage,
                currentFormatDate, System.currentTimeMillis(),
                0,
                false,
                mManager.getMyUid(),
                false);
        mySideDb.setValue(mySideChat);

        String myPhoto = "";
        Chat otherSideChat = new Chat(chatID, mManager.getMyUid(), mManager.getMyPhone(), myPhoto, inputMessage,
                currentFormatDate, System.currentTimeMillis(), 1,false,
                mManager.getMyUid(),
                false);
        otherSideDb.setValue(otherSideChat);

        getLastUnseenCont();
    }

    private void pushNewMessage(List<String>mediaUriList) {
        if (!mediaUriList.isEmpty()) {
            pushMediaMessages(mediaUriList);
        } else {
            currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
            messageId = mChatDb.push().getKey();
            if(messageId != null) {
                DatabaseReference newMessageDb = mChatDb.child(messageId);

                Message message = new Message(messageId,
                        inputMessage,
                        mManager.getMyUid(),
                        currentFormatDate,
                        System.currentTimeMillis(),
                        false
                );
                newMessageDb.setValue(message);

                if (!isFirstTime) updateLastMessage(inputMessage,mediaUriList);
            }
        }

    }

    private void getLastUnseenCont() {
        checkSeenEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null)
                    otherUnseenCount = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        otherSideUnseenChildDb = mManager.getReferenceToSpecificUserChat(otherUser.getUid(), chatID).child(Consts.UNSEEN_COUNT);
        otherSideUnseenChildDb.addValueEventListener(checkSeenEventListener);
    }

    private void resetMyUnseenCount() {
        mySideDb.child(Consts.UNSEEN_COUNT).setValue(0);
    }

    private void updateLastMessage(String inputMessage, List<String>mediaUriList) {
        currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
        if (TextUtils.isEmpty(inputMessage)) {
            if (!mediaUriList.isEmpty()) {
                inputMessage = "Photo";
            }
        }

        mySideDb.child(Consts.MESSAGE).setValue(inputMessage);
        mySideDb.child(Consts.CREATED_AT).setValue(currentFormatDate);
        mySideDb.child(Consts.TIME_STAMP).setValue(System.currentTimeMillis());
        mySideDb.child(Consts.CREATOR_ID).setValue(mManager.getMyUid());
        mySideDb.child(Consts.SEEN).setValue(false);

        otherSideDb.child(Consts.MESSAGE).setValue(inputMessage);
        otherSideDb.child(Consts.CREATED_AT).setValue(currentFormatDate);
        otherSideDb.child(Consts.TIME_STAMP).setValue(System.currentTimeMillis());
        otherSideDb.child(Consts.UNSEEN_COUNT).setValue(otherUnseenCount + 1);
        otherSideDb.child(Consts.CREATOR_ID).setValue(mManager.getMyUid());

    }

    private void pushMediaMessages(final List<String>mediaUriList) {
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
                            Message message = new Message(messageIdList.get(mediaUploaded), inputMessage, mManager.getMyUid(), currentFormatDate, uri.toString(),System.currentTimeMillis(), false);
                            DatabaseReference newMessageDb = mChatDb.child(messageIdList.get(mediaUploaded));
                            newMessageDb.setValue(message);
                            if (!isFirstTime) updateLastMessage(inputMessage,mediaUriList);
                            inputMessage = "";
                            mediaUploaded++;
                            if (mediaUriList.size() == mediaUploaded) {
                                mediaUriList.clear();
                            }
                        }
                    });
                }
            });
        }
    }

    public void unSubScribeAllListeners(){
        // remove firebase eventListener .. no need for them since activity is shutting down
        if (mChatDb != null)
            mChatDb.removeEventListener(newMessageEventListener);

        if (otherSideUnseenChildDb != null)
            otherSideUnseenChildDb.removeEventListener(checkSeenEventListener);
    }
}
