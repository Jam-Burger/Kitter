package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;
import com.squareup.picasso.Picasso;

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
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageUrl()).into(holder.postImage);
        holder.description.setText(post.getDescription());
        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
//                Picasso.get().load(user.getImageUrl()).into(holder.profileImage);
//                holder.username.setText(user.getUsername());
                Picasso.get().load("https://i.pinimg.com/736x/0f/bc/f5/0fbcf59b833f7420ab09b331715d6036.jpg").into(holder.profileImage);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, like, comment, save, postImage;
        public TextView username, noOfLikes, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.img_profile);
            postImage = itemView.findViewById(R.id.img_post);

            like = itemView.findViewById(R.id.btn_like);
            comment = itemView.findViewById(R.id.btn_comment);
            save = itemView.findViewById(R.id.btn_save);

            username = itemView.findViewById(R.id.txt_username);
            noOfLikes = itemView.findViewById(R.id.txt_likes);
            description = itemView.findViewById(R.id.description);
        }
    }
}
