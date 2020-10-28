package com.essam.chatapp.ui.home.fragments.chat;

import com.essam.chatapp.models.Chat;

public class HomeChatContract {

    interface Presenter{
        void getUserChatList();
    }

    interface View {
        void onNewChatAdded(Chat chat);

        void onChatUpdated(Chat chat);

        void onCheckExistingChats(boolean hasPreviousChats);

        void onNetworkError();
    }
}
