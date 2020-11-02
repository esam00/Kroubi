package com.essam.chatapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {
    private String id;
    private String userName;
    private String phone;
    private String avatar;
    private String status;
    private boolean isOnline;

    public Profile() {
    }

    public Profile(String id, String userName, String phone, String avatar, String status, boolean isOnline) {
        this.id = id;
        this.userName = userName;
        this.phone = phone;
        this.avatar = avatar;
        this.status = status;
        this.isOnline = isOnline;
    }

    public Profile(String userName) {
        this.userName = userName;
    }

    protected Profile(Parcel in) {
        id = in.readString();
        userName = in.readString();
        phone = in.readString();
        avatar = in.readString();
        status = in.readString();
        isOnline = in.readByte() != 0;
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userName);
        dest.writeString(phone);
        dest.writeString(avatar);
        dest.writeString(status);
        dest.writeByte((byte) (isOnline ? 1 : 0));
    }
}
