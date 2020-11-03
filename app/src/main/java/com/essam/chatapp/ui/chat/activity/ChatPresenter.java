package com.essam.chatapp.ui.chat.activity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.firebase.ChatManager;
import com.essam.chatapp.models.Chat;
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
    private ChildEventListener newMessageEventListener;
    private ValueEventListener mIsTypingListener;
    private ValueEventListener mUserProfileEventListener;

    // vars
    private boolean isFirstTime = true;
    private Profile otherProfile; // the profile of the other user
    private String chatID;

    private static final String TAG = ChatPresenter.class.getSimpleName();

    public ChatPresenter(ChatContract.View view) {
        mView = view;
    }

    /**
     * The entry point of chat room is to check if current user has any chat with any other user
     * if current user has any previous chat, we loop through the chat list to check if any of thees
     * chats is with the passed user
     *
     * @param userProfile: profile of other end user
     */
    @Override
    public void checkForPreviousChatWith(final Profile userProfile) {
        this.otherProfile = userProfile;
        mChatManager = new ChatManager(otherProfile);

        mChatManager.checkForPreviousChatWith(new ValueEventListener() {
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

                // we already have the profile info passed through intent to activity,
                // But this listener will update online state and any other changes like profile image
                // will be immediately updated in real time
                 getUserProfileInfo(otherProfile.getId());
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
        if (isFirstTime) {
            chatID = mChatManager.pushNewTopLevelChat(); // app/chat/chatId
            if (chatID != null) {
                mChatManager = new ChatManager(chatID, otherProfile);
                mChatManager.sendFirstMessage(mMediaUriList, inputMessage);

                listenForTyping();
                listenForMessages();
            }
        } else {
            mChatManager.pushNewMessage(mMediaUriList, inputMessage, false);
        }
    }

    @Override
    public void updateComingMessageAsSeen(String messageId) {
        mChatManager.updateComingMessageAsSeen(messageId);
    }

    @Override
    public void toggleIsTypingState(boolean isTyping) {
        if (mChatManager != null) {
            mChatManager.toggleIsTypingState(isTyping);
        }
    }

    @Override
    public void toggleOnlineState(boolean isOnline) {
        mChatManager.toggleOnlineState(isOnline);
    }

    private void checkChatHistoryBetweenTheesTwoUsers(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Chat chat = snapshot.getValue(Chat.class);
            if (chat != null && otherProfile.getId().equals(chat.getUserUid())) {
                // Found previous chat between the two users
                isFirstTime = false;
                chatID = chat.getChatId();
                mChatManager = new ChatManager(chatID, otherProfile);

                listenForTyping();
                listenForMessages();
                break;
            }
        }

        if (isFirstTime)
            mView.onCheckFirstTimeChat(true);
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

    private void listenForMessages() {
        // this listener is basically listening for a new message added to this conversation
        newMessageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        if (isFirstTime) {
                            isFirstTime = false;
                            mView.onCheckFirstTimeChat(false);
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
        mChatManager.listenForMessages(newMessageEventListener);
    }

    private void unSubScribeAllListeners() {
        toggleIsTypingState(false);
        mChatManager.removeLastUnseenCountListener();

        // remove firebase eventListener .. no need for them since activity is shutting down
        if (newMessageEventListener != null)
            mChatManager.reMoveChatListener(newMessageEventListener);

        if (mIsTypingListener != null) {
            mChatManager.removeIsTypingListener(mIsTypingListener);
        }

        if (mUserProfileEventListener != null) {
            mChatManager.removeUserProfileListener(mUserProfileEventListener);
        }
    }

    @Override
    public void detachView() {
        mView = null;
        unSubScribeAllListeners();
    }
}
