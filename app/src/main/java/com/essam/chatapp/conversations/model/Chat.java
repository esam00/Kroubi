package com.essam.chatapp.conversations.model;

public class Chat {
    private String chatId;
    private String creatorId;
    private String message;
    private String createdAt;
    private int unSeenCount;
    private long timeStamp;
    private String userName;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Chat(String chatId, String userName, String creatorId, String message, String createdAt, int unSeenCount, long timeStamp) {
        this.chatId = chatId;
        this.userName = userName;
        this.creatorId = creatorId;
        this.message = message;
        this.createdAt = createdAt;
        this.unSeenCount = unSeenCount;
        this.timeStamp = timeStamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(int unSeenCount) {
        this.unSeenCount = unSeenCount;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
