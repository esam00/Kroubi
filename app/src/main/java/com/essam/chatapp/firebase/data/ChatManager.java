package com.essam.chatapp.firebase.data;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.essam.chatapp.firebase.fcm.FcmUtils;
import com.essam.chatapp.firebase.fcm.FirebaseCloudMessage;
import com.essam.chatapp.models.Data;
import com.essam.chatapp.models.HomeChat;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.ProjectUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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
    // reference to this chat in both current user and other user
    private DatabaseReference mySideDb, otherSideDb;
    private int otherUnseenCount;
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

    private void updateLastMessage(Message message) {
        // update home chat in mySide
        mySideChat.setLastMessage(message);
        mySideDb.setValue(mySideChat);

        // update home chat in other side
        otherSideChat.setLastMessage(message);
        otherSideChat.setUnSeenCount(otherUnseenCount + 1);
        otherSideDb.setValue(otherSideChat);
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

    /*--------------------------------------- [Write] -------------------------------------------*/

    public void sendTextMessage(String inputMessage) {
        // Top level chat creation if not created yet
        if (chatID == null) {
            chatID = mManager.pushNewTopLevelChat();
            InitChatRoomInfo(chatID);
        }

        // push new child at the top level app/chat and get the message id
        String messageId = mChatDb.push().getKey();
        if (messageId != null) {
            DatabaseReference newMessageDb = mChatDb.child(messageId);

            Message message = new Message(messageId,
                    inputMessage,
                    myProfile.getId(), // creatorId
                    ProjectUtils.getDisplayableCurrentDateTime(), // createdAt
                    System.currentTimeMillis(), // timeٍٍٍٍٍٍٍStamp
                    false
            );

            newMessageDb.setValue(message);
            updateLastMessage(message);

            // finally push notification
            pushMessageNotification(message);

        }
    }

    private void pushMessageNotification(Message message) {
        Data data = new Data(message, myProfile);
        FirebaseCloudMessage firebaseCloudMessage = new FirebaseCloudMessage(otherProfile.getToken(), data);
        FcmUtils.pushNewMessageNotification(firebaseCloudMessage);
    }

    public void sendMediaMessage(Message message, Uri uri,
                                 StorageCallbacks.ChatCallBacks chatCallBacks) {

        mChatDb.child(message.getMessageId()).setValue(message);
        updateLastMessage(message);

        FirebaseStorageManager.getInstance().uploadMessageImage(uri, chatID, message.getMessageId(), chatCallBacks);
    }

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

    public void updateImageMessage(String url, String messageId) {
        mChatDb.child(messageId).child(Consts.MEDIA).setValue(url);
        mChatDb.child(messageId).child(Consts.LOADING).setValue(false);
    }


    public Message getImageMessagePlaceholder(String inputMessage, String imageUrl) {
        // Top level chat creation
        if (chatID == null) {
            chatID = mManager.pushNewTopLevelChat();
            InitChatRoomInfo(chatID);
        }

        // push new child at the top level app/chat and get the message id
        String messageId = mChatDb.push().getKey();
        if (messageId != null) {
            return new Message(
                    messageId,
                    inputMessage,
                    myProfile.getId(), // creatorId
                    ProjectUtils.getDisplayableCurrentDateTime(), // createdAt
                    imageUrl,
                    System.currentTimeMillis(), // timeٍٍٍٍٍٍٍStamp
                    false
            );
        }
        return null;
    }
}
