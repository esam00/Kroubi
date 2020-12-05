package com.essam.chatapp.ui.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.essam.chatapp.R;
import com.essam.chatapp.models.User;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<User> mUsers;
    private ListItemClickListener listItemClickListener;
    public interface ListItemClickListener {
        void onClick(int index);
    }

    public ContactsAdapter(ListItemClickListener listener, List<User> users) {
        this.listItemClickListener = listener;
        this.mUsers = users;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView contactNameTV, statusTv;
        private ImageView profileIv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTV = itemView.findViewById(R.id.contact_name_tv);
            statusTv = itemView.findViewById(R.id.status_tv);
            profileIv = itemView.findViewById(R.id.profile_iv);
            itemView.setOnClickListener(this);

        }
        void bind (User user){
            contactNameTV.setText(user.getProfile().getUserName());
            statusTv.setText(user.getProfile().getStatus());

            Glide.with(itemView.getContext()).load(user.getProfile().getAvatar())
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(profileIv);
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
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        if(null== mUsers){
            return 0;
        }
        return mUsers.size();
    }
}
