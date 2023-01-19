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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context mContext;
    List<Post> mPosts;
    FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getImageUrl()).into(holder.postImage);
        holder.description.setText(post.getDescription());
        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext)
                        .load("https://www.fcbarcelona.com/photo-resources/2022/11/02/ffeb9318-5479-4648-9fd5-a7486db80937/mini_30-GAVI.png?width=670&height=790")
                        .into(holder.profileImage);
                holder.username.setText(post.getPublisher());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, like, comment, save, postImage;
        public TextView username, noOfLikes, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.img_post);
            profileImage = itemView.findViewById(R.id.img_profile);

            like = itemView.findViewById(R.id.btn_like);
            save = itemView.findViewById(R.id.btn_save);
            comment = itemView.findViewById(R.id.btn_comment);

            noOfLikes = itemView.findViewById(R.id.txt_likes);
            username = itemView.findViewById(R.id.txt_username);
            description = itemView.findViewById(R.id.description);
        }
    }
}
