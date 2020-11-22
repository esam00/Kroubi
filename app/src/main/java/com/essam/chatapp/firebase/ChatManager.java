package com.essam.chatapp.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.essam.chatapp.models.HomeChat;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;
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

/**
 * ChatManager is a simple class created with every [one to one] chat room
 * It holds A three basic database references [ChatDb, MySideDb, OtherSideDb]
 * So this class is responsible for :
 * 1- Creating the chat room reference at top level app/chat/chatId that holds and listens for new messages
 * 2- Creating the chat snippet reference in both current user and other user chat node to update last message that displayed in home
 * 3- ChatManager is considered as a bridge between chatPresenter and FirebaseManager class
 */
public class ChatManager {
    private String chatID;
    private Profile otherProfile, myProfile;
    private DatabaseReference mChatDb; //App/chat/chatId/
    private DatabaseReference mySideDb, otherSideDb; // reference to this chat in both current user and other user

    // vars
    private String messageId;
    private String currentFormatDate;
    private int otherUnseenCount;
    private List<String> messageIdList;
    private int mediaUploaded;
    private FirebaseManager mManager;
    private ValueEventListener mLastUnseenCountListener;
    private HomeChat mySideChat, otherSideChat;

    //Constructor
    public ChatManager(Profile myProfile, Profile otherProfile) {
        this.otherProfile = otherProfile;
        this.myProfile = myProfile;
        mManager = FirebaseManager.getInstance();
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
        InitChatRoomInfo(chatID);
    }

    /* -------------------------------Initialization[private helpers]----------------------------*/

    private void InitChatRoomInfo(String chatID) {
        //initialize reference to top level chat reference in database >> app/chat/chatID
        mChatDb = mManager.getReferenceToSpecificAppChat(chatID);

        //initialize reference to chat node at my side in database >> app/user/myUid/chat/chatID
        mySideDb = mManager.getReferenceToSpecificUserChat(chatID);
        mySideChat = new HomeChat(chatID, otherProfile);

        //initialize reference to chat node at other user in database >> app/user/otherUid/chat/chatID
        otherSideDb = mManager.getReferenceToSpecificUserChat(otherProfile.getId(), chatID);
        otherSideChat = new HomeChat(chatID, myProfile);
    }

    /* ---------------------------------------- [Read] ------------------------------------------*/

    public void getAllChatHistory(ValueEventListener listener) {
        mManager.getAllChatHistory(listener);
    }

    public void getAllMessages(ChildEventListener childEventListener) {
        mChatDb.addChildEventListener(childEventListener);
    }

    public void removeChatMessagesListener(ChildEventListener childEventListener) {
        if (mChatDb != null)
            mChatDb.removeEventListener(childEventListener);
    }

    public void listenForTyping(ValueEventListener eventListener) {
        otherSideDb.child(Consts.IS_TYPING).addValueEventListener(eventListener);
    }

    public void removeTypingListener(ValueEventListener valueEventListener) {
        otherSideDb.child(Consts.IS_TYPING).removeEventListener(valueEventListener);
    }

    public void listenForUserProfileChanges(String userId, ValueEventListener listener) {
        mManager.getUserProfileInfo(userId, listener);
    }

    public void removeUserProfileListener(ValueEventListener listener) {
        mManager.removeUserProfileListener(otherProfile.getId(), listener);
    }

    /**
     * Keep track of other unseen count to be updated every time i send a new message
     */
    public void getLastUnseenCont() {
        mLastUnseenCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null)
                    otherUnseenCount = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        otherSideDb.child(Consts.UNSEEN_COUNT).addValueEventListener(mLastUnseenCountListener);
    }

    public void removeLastUnseenCountListener() {
        if (mLastUnseenCountListener != null) {
            otherSideDb.child(Consts.UNSEEN_COUNT).removeEventListener(mLastUnseenCountListener);
        }
    }

    /*--------------------------------------- [Write] ---------------------------------------------*/

    public void toggleIsTypingState(boolean isTyping) {
        if (chatID != null) {
            // if other user is currently inside chat room [ChatActivity] will be notified
            mySideDb.child(Consts.IS_TYPING).setValue(isTyping);

            // In case other user is outside in home then will be listening for otherTyping
            otherSideDb.child(Consts.OTHER_TYPING).setValue(isTyping);
        }
    }

    public void toggleOnlineState(boolean isOnline) {
        mManager.toggleOnlineState(isOnline);
    }

    public void resetMyUnseenCount() {
        mySideDb.child(Consts.UNSEEN_COUNT).setValue(0);
    }

    public void updateComingMessageAsSeen(String messageId) {
        mChatDb.child(messageId).child(Consts.SEEN).setValue(true);
        otherSideDb.child(Consts.LAST_MESSAGE).child(Consts.SEEN).setValue(true);
    }

    /* -----------------------------------Send Message logic[Private]---------------------------------*/

    public void pushNewMessage(List<String> mediaUriList, String inputMessage) {
        if (!mediaUriList.isEmpty()) {
            pushMediaMessages(mediaUriList, inputMessage);
        } else {
            sendMessage(inputMessage);
        }

    }

    private void pushMediaMessages(final List<String> mediaUriList, final String inputMessage) {
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
                            Message message = new Message(messageIdList.get(mediaUploaded), inputMessage, myProfile.getId(), currentFormatDate, uri.toString(), System.currentTimeMillis(), false);
                            DatabaseReference newMessageDb = mChatDb.child(messageIdList.get(mediaUploaded));
                            newMessageDb.setValue(message);
//                            if (!isFirstTime) updateLastMessage(inputMessage,mediaUriList);
//                            inputMessage = "";
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

    private void sendMessage(String inputMessage) {
        // Top level chat creation
        if (chatID == null) {
            chatID = mManager.pushNewTopLevelChat();
            InitChatRoomInfo(chatID);
        }

        // push new child at the top level app/chat and get the message id
        messageId = mChatDb.push().getKey();
        if (messageId != null) {
            DatabaseReference newMessageDb = mChatDb.child(messageId);

            Message message = new Message(messageId,
                    inputMessage,
                    myProfile.getId(), // creatorId
                    ProjectUtils.getDisplayableCurrentDateTime(), // createdAt
                    System.currentTimeMillis(), // time stamp
                    false
            );

            newMessageDb.setValue(message);

            // update home chat in mySide
            mySideChat.setLastMessage(message);
            mySideDb.setValue(mySideChat);

            // update home chat in other side
            otherSideChat.setLastMessage(message);
            otherSideChat.setUnSeenCount(otherUnseenCount + 1);
            otherSideDb.setValue(otherSideChat);
        }
    }
}
