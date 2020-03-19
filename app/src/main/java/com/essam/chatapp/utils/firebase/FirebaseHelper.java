package com.essam.chatapp.utils.firebase;

import com.essam.chatapp.utils.Consts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static DatabaseReference appChatDb;
    private static DatabaseReference appUserDb;
    private static DatabaseReference userChatDb;

    private final static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final static DatabaseReference mReference = mDatabase.getReference();
    private final static String userUid = mAuth.getUid();


    /**
    * returns a database reference to chat node in firebase database
    * */
    public static DatabaseReference getAppChatDbReference() {

        if (appChatDb == null)
            appChatDb = mReference.child(Consts.CHAT);

        return  appChatDb;
    }

    /**
     * returns a database reference to user node in firebase database
     * */
    public static DatabaseReference getAppUserDbReference() {
        if (appUserDb == null)
            appUserDb = mReference.child(Consts.USER);

        return appUserDb;

    }

    /**
     * returns a database reference to user/chat node in firebase database
     * */
    public static DatabaseReference getUserChatDbReference() {
        if (userChatDb == null && userUid!=null)
            userChatDb = mReference.child(Consts.USER).child(userUid).child(Consts.CHAT);

        return userChatDb;

    }
}
