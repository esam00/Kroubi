package com.essam.chatapp.conversations.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.essam.chatapp.R;
import com.essam.chatapp.conversations.model.Chat;
import com.essam.chatapp.utils.DateTimeUtils;

import java.util.List;

/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */

public class HomeChatAdapter extends RecyclerView.Adapter<HomeChatAdapter.ViewHolder> {

    private List<Chat> mChatList;
    private Context context;
    private ListItemClickListener listItemClickListener;

    public interface ListItemClickListener {
        void onClick(int index);
    }

    public HomeChatAdapter(ListItemClickListener listener,Context context) {
        this.listItemClickListener = listener;
        this.context = context;
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

        void bind (Chat chat, int position){
            // update ui [name, message text, date
            senderNameTV.setText(chat.getUserPhone());
            lastMessageTv.setText(chat.getMessage());
            dateTv.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(chat.getTimeStamp(),false));

            //colors
            if(chat.getUnSeenCount()>0){
                counterTv.setVisibility(View.VISIBLE);
                dateTv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                counterTv.setText(String.valueOf(chat.getUnSeenCount()));
            }else {
                counterTv.setVisibility(View.INVISIBLE);
                dateTv.setTextColor(context.getResources().getColor(R.color.dark_gray));
            }

            // hide separator for last item in the list
            if(isLastItem(position))
                separator.setVisibility(View.GONE);
            else
                separator.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            listItemClickListener.onClick(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_chat_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChatList.get(position);
        holder.bind(chat,position);
    }

    @Override
    public int getItemCount() {
        if(null== mChatList){
            return 0;
        }
        return mChatList.size();
    }

    public void setMessagesData(List<Chat> chatList) {
        mChatList = chatList;
        notifyDataSetChanged();
    }

    private boolean isLastItem(int position){
        return position == getItemCount()-1;
    }

}
