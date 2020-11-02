package com.essam.chatapp.firebase;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.essam.chatapp.models.Chat;
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

/** ChatManager is a simple class that created with every [one to one] chat room
 * It holds A three basic database references [ChatDb, MySideDb, OtherSideDb]
 * So this class is responsible for :
 * 1- Creating the chat room at top level app/chat/chatId that holds and listens for new messages
 * 2- Creating the chat snippet in both current user and other user chat node to update last message that displayed in home
 * 3- ChatManager is considered as a bridge between chatPresenter and FirebaseManager class
 * */
public class ChatManager {
    private Profile otherProfile; // the profile of the other user
    private String chatID;
    private DatabaseReference mChatDb; //App/chat/chatId/
    private DatabaseReference mySideDb, otherSideDb; // reference to this chat in both current user and other user

    // vars
    private String messageId;
    private String inputMessage;
    private String currentFormatDate;
    private int otherUnseenCount;
    private List<String> messageIdList;
    private int mediaUploaded;
    private FirebaseManager mManager;

    //Constructor
    // Created when user first enter chat room
    public ChatManager(Profile otherProfile) {
        this.otherProfile = otherProfile;
        mManager = FirebaseManager.getInstance();
    }

    public ChatManager(String chatID, Profile otherProfile){
        this.chatID = chatID;
        this.otherProfile = otherProfile;
        this.mManager = FirebaseManager.getInstance();
        InitChatRoomInfo();
    }

    /* -------------------------------Initialization[private helpers]----------------------------*/

    private void InitChatRoomInfo(){
        //initialize reference to top level chat reference in database >> app/chat/chatID
        mChatDb = mManager.getReferenceToSpecificAppChat(chatID);

        //initialize reference to chat node at my side in database >> app/user/myUid/chat/chatID
        mySideDb = mManager.getReferenceToSpecificUserChat(chatID);

        //initialize reference to chat node at other user in database >> app/user/otherUid/chat/chatID
        otherSideDb = mManager.getReferenceToSpecificUserChat(otherProfile.getId(), chatID);

        //Set User online state to true
        mManager.toggleOnlineState(true);
    }

    /* ----------------------------------------Public -------------------------------------------*/

    public void checkForPreviousChatWith(ValueEventListener listener){
        mManager.checkChatHistoryForCurrentUser(listener);
    }

    public String pushNewTopLevelChat(){
        return mManager.pushNewTopLevelChat();
    }

    public void listenForUserProfileChanges(String userId, ValueEventListener listener){
        mManager.getUserProfileInfo(userId, listener);
    }

    public void listenForTyping(ValueEventListener eventListener){
        otherSideDb.child(Consts.IS_TYPING).addValueEventListener(eventListener);
    }

    public void toggleIsTypingState(boolean isTyping){
        if (chatID != null){
            mySideDb.child(Consts.IS_TYPING).setValue(isTyping);
        }
    }

    public void listenForMessages(ChildEventListener childEventListener) {
        mChatDb.addChildEventListener(childEventListener);
    }

    public void getLastUnseenCont() {
        otherSideDb.child(Consts.UNSEEN_COUNT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null)
                    otherUnseenCount = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void resetMyUnseenCount() {
        mySideDb.child(Consts.UNSEEN_COUNT).setValue(0);
    }

    public void updateComingMessageAsSeen(String messageId){
        mChatDb.child(messageId).child(Consts.SEEN).setValue(true);
        otherSideDb.child(Consts.SEEN).setValue(true);
    }

    public void removeIsTypingListener(ValueEventListener valueEventListener){
        otherSideDb.child(Consts.IS_TYPING).removeEventListener(valueEventListener);
    }

    public void reMoveChatListener(ChildEventListener childEventListener){
        if (mChatDb != null)
            mChatDb.removeEventListener(childEventListener);
    }

    public void removeUserProfileListener(ValueEventListener listener){
        mManager.removeUserProfileListener(otherProfile.getId(), listener);
    }

    /* -----------------------------------Send Message logic[Private]---------------------------------*/

    public void sendFirstMessage(List<String>mMediaUriList, String inputMessage) {
        // this is the first message between these two users
            pushNewMessage(mMediaUriList, inputMessage, true);
            // create new chat item in both current user and other user
            pushNewChat(inputMessage);
    }

    public void pushNewMessage(List<String>mediaUriList, String inputMessage, boolean isFirstTime) {
        if (!mediaUriList.isEmpty()) {
            pushMediaMessages(mediaUriList, isFirstTime);
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

    private void pushNewChat(String inputMessage) {
        currentFormatDate = ProjectUtils.getDisplayableCurrentDateTime();
        Chat mySideChat = new Chat(chatID,
                otherProfile.getId(),
                otherProfile.getPhone(),
                otherProfile.getAvatar(),
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

    private void pushMediaMessages(final List<String>mediaUriList,final boolean isFirstTime) {
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
}
