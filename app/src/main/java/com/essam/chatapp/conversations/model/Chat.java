package com.essam.chatapp.conversations.model;

public class Chat {
    private String chatId;
    private String userUid;
    private String userPhone;
    private String userPhoto;
    private String message;
    private String createdAt;
    private Long timeStamp;
    private int unSeenCount;
    private boolean isTyping;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Chat.class)
    }

    public Chat(String chatId, String userUid, String userPhone, String userPhoto,
                String message, String createdAt, long timeStamp, int unSeenCount, boolean isTyping) {
        this.chatId = chatId;
        this.userUid = userUid;
        this.userPhone = userPhone;
        this.userPhoto = userPhoto;
        this.message = message;
        this.createdAt = createdAt;
        this.timeStamp = timeStamp;
        this.unSeenCount = unSeenCount;
        this.isTyping = isTyping;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
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

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }
}
