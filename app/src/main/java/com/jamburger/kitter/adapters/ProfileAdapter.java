package com.jamburger.kitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jamburger.kitter.ChatActivity;
import com.jamburger.kitter.OtherProfileActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.User;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    Context mContext;
    List<User> mUsers;
    String use;

    public ProfileAdapter(Context mContext, List<User> mUsers, String use) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.use = use;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_item, parent, false);
        return new ProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        Glide.with(mContext).load(user.getProfileImageUrl()).into(holder.profileImage);
        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());
        holder.container.setOnClickListener(view -> {
            Intent intent;
            if (use.equals("PROFILE")) {
                intent = new Intent(mContext, OtherProfileActivity.class);
            } else {
                intent = new Intent(mContext, ChatActivity.class);
            }
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void filterList(List<User> filteredList) {
        mUsers = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, name;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            profileImage = itemView.findViewById(R.id.img_profile);
            username = itemView.findViewById(R.id.txt_username);
            name = itemView.findViewById(R.id.txt_name);
        }
    }
}
