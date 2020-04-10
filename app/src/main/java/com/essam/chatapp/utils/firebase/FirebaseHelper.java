package com.essam.chatapp.utils.firebase;

import android.util.Log;

import com.essam.chatapp.utils.Consts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/** Created by esammosbah1@gmail.com 1/1/2020 */

public class FirebaseHelper {

    private static DatabaseReference appChatDb;
    private static DatabaseReference appUserDb;
    private static DatabaseReference userChatDb;

    private final static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final static DatabaseReference mReference = mDatabase.getReference();
    private static FirebaseUser firebaseUser = mAuth.getCurrentUser();

    public static String userUid = mAuth.getUid();

    private static final String TAG = FirebaseHelper.class.getSimpleName();


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
        if(userUid==null){
            userUid = mAuth.getUid();
        }
        if (userChatDb == null)
            userChatDb = mReference.child(Consts.USER).child(userUid).child(Consts.CHAT);

        return userChatDb;

    }

    /**
     * @param chatId
     * returns a database reference to user/myUid/chat/{chatId} node in firebase database
     * */
    public static DatabaseReference getReferenceToThisChatOfCurrentUser(String chatId){
        return userChatDb.child(chatId);
    }

    /**
     *
      * @param otherUid userUid of the user that this chat is with
     * @param chatId id of this chat
     * @return a database reference to user/otherUid/chat/{chatId} node in firebase database
     */
    public static DatabaseReference getReferenceToThisChatOfOtherUser(String otherUid, String chatId){
        return appUserDb.child(otherUid).child(Consts.CHAT).child(chatId);
    }

    public static boolean isUserLoggedIn(){
        return firebaseUser != null;
    }

    public static void signOut(){
        mAuth.signOut();
    }
}
