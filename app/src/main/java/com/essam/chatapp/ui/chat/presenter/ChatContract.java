package com.essam.chatapp.ui.chat.presenter;

import android.net.Uri;

import com.essam.chatapp.models.Message;
import com.essam.chatapp.models.Profile;

import java.util.List;

public class ChatContract {

    public interface Presenter {
        void initChatRoom(Profile myProfile, Profile otherProfile);

        void getChatHistory();

        void getUserProfileInfo(String userId);

        void sendTextMessage(String inputMessage);

        void sendMediaMessages(String inputMessage, List<Uri> mMediaUriList);

        void toggleIsTypingState(boolean isTyping);

        void toggleOnlineState(boolean isOnline);

        void updateComingMessageAsSeen(String messageId);

        void detachView();
    }

    public interface View{
        void onCheckFirstTimeChat(boolean isFirstTime);

        void onNewMessageAdded(Message message);

        void onMessageUpdated(Message message);

        void onToggleIsTyping(boolean isTyping);

        void onUpdateProfileInfo(Profile profile);
    }
}
