package com.essam.chatapp.models;

public class ChatT {
    private String chatId;
    private String userUid;
    private String userPhone;
    private String userPhoto;
    private String message;
    private String createdAt;
    private String creatorId;
    private boolean seen;
    private Long timeStamp;
    private int unSeenCount;
    private boolean isTyping;

    public ChatT() {
        // Default constructor required for calls to DataSnapshot.getValue(Chat.class)
    }

    public ChatT(String chatId, String userUid, String userPhone, String userPhoto,
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
