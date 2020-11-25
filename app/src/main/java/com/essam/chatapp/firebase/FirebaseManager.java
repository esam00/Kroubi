package com.essam.chatapp.firebase;

import com.essam.chatapp.models.Profile;
import com.essam.chatapp.models.User;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * FirebaseManager is a Singleton class that holds all firebase database references
 * Also holds UserAuth state and manages other cases ..
 * This is a very helpful class as we don't have to create DatabaseReference object every time.
 * we just declare and initialize those references once, and use the helper methods that attache and
 * remove eventListeners to them. YAAi ^_^
 */

public class FirebaseManager {
    public enum UserAuthState {
        LOGGED_IN,
        LOGGED_OUT,
    }

    private static FirebaseManager instance;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference appUserDb;        //App/user/
    private DatabaseReference appChatDb;        //App/chat/
    private DatabaseReference mUserDb;          //App/user/uid/
    private DatabaseReference userChatDb;       //App/user/uid/chat/
    private DatabaseReference userProfileDb;    //App/user/uid/profile/

    private FirebaseManager() {
        initFirebase();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private void initFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference rootDbReference = FirebaseDatabase.getInstance().getReference();
        appUserDb = rootDbReference.child(Consts.USER);
        appChatDb = rootDbReference.child(Consts.CHAT);

        if (isUserLoggedIn()) {
            mUserDb = appUserDb.child(getMyUid());
            userChatDb = mUserDb.child(Consts.CHAT);
            userProfileDb = mUserDb.child(Consts.PROFILE);
        }
    }

    /* ---------------------------------- Authentication ----------------------------------------*/

    public FirebaseUser getFirebaseUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    public void updateUserAuthState(UserAuthState state) {
        switch (state) {
            case LOGGED_IN:
                logInUser();
                break;
            case LOGGED_OUT:
                signOutUser();
        }
    }

    public boolean isUserLoggedIn() {
        return getFirebaseUser() != null;
    }

    private void signOutUser() {
        toggleOnlineState(false);
        getFirebaseAuth().signOut();
        instance = null;
    }

    private void logInUser() {
        initFirebase();
    }

    /* ----------------------------------- One To One Chat --------------------------------------*/

    /**
     * We just need to check if user has any previous chat history
     *
     * @param eventListener So add a single value event listener to userCharDb reference
     */
    public void getAllChatHistory(ValueEventListener eventListener) {
        userChatDb.addListenerForSingleValueEvent(eventListener);
    }

    /**
     * Here we need to Get all chat history and be notified whenever a new child added updated
     *
     * @param eventListener so we add a child listener to userCharDb reference
     */
    public void getAllChatHistory(ChildEventListener eventListener) {
        userChatDb.addChildEventListener(eventListener);
    }

    /**
     * Push new chat child to top level app/chat database reference
     * @return unique id of the new child
     */
    public String pushNewTopLevelChat() {
        return appChatDb.push().getKey();
    }

    /**
     * @param chatId unique id of chat child
     * @return app/chat/chatId database reference in firebase database
     */
    public DatabaseReference getReferenceToSpecificAppChat(String chatId) {
        return appChatDb.child(chatId);
    }

    /** We need a reference to this chat within current user
     * @param chatId unique id of chat child
     * @return app/user/myUid/chat/chatId database reference in firebase database
     */
    public DatabaseReference getReferenceToSpecificUserChat(String chatId) {
        return userChatDb.child(chatId);
    }

    /** get a reference to this chat within a provided user
     * @param chatId unique id of chat child
     * @return app/user/{userUid}/chat/chatId database reference in firebase database
     */
    public DatabaseReference getReferenceToSpecificUserChat(String userUid, String chatId) {
        return appUserDb.child(userUid).child(Consts.CHAT).child(chatId);
    }

    public void removeHomeChatListener(ChildEventListener eventListener) {
        userChatDb.removeEventListener(eventListener);
    }

    /*------------------------------------- User ---------------------------------------------*/

    public String getMyUid() {
        return getFirebaseUser().getUid();
    }

    public String getMyPhone() {
        return getFirebaseUser().getPhoneNumber();
    }

    public void checkIfUserExistInDataBase(ValueEventListener eventListener) {
        mUserDb.addListenerForSingleValueEvent(eventListener);
    }

    /**
     * Adding new child to app/user db with the basic info we just got like
     * [Uid, phone and an empty profile]
     */
    public void addNewUserToDataBase() {
        User user = new User(
                getMyUid(),
                getMyPhone(),
                new Profile()
        );
        mUserDb.setValue(user);
    }

    public void toggleOnlineState(boolean isOnline) {
        if (isUserLoggedIn()){
            userProfileDb.child(Consts.IS_ONLINE).setValue(isOnline);
        }
    }

    public void getUserProfileInfo(String userId, ValueEventListener listener) {
        appUserDb.child(userId).child(Consts.PROFILE).addValueEventListener(listener);
    }

    public void removeUserProfileListener(String userId, ValueEventListener listener) {
        appUserDb.child(userId).child(Consts.PROFILE).removeEventListener(listener);
    }

    public void updateUserProfile(Profile profile) {
        userProfileDb.setValue(profile);
    }

    public void updateUserStatus(String status) {
        userProfileDb.child(Consts.STATUS).setValue(status);
    }

    public void listenForCurrentUserProfileChanges(ValueEventListener listener) {
        userProfileDb.addValueEventListener(listener);
    }

    public void removeCurrentUserProfileListener(ValueEventListener listener) {
        userProfileDb.removeEventListener(listener);
    }
}
