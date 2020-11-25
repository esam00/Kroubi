package com.essam.chatapp.ui.home.fragments.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.firebase.FirebaseManager;
import com.essam.chatapp.models.HomeChat;
import com.essam.chatapp.models.Profile;
import com.essam.chatapp.ui.contacts.utils.ContactsHelper;
import com.essam.chatapp.ui.home.fragments.chat.HomeChatFragment;
import com.essam.chatapp.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.List;

/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */
public class HomeChatAdapter extends RecyclerView.Adapter<HomeChatAdapter.ViewHolder> {
    private Context context;
    private HomeChatListener mHomeChatListener;
    private SortedList<HomeChat> mChatList;
    private HomeChatFragment mHomeChatFragment;

    public interface HomeChatListener {
        void onClick(HomeChat chat, int adapterPosition);
    }

    public HomeChatAdapter(HomeChatFragment homeChatFragment, HomeChatListener listener, Context context) {
        this.mHomeChatListener = listener;
        this.context = context;
        this.mHomeChatFragment = homeChatFragment;
        sort();
    }

    private void sort() {
        mChatList = new SortedList<>(HomeChat.class, new SortedListAdapterCallback<HomeChat>(this) {
            @Override
            public int compare(HomeChat o1, HomeChat o2) {
                // we want to sort conversations by the newest
                return o2.getLastMessage().getTimeStamp().compareTo(o1.getLastMessage().getTimeStamp());
            }

            @Override
            public boolean areContentsTheSame(HomeChat oldItem, HomeChat newItem) {
                // What are the scenarios of home chat to be updated?
                //1- last message updated >> check for time stamp
                //2- last message was sent by current user and message has seen >> check for seen
                //3- isTyping state changed
                return oldItem.getLastMessage().getTimeStamp().equals(newItem.getLastMessage().getTimeStamp()) &&
                        oldItem.getLastMessage().isSeen() == newItem.getLastMessage().isSeen() &&
                        oldItem.isOtherTyping() == newItem.isOtherTyping();
            }

            @Override
            public boolean areItemsTheSame(HomeChat item1, HomeChat item2) {
                return item1.getChatId().equals(item2.getChatId());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView senderNameTV, nonContactNAmeTv, lastMessageTv, dateTv, counterTv;
        private View separator;
        private ImageView profileImv, messageStateIv;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTV = itemView.findViewById(R.id.tv_sender_name);
            nonContactNAmeTv = itemView.findViewById(R.id.non_contact_name);
            lastMessageTv = itemView.findViewById(R.id.tv_sender_last_message);
            dateTv = itemView.findViewById(R.id.tv_last_message_date);
            counterTv = itemView.findViewById(R.id.tv_unseen_count);
            separator = itemView.findViewById(R.id.separator);
            messageStateIv = itemView.findViewById(R.id.iv_message_state);
            profileImv = itemView.findViewById(R.id.profile_img);

            itemView.setOnClickListener(this);
        }

        void bind(HomeChat chat, int position) {
            handleUserProfile(chat.getUserProfile());
            dateTv.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(context, chat.getLastMessage().getTimeStamp(), false));

            //unseen messages
            if (chat.getUnSeenCount() > 0) {
                counterTv.setVisibility(View.VISIBLE);
                dateTv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                counterTv.setText((NumberFormat.getInstance().format(chat.getUnSeenCount())));
//                parentView.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_effect_highlighted));
            } else {
                counterTv.setVisibility(View.GONE);
                dateTv.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
//                parentView.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_effect_basic));
            }

            if (chat.isOtherTyping()) {
                lastMessageTv.setText(context.getText(R.string.typing));
                lastMessageTv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                messageStateIv.setVisibility(View.GONE);
            } else {
                lastMessageTv.setText(chat.getLastMessage().getMessage());
                lastMessageTv.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
                handleMessageState(chat);
            }

            // hide separator for last item in the list
            if (isLastItem(position))
                separator.setVisibility(View.GONE);
            else
                separator.setVisibility(View.VISIBLE);
        }

        void handleMessageState(HomeChat chat){
            if (chat.getLastMessage().getCreatorId().equals(FirebaseManager.getInstance().getMyUid())) {
                messageStateIv.setVisibility(View.VISIBLE);
                handleSeenUi(chat.getLastMessage().isSeen());
            } else {
                messageStateIv.setVisibility(View.GONE);
            }
        }

        void handleSeenUi(boolean isSeen) {
            if (isSeen)
                messageStateIv.setImageResource(R.drawable.ic_seen);
            else
                messageStateIv.setImageResource(R.drawable.ic_sent);
        }

        void handleUserProfile(Profile profile){
            // if this user name is already saved into my contacts replace user name with this saved name
            String name = ContactsHelper.getContactName(mHomeChatFragment.getActivity(), profile.getPhone());
            senderNameTV.setText(name);

            if (profile.getPhone().equals(name) && !profile.getUserName().equals(profile.getPhone())){
                // this user is not in my contacts list
                nonContactNAmeTv.setVisibility(View.VISIBLE);
                nonContactNAmeTv.setText(String.format("~ %s", profile.getUserName()));
            }else {
                nonContactNAmeTv.setVisibility(View.GONE);
            }

            Glide.with(context).load(profile.getAvatar())
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(profileImv);
        }

        @Override
        public void onClick(View view) {
            mHomeChatListener.onClick(mChatList.get(getAdapterPosition()), getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeChat chat = mChatList.get(position);
        holder.bind(chat, position);
    }

    @Override
    public int getItemCount() {
        if (null == mChatList) {
            return 0;
        }
        return mChatList.size();
    }

    public void addAll(List<HomeChat> chats) {
        mChatList.beginBatchedUpdates();
        for (HomeChat chat : chats) {
            mChatList.add(chat);
        }
        mChatList.endBatchedUpdates();
    }

    public void updateItem(HomeChat chat) {
        mChatList.beginBatchedUpdates();
        for (int i = 0; i < mChatList.size(); i++) {
            if (mChatList.get(i).getChatId().equals(chat.getChatId())) {
                mChatList.updateItemAt(i, chat);
            }
        }
        mChatList.endBatchedUpdates();
    }

    public void clearUnSeenCount(HomeChat chat, int adapterPosition) {
        chat.setUnSeenCount(0);
        mChatList.updateItemAt(adapterPosition, chat);
    }

    private boolean isLastItem(int position) {
        return position == getItemCount() - 1;
    }
}
