package com.essam.chatapp.ui.home.fragments.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.firebase.data.FirebaseManager;
import com.essam.chatapp.models.HomeChat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class HomeChatPresenter implements HomeChatContract.Presenter{
    private HomeChatContract.View mView;
    private FirebaseManager mFirebaseManager;
    private ChildEventListener mHomeChatsEventListener;
    private static final String TAG = HomeChatPresenter.class.getSimpleName();

    public HomeChatPresenter(HomeChatContract.View view) {
        mView = view;
        mFirebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public void getUserChatList() {
        // single event listener to check first if there is any chat history
        checkChatHistoryForCurrentUser();

        // Subscribing to user/chat to get notified whenever a chat added, changed or deleted
        startFetchingUserChatList();
    }

    private void checkChatHistoryForCurrentUser(){
        //single value event listener to check if user has any previous chats
        mFirebaseManager.getAllChatHistory(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mView.onCheckExistingChats(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mView.onNetworkError();
            }
        });
    }

    private void startFetchingUserChatList() {
        // this value event listener is triggered once a new chat added Or existing chat updated
        mHomeChatsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HomeChat chat = dataSnapshot.getValue(HomeChat.class);
                if (chat != null) {
                    Log.i(TAG, "new chat added");
                    mView.onNewChatAdded(chat);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HomeChat chat = dataSnapshot.getValue(HomeChat.class);
                if (chat != null) {
                    Log.i(TAG, " new message Arrived to an existing chat");
                    mView.onChatUpdated(chat);
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
                Log.e(TAG, "onCancelled: " + databaseError);
            }
        };
        mFirebaseManager.getAllChatHistory(mHomeChatsEventListener);
    }

    public void detachView(){
        mView = null;
        removeHomeChatListeners();
    }

    private void removeHomeChatListeners() {
        if (mHomeChatsEventListener != null) {
            mFirebaseManager.removeHomeChatListener(mHomeChatsEventListener);
        }
    }
}
