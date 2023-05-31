package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;

import java.util.List;

public class MyPictureAdapter extends RecyclerView.Adapter<MyPictureAdapter.ViewHolder> {
    Context mContext;
    List<DocumentReference> mPosts;

    public MyPictureAdapter(Context mContext, List<DocumentReference> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_picture, parent, false);
        return new MyPictureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentReference postReference = mPosts.get(position);
        postReference.get().addOnSuccessListener(postSnapshot -> {
            Post post = postSnapshot.toObject(Post.class);
            if (!post.getImageUrl().isEmpty()) {
                Glide.with(mContext).load(post.getImageUrl()).into(holder.myPostImage);
                holder.container.setVisibility(View.VISIBLE);
            } else {
                mPosts.remove(postReference);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        ImageView myPostImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myPostImage = itemView.findViewById(R.id.img_mypost);
            container = itemView.findViewById(R.id.container_my_picture);
            container.setVisibility(View.GONE);
        }
    }
}
