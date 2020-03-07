package com.essam.chatapp.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.essam.chatapp.R;
import com.essam.chatapp.contacts.model.Contact;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<Contact> mContacts;

    private ListItemClickListener listItemClickListener;

    public interface ListItemClickListener {
        void onClick(int index);
    }

    public ContactsAdapter(ListItemClickListener listener, List<Contact> contacts) {
        this.listItemClickListener = listener;
        this.mContacts= contacts;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView contactNameTV, statusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTV = itemView.findViewById(R.id.tv_contact_name);
            statusTv = itemView.findViewById(R.id.tv_status);
            itemView.setOnClickListener(this);

        }
        void bind (Contact contact){
            contactNameTV.setText(contact.getName());
            statusTv.setText(contact.getPhone());
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
                .inflate(R.layout.contacts_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = mContacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        if(null== mContacts){
            return 0;
        }
        return mContacts.size();
    }

    public void setContactsData(List<Contact> contacts) {
        mContacts = contacts;
        notifyDataSetChanged();
    }

}
