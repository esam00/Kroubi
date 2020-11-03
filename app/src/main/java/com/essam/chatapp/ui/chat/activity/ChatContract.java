package com.essam.chatapp.ui.chat.activity;

import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;

import java.util.List;

public class ChatContract {

    interface Presenter {
        void checkForPreviousChatWith(Profile userProfile);

        void getUserProfileInfo(String userId);

        void sendMessage(String inputMessage, List<String> mMediaUriList);

        void toggleIsTypingState(boolean isTyping);

        void toggleOnlineState(boolean isOnline);

        void updateComingMessageAsSeen(String messageId);

        void detachView();
    }

    interface View{
        void onCheckFirstTimeChat(boolean isFirstTime);

        void onNewMessageAdded(Message message);

        void onMessageSeen(String messageId);

        void onToggleIsTyping(boolean isTyping);

        void onUpdateProfileInfo(Profile profile);
    }
}
