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

/** FirebaseManager is a Singleton class that holds all firebase database references
 * Also holds UserAuth state and manages other cases ..
 * This is a very helpful class as we don't have to create DatabaseReference object every time.
 * we just declare and initialize those references once, and use the helper methods that attache and
 * remove eventListeners to them. YAAi ^_^
 * */

public class FirebaseManager {
    public enum UserAuthState{
        LOGGED_IN,
        LOGGED_OUT,
    }
    public boolean isFirstTime = false;
    private Profile currentUserProfile;

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

        if (isUserLoggedIn()){
            mUserDb = appUserDb.child(getMyUid());
            userChatDb = mUserDb.child(Consts.CHAT);
            userProfileDb = mUserDb.child(Consts.PROFILE);
        }
    }

    public Profile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void setCurrentUserProfile(Profile currentUserProfile) {
        this.currentUserProfile = currentUserProfile;
    }

    public String getMyUid() {
        return getFirebaseUser().getUid();
    }

    public String getMyPhone() {
        return getFirebaseUser().getPhoneNumber();
    }

    public void getCurrentUserProfile(ValueEventListener listener){
        userProfileDb.addValueEventListener(listener);
    }

    public void removeCurrentUserProfileListener(ValueEventListener listener){
        userProfileDb.removeEventListener(listener);
    }

    /* ---------------------------------- Authentication ----------------------------------------*/

    public void updateUserAuthState(UserAuthState state){
        switch (state){
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

    public FirebaseUser getFirebaseUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    public void signOutUser() {
        getFirebaseAuth().signOut();
        toggleOnlineState(false);
        instance = null;
    }

    private void logInUser(){
        initFirebase();
    }

    /* -------------------------------------- Login ---------------------------------------------*/

    public void checkIfUserExistInDataBase(ValueEventListener eventListener){
        mUserDb.addListenerForSingleValueEvent(eventListener);
    }

    public void addNewUserToDataBase(Profile userProfile){
        isFirstTime = true;
        currentUserProfile = userProfile;
        User user = new User(
                getMyUid(),
                getMyPhone(),
                userProfile
        );
        mUserDb.setValue(user);
    }

    public void toggleOnlineState(boolean isOnline){
        if (isFirstTime) return; // user just signed in and state is online by default
        if (isOnline) {
            userProfileDb.child(Consts.IS_ONLINE).setValue(true);
        } else {
            if (isUserLoggedIn()) // user just signed out and state updated to offline
                userProfileDb.child(Consts.IS_ONLINE).setValue(false);
        }
    }

    public void getUserProfileInfo(String userId, ValueEventListener listener){
        appUserDb.child(userId).child(Consts.PROFILE).addValueEventListener(listener);
    }

    public void removeUserProfileListener(String userId, ValueEventListener listener){
        appUserDb.child(userId).child(Consts.PROFILE).removeEventListener(listener);
    }

    /* -------------------------------------- Home Chat ----------------------------------------*/

    /**
     * Check if current user has any chat history
     */
    public void checkChatHistoryForCurrentUser (ValueEventListener eventListener){
        userChatDb.addListenerForSingleValueEvent(eventListener);
    }

    /**
     * fetch previous chats AND listen for new messages
     */
    public void getUserChatList(ChildEventListener eventListener){
        userChatDb.addChildEventListener(eventListener);
    }

    public void removeHomeChatListeners(ChildEventListener eventListener) {
        if (eventListener != null){
            userChatDb.removeEventListener(eventListener);
        }
    }

    /* ----------------------------------- One To One Chat -------------------------------------*/

    public String pushNewTopLevelChat(){
        return appChatDb.push().getKey();
    }

    public DatabaseReference getReferenceToSpecificAppChat(String chatId){
        return appChatDb.child(chatId);
    }

    public DatabaseReference getReferenceToSpecificUserChat(String chatId){
        return userChatDb.child(chatId);
    }

    public DatabaseReference getReferenceToSpecificUserChat(String userUid, String chatId){
        return appUserDb.child(userUid).child(Consts.CHAT).child(chatId);
    }

}
