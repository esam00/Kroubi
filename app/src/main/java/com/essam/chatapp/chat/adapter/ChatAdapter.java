package com.essam.chatapp.chat.adapter;


/*
  Created by esammosbah1@gmail.com on 01/10/19.
 */
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.essam.chatapp.R;
import com.essam.chatapp.chat.model.Message;
import com.essam.chatapp.utils.Consts;
import com.essam.chatapp.utils.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    private List<Message> mMessages;
    private Context context;
    private DatabaseReference chatDb, msgDb;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_SIMPLE_MESSAGE_RECEIVED = 3;
    private static final int VIEW_TYPE_SIMPLE_MESSAGE_SENT = 4;


    public ChatAdapter(Context context) {
        this.context = context;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = mMessages.get(position);
        boolean isReceiving = !message.getCreatorId().equals(FirebaseAuth.getInstance().getUid());
        Log.i("TAG", "getItemViewType: "+isReceiving);
        Message prevMessage= null;

        if(position>0){
            prevMessage = mMessages.get(position - 1);
        }

        if (isReceiving) {
            if (prevMessage != null) {
                return VIEW_TYPE_SIMPLE_MESSAGE_RECEIVED;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        } else {
            if (prevMessage!=null) {
                return VIEW_TYPE_SIMPLE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_SENT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = inflater.inflate(R.layout.sender_list_item, parent, false);
            return new SenderViewHolder(view);

        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = inflater.inflate(R.layout.receiver_list_item, parent, false);
            return new ReceiverViewHolder(view);

        } else if (viewType == VIEW_TYPE_SIMPLE_MESSAGE_RECEIVED) {
            view = inflater.inflate(R.layout.simple_receiver_list_item, parent, false);
            return new ReceiverViewHolder(view);

        } else {
            view = inflater.inflate(R.layout.simple_sender_list_item, parent, false);
            return new SenderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
            case VIEW_TYPE_SIMPLE_MESSAGE_SENT:
                ((SenderViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
            case VIEW_TYPE_SIMPLE_MESSAGE_RECEIVED:
                ((ReceiverViewHolder) holder).bind(message);
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
        private TextView sentAtTextView;
        private ImageView messageStateIV;
        private ImageView imageMessageIv;

        SenderViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            sentAtTextView = itemView.findViewById(R.id.tv_time);
            messageStateIV = itemView.findViewById(R.id.iv_message_state);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
        }

        void bind(final Message message) {
            //bind message sent time
            sentAtTextView.setText(message.getCreatedAt().split("\\s+")[1] + " " + message.getCreatedAt().split("\\s+")[2]);

            //bind seen
            if (message.isSeen())
                messageStateIV.setImageResource(R.drawable.ic_seen);
            else
                messageStateIV.setImageResource(R.drawable.ic_sent);

            //bind message text
            if(message.getMessage()!=null && !message.getMessage().isEmpty()){
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(message.getMessage());
            }else {
                messageTextView.setVisibility(View.GONE);
            }

            //bind message image
            if (message.getMedia() != null && !message.getMedia().isEmpty()) {
                imageMessageIv.setVisibility(View.VISIBLE);
                Glide.with(context).load(Uri.parse(message.getMedia())).into(imageMessageIv);
                imageMessageIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogZoom(Uri.parse(message.getMedia()));
                    }
                });
            }else {
                imageMessageIv.setVisibility(View.GONE);
            }
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView;
        private TextView sentAtTextView;
        private ImageView imageMessageIv;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            sentAtTextView = itemView.findViewById(R.id.tv_time);
            imageMessageIv = itemView.findViewById(R.id.image_message_iv);
        }

        void bind(final Message message) {
            //bind message time
            sentAtTextView.setText(message.getCreatedAt().split("\\s+")[1]+" "+message.getCreatedAt().split("\\s+")[2]);

            //update message seen = true
            msgDb = chatDb.child(message.getMessageId());
            msgDb.child(Consts.SEEN).setValue(true);

            //bind message text
            if(message.getMessage()!=null && !message.getMessage().isEmpty()){
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(message.getMessage());
            }else {
                messageTextView.setVisibility(View.GONE);
            }

            //bind message image
            if (message.getMedia() != null && !message.getMedia().isEmpty()) {
                imageMessageIv.setVisibility(View.VISIBLE);
                Glide.with(context).load(Uri.parse(message.getMedia())).into(imageMessageIv);
                imageMessageIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogZoom(Uri.parse(message.getMedia()));
                    }
                });
            }else {
                imageMessageIv.setVisibility(View.GONE);
            }
        }

    }

    public void setMessagesData(List<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    /**
     * get the reference to this chat id chat database >> chat/chatId
     * @param mChatDb >> chat/chatId
     */
    public void setChatDp(DatabaseReference mChatDb) {
        chatDb = mChatDb;
        notifyDataSetChanged();
    }

    public void updateSeen (int index){
        mMessages.get(index).setSeen(true);
        mMessages.get(mMessages.size()-1).setSeen(true);
        notifyDataSetChanged();
    }

    Dialog zoomDialog;
    ImageView ZoomDialogIv;
    private void dialogZoom(Uri imgUrl){
        zoomDialog = new Dialog(context);
        zoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zoomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        zoomDialog.setContentView(R.layout.dialog_image_zoom);

        ZoomDialogIv = zoomDialog.findViewById(R.id.ZoomDialogIv);
        Glide.with(context).
                load(imgUrl)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ZoomDialogIv);
        zoomDialog.show();
        Window window = zoomDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
