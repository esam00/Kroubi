package com.essam.chatapp.models;

public class Data {
    private Message message;
    private Profile profile;

    public Data(Message message, Profile profile) {
        this.message = message;
        this.profile = profile;
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
