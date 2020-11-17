package com.essam.chatapp.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.essam.chatapp.utils.Consts;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class FirebaseStorageManager {
    private static FirebaseStorageManager instance;
    private StorageReference mProfileImageReference;
    private StorageReference mChatImagesReference;

    public FirebaseStorageManager() {
        initFireBaseStorageReferences();
    }

    public static FirebaseStorageManager getInstance() {
        if (instance == null){
            instance = new FirebaseStorageManager();
        }
        return instance;
    }

    private void initFireBaseStorageReferences(){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mProfileImageReference = firebaseStorage.getReference().child(Consts.PROFILE_IMAGES);
        mChatImagesReference = firebaseStorage.getReference().child(Consts.CHAT_IMAGES);
    }

    public void uploadProfileImage(Uri imageUri, String userUid, final StorageCallbacks.ProfileCallBack callBack){
        final StorageReference imageRef = mProfileImageReference.child(userUid).child(Objects.requireNonNull(imageUri.getLastPathSegment()));

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        callBack.onUploadProfileSuccess(downloadUri.toString());
                    }
                }

                else {
                    callBack.onUploadProfileFailed();
                }
            }
        });
    }

    public void uploadMessageImage(Uri imageUri, String chatId, final StorageCallbacks.ChatCallBacks callBack){
        final StorageReference imageRef = mChatImagesReference.child(chatId).child(Objects.requireNonNull(imageUri.getLastPathSegment()));

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        callBack.onUploadImageMessageSuccess(downloadUri.toString());
                    }
                }

                else {
                    callBack.onUploadImageMessageFailed();
                }
            }
        });
    }

}
