package com.essam.chatapp.models;

public class HomeChat {
    private String chatId;
    private Message lastMessage;
    private Profile userProfile;
    private int unSeenCount;
    private boolean isTyping;
    private boolean otherTyping;

    public HomeChat() {
        // Default constructor required for calls to DataSnapshot.getValue(HomeChat.class)
    }

    public HomeChat(String chatId, Message lastMessage, Profile userProfile, int unSeenCount, boolean isTyping, boolean otherTyping) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.userProfile = userProfile;
        this.unSeenCount = unSeenCount;
        this.isTyping = isTyping;
        this.otherTyping = otherTyping;
    }

    public HomeChat(String chatId, Profile userProfile) {
        this.chatId = chatId;
        this.userProfile = userProfile;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Profile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Profile userProfile) {
        this.userProfile = userProfile;
    }

    public int getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(int unSeenCount) {
        this.unSeenCount = unSeenCount;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        this.isTyping = typing;
    }

    public boolean isOtherTyping() {
        return otherTyping;
    }

    public void setOtherTyping(boolean otherTyping) {
        this.otherTyping = otherTyping;
    }
}
