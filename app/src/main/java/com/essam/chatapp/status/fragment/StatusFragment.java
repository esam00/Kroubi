package com.essam.chatapp.status.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.essam.chatapp.R;
import com.essam.chatapp.conversations.adapter.HomeChatAdapter;
import com.essam.chatapp.utils.firebase.FirebaseHelper;
import com.essam.chatapp.conversations.model.Chat;
import com.essam.chatapp.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends Fragment {

    private RecyclerView homeChatRv;
    private HomeChatAdapter homeChatAdapter;
    private LinearLayout welcomeLl;
    private LottieAnimationView welcomeAnimation,loadingAnimation;
    private List<Chat> chatList = new ArrayList<>();

    private DatabaseReference appUserDb;


    public StatusFragment() {
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
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        appUserDb = FirebaseHelper.getAppUserDbReference();

        initViews(view);
        return view;
    }

    private void initViews(View view) {
        welcomeLl = view.findViewById(R.id.welcome_ll);
        welcomeAnimation = view.findViewById(R.id.welcome_animation);
        loadingAnimation = view.findViewById(R.id.loading_animation);

        chatList = new ArrayList<>();
        homeChatRv = view.findViewById(R.id.my_messages_rv);
        homeChatRv.setAdapter(homeChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        homeChatRv.setLayoutManager(layoutManager);
        homeChatRv.setVisibility(View.INVISIBLE);
    }
}
