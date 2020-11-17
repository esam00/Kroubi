package com.essam.chatapp.ui.home.fragments.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.models.Chat;
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
        mFirebaseManager.checkChatHistoryForCurrentUser(new ValueEventListener() {
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
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, "new chat added");
                    // TODO: 11/12/2020 Chat model should only holds a reference to last message id
                    // and user id >> this way we could listen constantly for profile changes like profile image
                    mView.onNewChatAdded(chat);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
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
        mFirebaseManager.getUserChatList(mHomeChatsEventListener);
    }

    public void detachView(){
        mView = null;
        removeHomeChatListeners();
    }

    private void removeHomeChatListeners() {
        mFirebaseManager.removeHomeChatListeners(mHomeChatsEventListener);
    }

}
