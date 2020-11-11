package com.essam.chatapp.ui.intro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.essam.chatapp.R;

import java.util.List;

public class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder> {

    private List<OnBoardingItem> mOnBoardingItems;
    private Context mContext;

    public OnBoardingAdapter(List<OnBoardingItem> onBoardingItems) {
        mOnBoardingItems = onBoardingItems;
    }

    @NonNull
    @Override
    public OnBoardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new OnBoardingViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OnBoardingViewHolder holder, int position) {
        holder.bindOnBoardingData(mOnBoardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mOnBoardingItems.size();
    }

    class OnBoardingViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleTv;
        private TextView descriptionTv;

        public OnBoardingViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.on_boarding_iv);
            titleTv = itemView.findViewById(R.id.title);
            descriptionTv = itemView.findViewById(R.id.description);
        }

        void bindOnBoardingData(OnBoardingItem item) {
            imageView.setImageResource(item.getImageSrcId());
            titleTv.setText(mContext.getResources().getText(item.getTitleSrcId()));
            descriptionTv.setText(mContext.getResources().getText(item.getDescriptionSrcId()));
        }
    }
}
