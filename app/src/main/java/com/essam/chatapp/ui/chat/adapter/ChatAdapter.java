package com.essam.chatapp.ui.chat.adapter;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.essam.chatapp.R;
import com.essam.chatapp.firebase.data.FirebaseManager;
import com.essam.chatapp.models.Content;
import com.essam.chatapp.models.Message;
import com.essam.chatapp.utils.DateTimeUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> mMessages;
    private Context mContext;
    private ChatListener mListener;

    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(ChatListener listener, Context context) {
        this.mContext = context;
        this.mListener = listener;
    }

    public interface ChatListener {
        void onUpdateComingMessageAsSeen(String messageId);
    }

    @Override
    public int getItemViewType(int position) {
        // getItemViewType according to the message creator.
        if (mMessages.get(position).getCreatorId().equals(FirebaseManager.getInstance().getMyUid())) {
            return VIEW_TYPE_OUTGOING;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_OUTGOING) {
            view = inflater.inflate(R.layout.item_outgoing_message, parent, false);
            return new SenderViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_OUTGOING:
                ((SenderViewHolder) holder).bind(mMessages.get(position));
                break;
            case VIEW_TYPE_RECEIVED:
                ((ReceiverViewHolder) holder).bind(mMessages.get(position));
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
        private TextView messageTv, smallMessageTv, sentAtTv;
        private ImageView messageStateIV, imageMessageIv;
        private LinearLayout llMessageBody;
        private ProgressBar loadingProgress;

        SenderViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            smallMessageTv = itemView.findViewById(R.id.tv_small_message);
            sentAtTv = itemView.findViewById(R.id.tv_time);
            messageStateIV = itemView.findViewById(R.id.iv_message_state);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
            llMessageBody = itemView.findViewById(R.id.ll_message_body);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
        }

        void bind(final Message message) {
            setItemLayoutBackground();
            handleMessageText(message);
            handleMessageMedia(message);
            handleSeenUi(message.isSeen());
            sentAtTv.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(
                    mContext, message.getTimeStamp(), true));

            imageMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogZoom(Uri.parse(message.getMedia()));
                }
            });
        }

        void handleMessageText(Message message) {
            String messageText = "";
            if (message.getContent() == Content.TEXT) {
                messageText = message.getMessage();
            }

            String textMinimumLong = "This is a short message";
            int messageLength = messageText.length();
            if (messageLength <= textMinimumLong.length()) {
                messageTv.setVisibility(View.GONE);
                smallMessageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setText(messageText);
            } else {
                messageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setVisibility(View.GONE);
                messageTv.setText(messageText);
            }
        }

        void handleMessageMedia(final Message message) {
            if (message.getContent() == Content.IMAGE) {
                imageMessageIv.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(Uri.parse(message.getMedia()))
                        .placeholder(R.drawable.shape_image_place_holder_bg).into(imageMessageIv);
                if (message.isLoading()) {
                    messageStateIV.setVisibility(View.GONE);
                    sentAtTv.setVisibility(View.GONE);
                    loadingProgress.setVisibility(View.VISIBLE);
                } else {
                    messageStateIV.setVisibility(View.VISIBLE);
                    sentAtTv.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.GONE);
                }
            } else {
                imageMessageIv.setVisibility(View.GONE);
            }
        }

        void handleSeenUi(boolean isSeen) {
            if (isSeen)
                messageStateIV.setImageResource(R.drawable.ic_seen);
            else
                messageStateIV.setImageResource(R.drawable.ic_sent);
        }

        void setItemLayoutBackground() {
            int position = getAdapterPosition();
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
                llMessageBody.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.outgoing_second, null));

            } else {
                params.setMargins(3, 25, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.outgoing_first, null));
            }
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTv, smallMessageTv, sentAtTv;
        private ImageView imageMessageIv;
        private LinearLayout llMessageBody;
        private ProgressBar loadingProgress;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            smallMessageTv = itemView.findViewById(R.id.tv_small_message);
            sentAtTv = itemView.findViewById(R.id.tv_time);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
            llMessageBody = itemView.findViewById(R.id.ll_message_body);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
        }

        void bind(final Message message) {
            setItemLayoutBackground();
            handleMessageText(message);
            handleMediaMessage(message);
            sentAtTv.setText(DateTimeUtils.getDisplayableDateOfGivenTimeStamp(mContext, message.getTimeStamp(), true));
            //update message seen = true
            mListener.onUpdateComingMessageAsSeen(message.getMessageId());

            imageMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogZoom(Uri.parse(message.getMedia()));
                }
            });
        }

        void setItemLayoutBackground() {
            int position = getAdapterPosition();
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
                llMessageBody.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.receiving_second, null));

            } else {
                // If some other user sent the message
                params.setMargins(3, 25, 3, 8);
                llMessageBody.setLayoutParams(params);
                llMessageBody.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.receiving_first, null));
            }
        }

        void handleMessageText(Message message) {
            String messageText = "";
            if (message.getContent() == Content.TEXT) {
                messageText = message.getMessage();
            }

            String textMinimumLong = "This is a short message";
            int messageLength = messageText.length();
            if (messageLength <= textMinimumLong.length()) {
                messageTv.setVisibility(View.GONE);
                smallMessageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setText(messageText);
            } else {
                messageTv.setVisibility(View.VISIBLE);
                smallMessageTv.setVisibility(View.GONE);
                messageTv.setText(messageText);
            }
        }

        void handleMediaMessage(final Message message) {
            if (message.getContent() == Content.IMAGE) {
                imageMessageIv.setVisibility(View.VISIBLE);
                if (message.isLoading()) {
                    imageMessageIv.setEnabled(false);
                    Glide.with(mContext).load(R.drawable.shape_image_place_holder_bg).into(imageMessageIv);
                    sentAtTv.setVisibility(View.GONE);
                    loadingProgress.setVisibility(View.VISIBLE);
                } else {
                    Glide.with(mContext).load(Uri.parse(message.getMedia()))
                            .placeholder(R.drawable.shape_image_place_holder_bg).into(imageMessageIv);
                    imageMessageIv.setEnabled(true);
                    sentAtTv.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.GONE);
                }
            } else {
                loadingProgress.setVisibility(View.GONE);
                imageMessageIv.setVisibility(View.GONE);
            }
        }
    }

    public void addAllMessages(List<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    public void updateMessage(Message message) {
        for (int i = 0; i < mMessages.size(); i++) {
            if (mMessages.get(i).getMessageId().equals(message.getMessageId())) {
                mMessages.set(i, message);
                notifyDataSetChanged();
                break;
            }
        }
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
