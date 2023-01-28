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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.CommentActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context mContext;
    List<Post> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    User user;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(FirebaseAuth.getInstance().getUid());
        userReference.get().addOnSuccessListener(snapshot -> user = snapshot.toObject(User.class));
        ViewHolder.userReference = userReference;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);

        db.collection("Users").document(post.getCreator()).get().addOnSuccessListener(snapshot -> {
            user = snapshot.toObject(User.class);
            Glide.with(mContext).load(user.getProfileImageUrl()).into(holder.profileImage);
            holder.username.setText(user.getUsername());

//          db.collection("Posts").document(post.getPostid()).delete();

        });

        holder.caption.setText(post.getCaption());
        if (post.getKitt().isEmpty()) {
            Glide.with(mContext).load(post.getImageUrl()).into(holder.postImage);
        } else {
            holder.kitt.setVisibility(View.VISIBLE);
            holder.kitt.setText(post.getKitt());
        }
        holder.post = post;

        holder.checkIfLiked();
        holder.update();

        holder.like.setOnClickListener(v -> holder.likePost());
        holder.postImage.setOnClickListener(view -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                holder.likePost();
            }
            lastClickTime = clickTime;
        });

        DocumentReference postReference = db.collection("Posts").document(post.getPostid());
        userReference.get().addOnSuccessListener(documentSnapshot -> {
            holder.isSaved = false;
            user = documentSnapshot.toObject(User.class);
            for (DocumentReference dr : user.getSaved()) {
                if (dr.equals(postReference)) {
                    holder.isSaved = true;
                    break;
                }
            }
            holder.update();
        });
        holder.save.setOnClickListener(v -> updateIfSaved(holder, post));

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postid", post.getPostid());
            mContext.startActivity(intent);
        });
    }

    private void updateIfSaved(ViewHolder holder, Post post) {
        DocumentReference postReference = db.collection("Posts").document(post.getPostid());
        userReference.get().addOnSuccessListener(documentSnapshot -> {
            holder.isSaved = false;
            user = documentSnapshot.toObject(User.class);
            for (DocumentReference dr : user.getSaved()) {
                if (dr.equals(postReference)) {
                    holder.isSaved = true;
                    break;
                }
            }
            holder.isSaved = !holder.isSaved;
            if (holder.isSaved) {
                userReference.update("saved", FieldValue.arrayUnion(postReference));
                user.getSaved().add(postReference);
            } else {
                userReference.update("saved", FieldValue.arrayRemove(postReference));
                user.getSaved().remove(postReference);
            }
            holder.update();
        });
    }


    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, like, comment, save, postImage;
        public TextView username, noOfLikes, caption, kitt;
        public static DocumentReference userReference;
        protected boolean isLiked, isSaved;
        public Post post;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.img_post);
            profileImage = itemView.findViewById(R.id.img_profile);

            like = itemView.findViewById(R.id.btn_like);
            save = itemView.findViewById(R.id.btn_save);
            comment = itemView.findViewById(R.id.btn_comment);

            noOfLikes = itemView.findViewById(R.id.txt_likes);
            username = itemView.findViewById(R.id.txt_username);
            caption = itemView.findViewById(R.id.description);
            kitt = itemView.findViewById(R.id.txt_kitt);
        }

        void update() {
            if (isLiked)
                like.setImageResource(R.drawable.ic_heart);
            else
                like.setImageResource(R.drawable.ic_heart_outlined);
            noOfLikes.setText(post.getLikes().size() + " likes");

            if (isSaved)
                save.setImageResource(R.drawable.ic_bookmark);
            else
                save.setImageResource(R.drawable.ic_bookmark_outlined);
        }

        public void likePost() {
            checkIfLiked();
            isLiked = !isLiked;
            updateLikesData();
            update();
        }

        private void checkIfLiked() {
            isLiked = false;
            for (DocumentReference s : post.getLikes()) {
                if (s.equals(userReference)) {
                    isLiked = true;
                    break;
                }
            }
        }

        void updateLikesData() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            if (isLiked) {
                postReference.update("likes", FieldValue.arrayUnion(userReference));
                post.getLikes().add(userReference);
            } else {
                postReference.update("likes", FieldValue.arrayRemove(userReference));
                post.getLikes().remove(userReference);
            }
        }
    }
}
