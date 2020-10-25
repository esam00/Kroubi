package com.essam.chatapp.ui.conversations;

import com.essam.chatapp.models.Chat;

public interface HomeChatCallBacks {

    void onNewChatAdded(Chat chat);

    void onChatUpdated(Chat chat);

    void onCheckExistingChats(boolean hasPreviousChats);
}
