package com.essam.chatapp.firebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public interface ChatRepository {
     void setChatID(String chatID);

    /* ---------------------------------------- [Read] ------------------------------------------*/

    void getAllChatHistory(ValueEventListener listener);

     void getAllMessages(ChildEventListener childEventListener);

     void removeChatMessagesListener(ChildEventListener childEventListener);

     void listenForTyping(ValueEventListener eventListener);

     void removeTypingListener(ValueEventListener valueEventListener);

     void listenForUserProfileChanges(String userId, ValueEventListener listener);

     void removeUserProfileListener(ValueEventListener listener);

     void getLastUnseenCont();

     void removeLastUnseenCountListener();

    /*--------------------------------------- [Write] ---------------------------------------------*/

     void toggleIsTypingState(boolean isTyping);

     void toggleOnlineState(boolean isOnline);

     void resetMyUnseenCount();

     void updateComingMessageAsSeen(String messageId);

     void pushNewMessage(List<String> mediaUriList, String inputMessage);
}
