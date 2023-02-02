package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;

import java.util.List;

public class MyPictureAdapter extends RecyclerView.Adapter<MyPictureAdapter.ViewHolder> {
    Context mContext;
    List<Post> mPosts;

    public MyPictureAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_picture_item, parent, false);
        return new MyPictureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getImageUrl()).into(holder.myPostImage);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myPostImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myPostImage = itemView.findViewById(R.id.img_mypost);
        }
    }
}
