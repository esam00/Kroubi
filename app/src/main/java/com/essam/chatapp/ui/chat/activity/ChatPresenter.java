package com.essam.chatapp.ui.chat.activity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.firebase.ChatManager;
import com.essam.chatapp.models.HomeChat;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatPresenter implements ChatContract.Presenter {
    private ChatContract.View mView;
    private ChatManager mChatManager;

    //firebase
    private ChildEventListener mNewMessageEventListener;
    private ValueEventListener mIsTypingListener;
    private ValueEventListener mUserProfileEventListener;

    // vars
    private boolean isFirstTime = true;
    private Profile otherProfile; // the profile of the other user

    private static final String TAG = ChatPresenter.class.getSimpleName();

    public ChatPresenter(ChatContract.View view) {
        mView = view;
    }

    @Override
    public void initChatRoom(Profile myProfile, Profile otherProfile) {
        this.otherProfile = otherProfile;
        mChatManager = new ChatManager(myProfile, otherProfile);
    }

    /**
     * The entry point of chat room is to check if current user has any chat with any other user
     * if dataSnapshot.exists() == false >> means No chat found at all
     */
    @Override
    public void getChatHistory() {
        mChatManager.getAllChatHistory(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    checkChatHistoryBetweenTheesTwoUsers(dataSnapshot);
                } else {
                    // Current user has no chat with any other user yet
                    isFirstTime = true;
                    mView.onCheckFirstTimeChat(true);
                    Log.i(TAG, "User has no previous chat with any other user yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void getUserProfileInfo(String userId) {
        mUserProfileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile != null)
                    mView.onUpdateProfileInfo(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mChatManager.listenForUserProfileChanges(userId, mUserProfileEventListener);
    }

    @Override
    public void sendMessage(String inputMessage, List<String> mMediaUriList) {
        mChatManager.pushNewMessage(mMediaUriList, inputMessage);

        if (isFirstTime) {
            listenForTyping();
            getAllMessages();
        }
    }

    @Override
    public void updateComingMessageAsSeen(String messageId) {
        mChatManager.updateComingMessageAsSeen(messageId);
    }

    @Override
    public void toggleIsTypingState(boolean isTyping) {
        mChatManager.toggleIsTypingState(isTyping);
    }

    @Override
    public void toggleOnlineState(boolean isOnline) {
        mChatManager.toggleOnlineState(isOnline);
    }

    @Override
    public void detachView() {
        mView = null;
        unSubScribeAllListeners();
    }

    private void checkChatHistoryBetweenTheesTwoUsers(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            HomeChat chat = snapshot.getValue(HomeChat.class);
            if (chat != null && otherProfile.getId().equals(chat.getUserProfile().getId())) {
                // Found previous chat between the two users
                isFirstTime = false;
                mChatManager.setChatID(chat.getChatId());

                listenForTyping();
                getAllMessages();
                break;
            }
        }

        mView.onCheckFirstTimeChat(isFirstTime);
    }

    private void listenForTyping() {
        mIsTypingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null)
                    mView.onToggleIsTyping((boolean) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mChatManager.listenForTyping(mIsTypingListener);
    }

    private void getAllMessages() {
        // this listener is basically listening for a new message added to this conversation
        mNewMessageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        if (isFirstTime) {
                            isFirstTime = false;
                        }

                        mView.onNewMessageAdded(message);

                        mChatManager.getLastUnseenCont();
                        mChatManager.resetMyUnseenCount();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mView.onMessageSeen(s);
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
        mChatManager.getAllMessages(mNewMessageEventListener);
    }

    private void unSubScribeAllListeners() {
        toggleIsTypingState(false);
        mChatManager.removeLastUnseenCountListener();

        // remove firebase eventListener .. no need for them since activity is shutting down
        if (mNewMessageEventListener != null)
            mChatManager.removeChatMessagesListener(mNewMessageEventListener);

        if (mIsTypingListener != null) {
            mChatManager.removeTypingListener(mIsTypingListener);
        }

        if (mUserProfileEventListener != null) {
            mChatManager.removeUserProfileListener(mUserProfileEventListener);
        }
    }
}
