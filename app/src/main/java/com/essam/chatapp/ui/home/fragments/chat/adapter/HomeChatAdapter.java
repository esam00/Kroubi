package com.essam.chatapp.ui.home.fragments.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.contacts.utils.ContactsHelper;
import com.essam.chatapp.ui.home.fragments.chat.HomeChatFragment;
import com.essam.chatapp.models.Chat;
import com.essam.chatapp.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.List;

/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */

public class HomeChatAdapter extends RecyclerView.Adapter<HomeChatAdapter.ViewHolder> {

    private Context context;
    private ListItemClickListener listItemClickListener;
    private SortedList<Chat> mChatList;
    private HomeChatFragment mHomeChatFragment;

    public interface ListItemClickListener {
        void onClick(Chat chat,int adapterPosition);
    }

    public HomeChatAdapter(HomeChatFragment homeChatFragment, ListItemClickListener listener, Context context) {
        this.listItemClickListener = listener;
        this.context = context;
        this.mHomeChatFragment = homeChatFragment;
        sort();
    }

    private void sort() {
        mChatList = new SortedList<>(Chat.class, new SortedListAdapterCallback<Chat>(this) {
            @Override
            public int compare(Chat o1, Chat o2) {
                // we want to sort conversations by the newest
                return o2.getTimeStamp().compareTo(o1.getTimeStamp());
            }

            @Override
            public boolean areContentsTheSame(Chat oldItem, Chat newItem) {
                return oldItem.getTimeStamp().equals(newItem.getTimeStamp());
            }

            @Override
            public boolean areItemsTheSame(Chat item1, Chat item2) {
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
        private TextView senderNameTV, lastMessageTv, dateTv, counterTv;
        private View separator;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTV = itemView.findViewById(R.id.tv_sender_name);
            lastMessageTv = itemView.findViewById(R.id.tv_sender_last_message);
            dateTv = itemView.findViewById(R.id.tv_last_message_date);
            counterTv = itemView.findViewById(R.id.tv_unseen_count);
            separator = itemView.findViewById(R.id.separator);

            itemView.setOnClickListener(this);

        }

        void bind(Chat chat, int position) {
            // update ui [name, messageText, date]
            senderNameTV.setText(chat.getUserPhone());
            lastMessageTv.setText(chat.getMessage());
            dateTv.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(context,chat.getTimeStamp(), false));

            //colors
            if (chat.getUnSeenCount() > 0) {
                counterTv.setVisibility(View.VISIBLE);
                dateTv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                counterTv.setText((NumberFormat.getInstance().format(chat.getUnSeenCount())));
            } else {
                counterTv.setVisibility(View.GONE);
                dateTv.setTextColor(context.getResources().getColor(R.color.dark_gray));
            }

            // hide separator for last item in the list
            if (isLastItem(position))
                separator.setVisibility(View.GONE);
            else
                separator.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            listItemClickListener.onClick(mChatList.get(getAdapterPosition()),getAdapterPosition());
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
        Chat chat = mChatList.get(position);
        holder.bind(chat, position);
    }

    @Override
    public int getItemCount() {
        if (null == mChatList) {
            return 0;
        }
        return mChatList.size();
    }

    public void addAll(List<Chat> chats) {
        mChatList.beginBatchedUpdates();
        for(Chat chat : chats){
            mChatList.add(chat);
        }
        mChatList.endBatchedUpdates();
    }

    public void updateItem(Chat chat) {
        mChatList.beginBatchedUpdates();
        for (int i = 0; i < mChatList.size(); i++) {
            if (mChatList.get(i).getChatId().equals(chat.getChatId())) {
                chat.setUserPhone(ContactsHelper.getContactName(mHomeChatFragment.getActivity(), chat.getUserPhone()));
                mChatList.updateItemAt(i,chat);
            }
        }
        mChatList.endBatchedUpdates();
    }

    public void clearUnSeenCount(Chat chat,int adapterPosition){
        chat.setUnSeenCount(0);
        mChatList.updateItemAt(adapterPosition,chat);
    }

    private boolean isLastItem(int position) {
        return position == getItemCount() - 1;
    }

}
