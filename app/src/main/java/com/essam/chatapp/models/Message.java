package com.essam.chatapp.models;

public class Message {
    private String messageId;
    private String message;
    private String creatorId;
    private String createdAt;
    private Long timeStamp;
    private String media;
    private boolean seen;
    private Content content = Content.TEXT;
    private boolean isLoading;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String messageId, String message, String creatorId, String createdAt, Long timeStamp, boolean seen) {
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
        this.isLoading = true;
        this.content = Content.IMAGE;
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

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}
