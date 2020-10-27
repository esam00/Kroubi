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
    private FirebaseManager mManager;
    private ChildEventListener mHomeChatsEventListener;

    private static final String TAG = HomeChatPresenter.class.getSimpleName();

    public HomeChatPresenter(HomeChatContract.View view) {
        mView = view;
        mManager = FirebaseManager.getInstance();
    }

    public void detachView(){
        mView = null;
        removeHomeChatListeners();
    }

    @Override
    public void getUserChatList() {
        checkChatHistoryForCurrentUser();
    }

    /**
     * Check if current user has any chat history
     */
    private void checkChatHistoryForCurrentUser(){
        //single value event listener to check if user has any previous chats
        mManager.getUserChatDb().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasPreviousChats = dataSnapshot.exists();
                mView.onCheckExistingChats(hasPreviousChats);

                // if current user has chat history start fetching chat list
                if (hasPreviousChats) startFetchingUserChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * This method is responsible for fetching previous chats AND listen for new messages
     */
    private void startFetchingUserChatList() {
        // this value event listener is triggered once a new chat added Or existing chat updated
        mHomeChatsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, "new chat added");
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
        mManager.getUserChatDb().addChildEventListener(mHomeChatsEventListener);
    }

    private void removeHomeChatListeners() {
        if (mHomeChatsEventListener != null){
            mManager.getUserChatDb().removeEventListener(mHomeChatsEventListener);
        }
    }
}
