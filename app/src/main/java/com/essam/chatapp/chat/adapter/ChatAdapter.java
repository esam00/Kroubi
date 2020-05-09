package com.essam.chatapp.chat.adapter;


/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.essam.chatapp.R;
import com.essam.chatapp.chat.model.Message;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.DateTimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    private List<Message> mMessages;
    private Context mContext;
    private DatabaseReference chatDb;

    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(Context context) {
        this.mContext = context;
    }

    // Determine the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = mMessages.get(position);
        boolean isReceiving = !message.getCreatorId().equals(FirebaseAuth.getInstance().getUid());

        if (isReceiving) {
            return VIEW_TYPE_RECEIVED;
        } else {
            return VIEW_TYPE_OUTGOING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_OUTGOING) {
            view = inflater.inflate(R.layout.outgoing_list_item, parent, false);
            return new SenderViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.receiver_list_item, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_OUTGOING:
                ((SenderViewHolder) holder).bind(position);
                break;
            case VIEW_TYPE_RECEIVED:
                ((ReceiverViewHolder) holder).bind(position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (null == mMessages) {
            return 0;
        }
        return mMessages.size();
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView smallMessageTv;
        private TextView sentAtTextView;
        private ImageView messageStateIV;
        private ImageView imageMessageIv;
        private LinearLayout llMessageBody;

        SenderViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            smallMessageTv = itemView.findViewById(R.id.tv_small_message);
            sentAtTextView = itemView.findViewById(R.id.tv_time);
            messageStateIV = itemView.findViewById(R.id.iv_message_state);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
            llMessageBody = itemView.findViewById(R.id.ll_message_body);
        }

        void bind(final int position) {
            final Message message = mMessages.get(position);

            // set background
            setItemLayoutBackground(position);

            //bind message sent time
            sentAtTextView.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(message.getTimeStamp(),true));

            //bind message text
            bindMessageText(message);

            //bind message image
            bindMessageMedia(message);

            //bind seen
            bindSeenLogic(message);
        }

        void bindMessageText(Message message) {
            String textMinimumLong = "This is a short message";
            String messageText = message.getMessage();
            int messageLength = messageText.length();

            if (messageLength <= textMinimumLong.length()) {
                messageTextView.setVisibility(View.GONE);
                smallMessageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setText(message.getMessage());
            } else {
                messageTextView.setVisibility(View.VISIBLE);
                smallMessageTv.setVisibility(View.GONE);
                messageTextView.setText(message.getMessage());
            }

        }

        void bindMessageMedia(final Message message) {
            if (message.getMedia() != null && !message.getMedia().isEmpty()) {
                imageMessageIv.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(Uri.parse(message.getMedia())).into(imageMessageIv);
                imageMessageIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogZoom(Uri.parse(message.getMedia()));
                    }
                });
            } else {
                imageMessageIv.setVisibility(View.GONE);
            }
        }

        void bindSeenLogic(Message message) {
            if (message.isSeen())
                messageStateIV.setImageResource(R.drawable.ic_seen);
            else
                messageStateIV.setImageResource(R.drawable.ic_sent);
        }

        void setItemLayoutBackground(int position) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            Message prevMessage = null;
            boolean isPreviousMessageReceivingType = false;

            if (position > 0) {
                prevMessage = mMessages.get(position - 1);
                isPreviousMessageReceivingType = !prevMessage.getCreatorId().equals(FirebaseAuth.getInstance().getUid());
            }

            if (prevMessage != null && !isPreviousMessageReceivingType) {
                params.setMargins(3, 5, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(mContext.getResources().getDrawable(R.drawable.outgoing_second));

            } else {
                // If some other user sent the message
                params.setMargins(3, 25, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(mContext.getResources().getDrawable(R.drawable.outgoing_first));
            }
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView smallMessageTv;
        private TextView sentAtTextView;
        private ImageView imageMessageIv;
        private LinearLayout llMessageBody;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            smallMessageTv = itemView.findViewById(R.id.tv_small_message);
            sentAtTextView = itemView.findViewById(R.id.tv_time);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
            llMessageBody = itemView.findViewById(R.id.ll_message_body);
        }

        void bind(final int position) {
            final Message message = mMessages.get(position);

            // set background
            setItemLayoutBackground(position);

            //bind message time
            sentAtTextView.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(message.getTimeStamp(),true));

            //update message seen = true
            updateSeenLogic(message);

            //bind message text
            bindMessageText(message);

            //bind message image
            bindMessageMedia(message);
        }

        void setItemLayoutBackground(int position) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            Message prevMessage = null;
            boolean isPreviousMessageReceivingType = false;

            if (position > 0) {
                prevMessage = mMessages.get(position - 1);
                isPreviousMessageReceivingType = !prevMessage.getCreatorId().equals(FirebaseAuth.getInstance().getUid());
            }

            if (prevMessage != null && isPreviousMessageReceivingType) {
                params.setMargins(3, 5, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(mContext.getResources().getDrawable(R.drawable.receiving_second));

            } else {
                // If some other user sent the message
                params.setMargins(3, 25, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(mContext.getResources().getDrawable(R.drawable.receiving_first));
            }
        }

        void bindMessageText(Message message) {
            String textMinimumLong = "This is a short message";
            String messageText = message.getMessage();
            int messageLength = messageText.length();

            if (messageLength <= textMinimumLong.length()) {
                messageTextView.setVisibility(View.GONE);
                smallMessageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setText(message.getMessage());
            } else {
                messageTextView.setVisibility(View.VISIBLE);
                smallMessageTv.setVisibility(View.GONE);
                messageTextView.setText(message.getMessage());
            }

        }

        void bindMessageMedia(final Message message) {
            if (message.getMedia() != null && !message.getMedia().isEmpty()) {
                imageMessageIv.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(Uri.parse(message.getMedia())).into(imageMessageIv);
                imageMessageIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogZoom(Uri.parse(message.getMedia()));
                    }
                });
            } else {
                imageMessageIv.setVisibility(View.GONE);
            }
        }

        void updateSeenLogic(Message message) {
            DatabaseReference msgDb = chatDb.child(message.getMessageId());
            msgDb.child(Consts.SEEN).setValue(true);
        }
    }

    public void setMessagesData(List<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    /**
     * get the reference to this chat id chat database >> chat/chatId
     *
     * @param mChatDb >> chat/chatId
     */
    public void setChatDp(DatabaseReference mChatDb) {
        chatDb = mChatDb;
        notifyDataSetChanged();
    }

    public void updateSeen(int index) {
        mMessages.get(index).setSeen(true);
        mMessages.get(mMessages.size() - 1).setSeen(true);
        notifyDataSetChanged();
    }

    private void dialogZoom(Uri imgUrl) {
        Dialog zoomDialog = new Dialog(mContext);
        zoomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        zoomDialog.setContentView(R.layout.dialog_image_zoom);

        ImageView zoomDialogIv = zoomDialog.findViewById(R.id.ZoomDialogIv);
        Glide.with(mContext).
                load(imgUrl)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(zoomDialogIv);
        zoomDialog.show();
        Window window = zoomDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
