package com.essam.chatapp.ui.home.fragments.chat;

import com.essam.chatapp.models.HomeChat;

public class HomeChatContract {

    interface Presenter{
        void getUserChatList();
    }

    interface View {
        void onNewChatAdded(HomeChat chat);

        void onChatUpdated(HomeChat chat);

        void onCheckExistingChats(boolean hasPreviousChats);

        void onNetworkError();
    }
}
