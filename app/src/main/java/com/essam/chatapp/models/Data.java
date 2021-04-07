package com.essam.chatapp.models;

public class Data {
    private Message message;
    private Profile profile;
    private String chatID;

    public Data(Message message, Profile profile, String chatID) {
        this.message = message;
        this.profile = profile;
        this.chatID = chatID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
