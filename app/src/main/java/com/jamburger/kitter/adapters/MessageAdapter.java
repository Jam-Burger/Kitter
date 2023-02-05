package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.jamburger.kitter.MainActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    static String myUID;
    Context mContext;
    List<Message> messages;

    public MessageAdapter(Context mContext, List<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
        myUID = FirebaseAuth.getInstance().getUid();
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
        holder.message.setText(message.getText());
        holder.time.setText(MainActivity.dateIdToTime(message.getMessageId()));
        if (myUID.equals(message.getSenderId())) {
            holder.container.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            holder.container.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.txt_message);
            time = itemView.findViewById(R.id.txt_time);
            container = itemView.findViewById(R.id.container);
        }
    }
}
