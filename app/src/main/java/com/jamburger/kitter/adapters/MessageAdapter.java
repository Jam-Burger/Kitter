package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.jamburger.kitter.MainActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    String fellowProfileImageUrl;
    String myUID;
    Context mContext;
    List<Message> messages;


    public MessageAdapter(Context mContext, List<Message> messages, String fellowProfileImageUrl) {
        this.mContext = mContext;
        this.messages = messages;
        myUID = FirebaseAuth.getInstance().getUid();
        this.fellowProfileImageUrl = fellowProfileImageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        Message nextMessage = position + 1 < messages.size() ? messages.get(position + 1) : null;

        holder.message.setText(message.getText());
        holder.time.setText(MainActivity.dateIdToTime(message.getMessageId()));
        if (myUID.equals(message.getSenderId())) {
            holder.container.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            holder.profileImage.setVisibility(View.GONE);
        } else {
            holder.container.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            if (nextMessage == null || nextMessage.getSenderId().equals(myUID)) {
                holder.profileImage.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(fellowProfileImageUrl).into(holder.profileImage);
            } else {
                holder.profileImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView profileImage;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.txt_message);
            time = itemView.findViewById(R.id.txt_time);
            profileImage = itemView.findViewById(R.id.img_profile);
            container = itemView.findViewById(R.id.container);
        }
    }
}
