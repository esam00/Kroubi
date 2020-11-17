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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.essam.chatapp.R;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.contacts.utils.ContactsHelper;
import com.essam.chatapp.ui.home.fragments.chat.adapter.HomeChatAdapter;
import com.essam.chatapp.utils.ProjectUtils;
import com.essam.chatapp.models.Chat;
import com.essam.chatapp.ui.chat.activity.ChatActivity;
import com.essam.chatapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class HomeChatFragment extends Fragment implements HomeChatAdapter.HomeChatListener,
        Observer, HomeChatContract.View {

    private RecyclerView homeChatRv;
    private HomeChatAdapter homeChatAdapter;
    private LinearLayout welcomeLl;
    private List<Chat> chatList = new ArrayList<>();
    private Dialog loadingDialog;

    private Context mContext;
    private boolean showNotification;

    private HomeChatPresenter mPresenter = new HomeChatPresenter(this);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initViews(view);
        checkContactsPermission();
        return view;
    }

    private void initViews(View view) {
        welcomeLl = view.findViewById(R.id.welcome_ll);

        // recyclerView
        chatList = new ArrayList<>();
        homeChatRv = view.findViewById(R.id.my_messages_rv);
        homeChatAdapter = new HomeChatAdapter(this, this, this.getContext());
        homeChatRv.setAdapter(homeChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        homeChatRv.setLayoutManager(layoutManager);
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
     * fetching previous chats AND listen for new messages
     */
    private void getUserChatList() {
        if (!ProjectUtils.isNetworkConnected(mContext)) {
            ProjectUtils.showToast(getActivity(), "Check your network connection!");
            return;
        }
        showLoadingDialog();
        mPresenter.getUserChatList();
    }

    /**
     * hide loading and display user chats list as soon as they are successfully fetched
     */
    private void displayChatList() {
        hideLoading();
        homeChatRv.setVisibility(View.VISIBLE);
        welcomeLl.setVisibility(View.GONE);
    }

    /**
     * If this is the first time for user display a nice welcome view or animation
     */
    private void hideChatListAndDisplayWelcomeAnimation() {
        hideLoading();
        homeChatRv.setVisibility(View.GONE);
        welcomeLl.setVisibility(View.VISIBLE);
    }

    private void showLoadingDialog() {
        loadingDialog = new Dialog(mContext);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);

        LottieAnimationView lottieAnimationView = loadingDialog.findViewById(R.id.loading_animation);
        lottieAnimationView.playAnimation();

        loadingDialog.show();
        Window window = loadingDialog.getWindow();
        if (window != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void hideLoading() {
        Log.i(TAG, "hideLoading: ");
        loadingDialog.dismiss();
        if (showNotification) showNotificationDialog();
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
     * onClick method of listItemClickListener interface in chats adapter
     * if a chat is clicked this listener will be triggered and it will  return the index of the item that was clicked
     *
     * @param chat : selected item
     */
    @Override
    public void onClick(Chat chat, int adapterPosition) {
        // clear un seen count for this conversation
        homeChatAdapter.clearUnSeenCount(chat, adapterPosition);

        //Chat Activity only accepts User object as extras..
        Profile profile = new Profile(
                chat.getUserUid(),
                chat.getUserPhone(),
                chat.getUserPhone(),
                chat.getUserPhoto(),
                "",
                false);
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        intent.putExtra(Consts.PROFILE, profile);
        startActivity(intent);
    }

    @Override
    public void update(Observable o, Object arg) {
        getUserChatList();
    }

    @Override
    public void onNewChatAdded(Chat chat) {
        // if this user name is already saved into my contacts replace user name with this saved name
        chat.setUserPhone(ContactsHelper.getContactName(getActivity(), chat.getUserPhone()));

        chatList.add(chat);
        homeChatAdapter.addAll(chatList);
        displayChatList();
    }

    @Override
    public void onChatUpdated(Chat chat) {
        //update this chat with the new data
        homeChatAdapter.updateItem(chat);
    }

    @Override
    public void onCheckExistingChats(boolean hasPreviousChats) {
        if (!hasPreviousChats){
            // no conversations yet
            hideChatListAndDisplayWelcomeAnimation();
        }
    }

    @Override
    public void onNetworkError() {
        Toast.makeText(mContext, R.string.network_error_msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
