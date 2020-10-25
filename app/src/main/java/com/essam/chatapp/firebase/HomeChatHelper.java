package com.essam.chatapp.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.essam.chatapp.ui.conversations.HomeChatCallBacks;
import com.essam.chatapp.models.Chat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class HomeChatHelper {
    private HomeChatCallBacks mCallBacks;
    private FirebaseManager mManager;

    private ChildEventListener onChatAddedEventListener;
    private ValueEventListener checkExistValueEventListener;

    private static final String TAG = "HomeChatHelper";

    public HomeChatHelper(HomeChatCallBacks callBacks) {
        mCallBacks = callBacks;
        mManager = FirebaseManager.getInstance();
    }

    /**
     * This is the main method that is responsible for fetching previous chats AND listen for new messages
     */
    public void getUserChatList() {
        addHomeChatEventListeners();
        mManager.getUserChatDb().addChildEventListener(onChatAddedEventListener);
        mManager.getUserChatDb().addListenerForSingleValueEvent(checkExistValueEventListener);
    }

    private void addHomeChatEventListeners() {
        // this value event listener is triggered once a new chat added Or existing chat updated
        onChatAddedEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, "new chat added");
                    mCallBacks.onNewChatAdded(chat);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, " new message Arrived to an existing chat");
                    mCallBacks.onChatUpdated(chat);
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

        //single value event listener to check if user has any previous chats
        checkExistValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCallBacks.onCheckExistingChats(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }

    public void removeHomeChatListeners() {
        mManager.userChatDb.removeEventListener(onChatAddedEventListener);
        mManager.userChatDb.removeEventListener(checkExistValueEventListener);
    }
}
