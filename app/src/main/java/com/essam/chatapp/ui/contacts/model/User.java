package com.essam.chatapp.ui.contacts.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String uid;
    private String name;
    private String phone;
    private String status;
    private String image = "";
    private String state = "online";

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name, String phone, String status, String image, String state) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.image = image;
        this.state = state;
    }

    public User(String uid, String name, String phone, String image, String state) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.state = state;
    }

    public User(String uid, String name, String phone) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected User(Parcel in) {
        uid = in.readString();
        name = in.readString();
        phone = in.readString();
        status = in.readString();
        image = in.readString();
        state = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(status);
        parcel.writeString(image);
        parcel.writeString(state);
    }
}
