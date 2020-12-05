package com.essam.chatapp.ui.home.fragments.chat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.essam.chatapp.R;
import com.essam.chatapp.models.HomeChat;
import com.essam.chatapp.ui.home.fragments.chat.adapter.HomeChatAdapter;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.ui.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class HomeChatFragment extends Fragment implements HomeChatAdapter.HomeChatListener,
        Observer, HomeChatContract.View {
    private RecyclerView homeChatRv;
    private HomeChatAdapter homeChatAdapter;
    private List<HomeChat> chatList = new ArrayList<>();
    private Context mContext;
    private boolean showNotification;
    private LinearLayout firstTimeLayout, noInternetLayout;
    private ShimmerFrameLayout loadingLayout;

    private HomeChatPresenter mPresenter;
    private final static String TAG = HomeChatFragment.class.getSimpleName();

    public HomeChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        showNotification = false;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPresenter = new HomeChatPresenter(this);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        initViews(view);
        checkContactsPermission();
        return view;
    }

    private void initViews(View view) {
        firstTimeLayout = view.findViewById(R.id.first_time_layout);
        noInternetLayout= view.findViewById(R.id.no_internet_layout);
        loadingLayout= view.findViewById(R.id.shimmer_view_container);
        // recyclerView
        chatList = new ArrayList<>();
        homeChatRv = view.findViewById(R.id.my_messages_rv);
        homeChatAdapter = new HomeChatAdapter(this, this, this.getContext());
        homeChatRv.setAdapter(homeChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        homeChatRv.setLayoutManager(layoutManager);

        Button refreshButton = view.findViewById(R.id.refresh_btn);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkContactsPermission();
            }
        });
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

    private void getUserChatList() {
        if (!ProjectUtils.isNetworkConnected(mContext)) {
            noInternetLayout.setVisibility(View.VISIBLE);
            return;
        }

        noInternetLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        mPresenter.getUserChatList();
    }

    private void displayChatList() {
        hideLoading();
        homeChatRv.setVisibility(View.VISIBLE);
        firstTimeLayout.setVisibility(View.GONE);
    }

    /**
     * If this is the first time for user display a nice welcome view or animation
     */
    private void hideChatListAndDisplayWelcomeView() {
        hideLoading();
        homeChatRv.setVisibility(View.GONE);
        firstTimeLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        Log.i(TAG, "hideLoading: ");
        loadingLayout.setVisibility(View.GONE);
    }

    private void showNotificationDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        dialog.setContentView(R.layout.dialog_welcome);
        ((TextView) dialog.findViewById(R.id.notificationText)).setText("إهداء الى قروبي الحبيب ❤️");

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * @param chat : selected chat item
     */
    @Override
    public void onClick(HomeChat chat, int adapterPosition) {
        // clear unseen count for this conversation
        homeChatAdapter.clearUnSeenCount(chat, adapterPosition);

        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        chat.getUserProfile().setOnline(false);
        intent.putExtra(Consts.PROFILE, chat.getUserProfile());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void update(Observable o, Object arg) {
        getUserChatList();
    }

    /* ----------------------------------- Presenter Callbacks -----------------------------------*/

    @Override
    public void onNewChatAdded(HomeChat chat) {
        chatList.add(chat);
        homeChatAdapter.addAll(chatList);
        displayChatList();
    }

    @Override
    public void onChatUpdated(HomeChat chat) {
        //update this chat with the new data
        homeChatAdapter.updateItem(chat);
    }

    @Override
    public void onCheckExistingChats(boolean hasPreviousChats) {
        if (!hasPreviousChats) {
            // no conversations yet
            hideChatListAndDisplayWelcomeView();
        }
    }

    @Override
    public void onNetworkError() {
        Toast.makeText(mContext, R.string.network_error_msg, Toast.LENGTH_SHORT).show();
    }
}
