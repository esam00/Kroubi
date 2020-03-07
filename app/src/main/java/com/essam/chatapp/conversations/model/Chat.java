package com.essam.chatapp.conversations.model;

public class Chat {
    private String chatId;
    private String senderName;
    private String lastMessage;
    private String sentAt;
    private int unSeenCount;


    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public int getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(int unSeenCount) {
        this.unSeenCount = unSeenCount;
    }
}
