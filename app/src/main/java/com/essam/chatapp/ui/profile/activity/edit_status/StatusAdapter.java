package com.essam.chatapp.ui.profile.activity.edit_status;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.essam.chatapp.R;
import com.essam.chatapp.models.Status;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private List<Status> statusLis;
    private OnClickListener mListener;
    private Context mContext;

    public StatusAdapter(List<Status> statusLis, OnClickListener listener) {
        this.statusLis = statusLis;
        mListener = listener;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new StatusViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_status,parent,false)) ;
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        holder.bind(statusLis.get(position));
    }

    @Override
    public int getItemCount() {
        return statusLis.size();
    }

    interface OnClickListener{
        void onItemClick(int selectedIndex);
    }

    class StatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView statusTv;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            statusTv = itemView.findViewById(R.id.statusTv);

            itemView.setOnClickListener(this);
        }

        void bind(Status status){
            statusTv.setText(status.getStatus());
            if (status.isCurrent()){
                statusTv.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            }else {
                statusTv.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            }
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(getAdapterPosition());
        }
    }
}
