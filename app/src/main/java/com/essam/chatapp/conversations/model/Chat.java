package com.essam.chatapp.conversations.model;

public class Chat {
    private String chatId;
    private String userName;
    private String userUid;
    private String lastMessage;
    private String sentAt;
    private int unSeenCount;
    private long timeStamp;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Chat(String chatId, String userName, String userUid, String lastMessage, String sentAt, int unSeenCount, long timeStamp) {
        this.chatId = chatId;
        this.userName = userName;
        this.userUid = userUid;
        this.lastMessage = lastMessage;
        this.sentAt = sentAt;
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
