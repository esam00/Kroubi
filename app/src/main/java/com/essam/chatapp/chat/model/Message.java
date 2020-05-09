package com.essam.chatapp.chat.model;

import java.util.List;

public class Message {
    private String messageId;
    private String message;
    private String creatorId;
    private String createdAt;
    private long timeStamp;
    private String media;
    private boolean seen;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Message(String messageId, String message, String creatorId, String createdAt, long timeStamp, boolean seen) {
        this.messageId = messageId;
        this.message = message;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.timeStamp = timeStamp;
        this.seen = seen;
    }

    public Message(String messageId, String message, String creatorId, String createdAt, String media,long timeStamp, boolean seen) {
        this.messageId = messageId;
        this.message = message;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.media = media;
        this.timeStamp = timeStamp;
        this.seen = seen;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
