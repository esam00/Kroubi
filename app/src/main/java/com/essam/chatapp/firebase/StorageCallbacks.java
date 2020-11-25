package com.essam.chatapp.firebase;

public class StorageCallbacks {

    public interface ProfileCallBack{
        void onUploadProfileSuccess(String imageUrl);

        void onUploadProfileFailed();
    }

    public interface ChatCallBacks{
        void onUploadImageMessageSuccess(String imageUrl, String messageId);

        void onUploadImageMessageFailed();
    }
}
