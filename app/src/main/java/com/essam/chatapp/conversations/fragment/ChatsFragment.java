package com.essam.chatapp.conversations.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.essam.chatapp.R;
import com.essam.chatapp.contacts.utils.ContactsHelper;
import com.essam.chatapp.conversations.adapter.HomeChatAdapter;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.conversations.model.Chat;
import com.essam.chatapp.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment implements HomeChatAdapter.ListItemClickListener {

    private RecyclerView homeChatRv;
    private HomeChatAdapter homeChatAdapter;
    private LinearLayout welcomeLl;
    private LottieAnimationView welcomeAnimation, loadingAnimation;
    private List<Chat> chatList = new ArrayList<>();

    //firebase
    private  DatabaseReference userChatDb;
    private ChildEventListener onChatAddedEventListener;
    private ValueEventListener checkExistValueEventListener;

    private final static String TAG = ChatsFragment.class.getSimpleName();

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initFirebase();
        initViews(view);
        initEventListener();
        checkContactsPermission();
        return view;
    }

    private void initViews(View view) {
        welcomeLl = view.findViewById(R.id.welcome_ll);
        welcomeAnimation = view.findViewById(R.id.welcome_animation);
        loadingAnimation = view.findViewById(R.id.loading_animation);
        showLoading();

        // recyclerView
        chatList = new ArrayList<>();
        homeChatRv = view.findViewById(R.id.my_messages_rv);
        homeChatAdapter = new HomeChatAdapter(this, this.getContext());
        homeChatRv.setAdapter(homeChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        homeChatRv.setLayoutManager(layoutManager);
        homeChatAdapter.setMessagesData(chatList);
    }

    private void initFirebase(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mReference = mDatabase.getReference();

        String userUid = mAuth.getUid();
        if(userUid!=null) userChatDb = mReference.child(Consts.USER).child(userUid).child(Consts.CHAT);
    }

    private void initEventListener() {
        // this value event listener is triggered once a new chat added Or existing chat updated
        onChatAddedEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, "new chat added with : " + chat.getUserName());
                    // if this user name is already saved into my contacts replace user name with this saved name
                    chat.setUserName(ContactsHelper.getContactName(getActivity(), chat.getUserName()));

                    chatList.add(chat);
                    homeChatAdapter.setMessagesData(chatList);
                    displayChatList();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    Log.i(TAG, "new message added to chat with : " + chat.getUserName());

                    //update this chat with the new data
                    for (int i = 0; i < chatList.size(); i++) {
                        if (chatList.get(i).getChatId().equals(chat.getChatId())) {
                            chatList.get(i).setMessage(chat.getMessage());
                            chatList.get(i).setCreatedAt(chat.getCreatedAt());
                            chatList.get(i).setUnSeenCount(chat.getUnSeenCount());
                            homeChatAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError);
            }
        };

        //single value event listener to check if user has any previous chats
        checkExistValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i(TAG, "onDataChange: Exist");
                } else {
                    Log.i(TAG, "onDataChange: not exist");
                    hideChatListAndDisplayWelcomeAnimation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    /**
     * Before fetching user conversations list we need to be sure that user has accepted READ_CONTACTS
     * permission , we might need this permission granted to display chat's user name as it is saved [ if it is saved]
     * in device contacts list
     */
    private void checkContactsPermission() {
        if (ProjectUtils.hasPermissionInManifest(getActivity(),
                Consts.READ_CONTACTS_REQUEST,
                Manifest.permission.READ_CONTACTS))
            // if contacts permission already granted >> fetch chats list
            getUserChatList();
    }

    /**
     * This is the main method that is responsible for fetching previous chats AND listen for new messages
     */
    private void getUserChatList() {
        userChatDb.addChildEventListener(onChatAddedEventListener);
        userChatDb.addListenerForSingleValueEvent(checkExistValueEventListener);
    }

    /**
     * hide loading and display user chats list as soon as they are successfully fetched
     */
    private void displayChatList() {
        hideLoading();
        homeChatRv.setVisibility(View.VISIBLE);
        welcomeLl.setVisibility(View.GONE);
        loadingAnimation.setVisibility(View.GONE);
    }

    /**
     * If this is the first time for user display a nice welcome view or animation
     */
    private void hideChatListAndDisplayWelcomeAnimation() {
        hideLoading();
        homeChatRv.setVisibility(View.GONE);
        welcomeLl.setVisibility(View.VISIBLE);
        welcomeAnimation.playAnimation();
    }

    private void hideLoading() {
        Log.i(TAG, "hideLoading: ");
        loadingAnimation.setVisibility(View.GONE);
        loadingAnimation.cancelAnimation();
    }

    private void showLoading() {
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }

    private void showNotificationDialog(String message) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.notification_dialog);
        dialog.setCancelable(true);
        ((TextView) dialog.findViewById(R.id.notificationText)).setText(message);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * onClick method of listItemClickListener interface in chats adapter
     * if a chat is clicked this listener will be triggered and it will  return the index of the item that was clicked
     *
     * @param index
     */
    @Override
    public void onClick(int index) {
        // clear un seen count for this conversation
        chatList.get(index).setUnSeenCount(0);
        homeChatAdapter.notifyDataSetChanged();

        Bundle bundle = new Bundle();
        bundle.putString(Consts.USER_UID, chatList.get(index).getUserUid());
        bundle.putString(Consts.USER_NAME, chatList.get(index).getUserName());
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        userChatDb.removeEventListener(onChatAddedEventListener);
        userChatDb.removeEventListener(checkExistValueEventListener);
        super.onPause();
    }

    @Override
    public void onResume() {
        initEventListener();
        super.onResume();
    }
}
