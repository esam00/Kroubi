package com.essam.chatapp.conversations.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.essam.chatapp.R;
import com.essam.chatapp.contacts.utils.ContactsHelper;
import com.essam.chatapp.conversations.adapter.HomeChatAdapter;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.utils.firebase.FirebaseHelper;
import com.essam.chatapp.conversations.model.Chat;
import com.essam.chatapp.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment implements HomeChatAdapter.ListItemClickListener {

    private RecyclerView homeChatRv;
    private HomeChatAdapter homeChatAdapter;
    private LinearLayout welcomeLl;
    private LottieAnimationView welcomeAnimation, loadingAnimation;
    private List<Chat> chatList = new ArrayList<>();

    private DatabaseReference appUserDb;
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

        appUserDb = FirebaseHelper.getAppUserDbReference();

        checkContactsPermission();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        welcomeLl = view.findViewById(R.id.welcome_ll);
        welcomeAnimation = view.findViewById(R.id.welcome_animation);
        loadingAnimation = view.findViewById(R.id.loading_animation);
        showLoading();

        // recycler view
        chatList = new ArrayList<>();
        homeChatRv = view.findViewById(R.id.my_messages_rv);
        homeChatAdapter = new HomeChatAdapter(this, this.getContext());
        homeChatRv.setAdapter(homeChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        homeChatRv.setLayoutManager(layoutManager);
        homeChatAdapter.setMessagesData(chatList);
    }

    private void getUserChatList() {
        DatabaseReference mUserChatDb = FirebaseHelper.getUserChatDbReference();
        mUserChatDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                        for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                            String name = "";
                            Chat chat = new Chat();
                            chat.setChatId(childSnapShot.getKey());
                            chat.setSentAt(childSnapShot.child(Consts.CREATED_AT).getValue().toString());
                            chat.setLastMessage(childSnapShot.child(Consts.TEXT).getValue().toString());
                            chat.setUnSeenCount(Integer.parseInt(childSnapShot.child(Consts.UNSEEN_COUNT).getValue().toString()));
                            name = (childSnapShot.child(Consts.USER_NAME).getValue().toString());
                            chat.setSenderName(ContactsHelper.getContactName(getActivity(), name));

                            boolean exists = false;
                            for (int i = 0; i < chatList.size(); i++) {
                                if (chatList.get(i).getChatId().equals(childSnapShot.getKey())) {
                                    chatList.get(i).setLastMessage(childSnapShot.child(Consts.TEXT).getValue().toString());
                                    chatList.get(i).setSentAt(childSnapShot.child(Consts.CREATED_AT).getValue().toString());
                                    chatList.get(i).setUnSeenCount(Integer.parseInt(childSnapShot.child(Consts.UNSEEN_COUNT).getValue().toString()));
                                    homeChatAdapter.notifyDataSetChanged();
                                    exists = true;
                                }
                            }
                            if (exists) continue;
                            chatList.add(chat);
                            homeChatAdapter.setMessagesData(chatList);
                            displayChatList();
                        }
                } else {
                    hideChatListAndDisplayWelcomeAnimation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayChatList() {
        hideLoading();
        homeChatRv.setVisibility(View.VISIBLE);
        welcomeLl.setVisibility(View.GONE);
        loadingAnimation.setVisibility(View.GONE);
    }

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

    private void checkContactsPermission() {
        if (ProjectUtils.hasPermissionInManifest(getActivity(), Consts.READ_CONTACTS_REQUEST, Manifest.permission.READ_CONTACTS))
            getUserChatList();
    }

    @Override
    public void onClick(int index) {
        // clear un seen count for this conversation
        chatList.get(index).setUnSeenCount(0);
        homeChatAdapter.notifyDataSetChanged();

        Bundle bundle = new Bundle();
        bundle.putString(Consts.CHAT_ID, chatList.get(index).getChatId());
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
