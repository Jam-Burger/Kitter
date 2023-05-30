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

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.CommentsActivity;
import com.jamburger.kitter.MainActivity;
import com.jamburger.kitter.OtherProfileActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.backend.NotificationManager;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context mContext;
    List<Post> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    User user;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds

    long lastClickTime = 0;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_post, parent, false);
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
        });

        holder.post = post;
        holder.time.setText(MainActivity.dateIdToString(post.getPostid()));
        Glide.with(mContext).load(post.getImageUrl()).into(holder.postImage);
        holder.caption.setText(post.getCaption());
        holder.kitt.setText(post.getKitt());

        if (post.getKitt().isEmpty()) {
            holder.kitt.setVisibility(View.GONE);
            holder.caption.setVisibility(View.VISIBLE);
        } else {
            holder.kitt.setVisibility(View.VISIBLE);
            holder.caption.setVisibility(View.GONE);
        }

        holder.checkIfLiked();

        holder.like.setOnClickListener(v -> holder.likePost(user));
        holder.postImage.setOnClickListener(view -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                holder.likePost(user);
            }
            lastClickTime = clickTime;
        });

        holder.checkIfSaved();
        holder.save.setOnClickListener(v -> holder.savePost());

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            mContext.startActivity(intent);
        });
        holder.header.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, OtherProfileActivity.class);
            intent.putExtra("userid", post.getCreator());
            mContext.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, like, comment, save, postImage;
        public TextView username, noOfLikes, caption, kitt, time;
        public View header;
        LottieAnimationView likeAnimation;
        public static DocumentReference userReference;
        protected boolean isLiked, isSaved;
        public Post post;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.img_post);
            profileImage = itemView.findViewById(R.id.img_profile);
            time = itemView.findViewById(R.id.txt_time);
            header = itemView.findViewById(R.id.header);

            like = itemView.findViewById(R.id.btn_like);
            likeAnimation = itemView.findViewById(R.id.animation_like);
            save = itemView.findViewById(R.id.btn_save);
            comment = itemView.findViewById(R.id.btn_comment);

            noOfLikes = itemView.findViewById(R.id.txt_likes);
            username = itemView.findViewById(R.id.txt_username);
            caption = itemView.findViewById(R.id.caption);
            kitt = itemView.findViewById(R.id.txt_kitt);
        }

        void update() {
            if (isLiked) like.setImageResource(R.drawable.ic_heart);
            else {
                like.setImageResource(R.drawable.ic_heart_outlined);
                likeAnimation.setVisibility(View.INVISIBLE);
            }
            int n = post.getLikes().size();
            if (n > 0) {
                noOfLikes.setVisibility(View.VISIBLE);
                String str = n + (n > 1 ? " likes" : " like");
                noOfLikes.setText(str);
            } else {
                noOfLikes.setVisibility(View.GONE);
            }
            if (isSaved) save.setImageResource(R.drawable.ic_save);
            else save.setImageResource(R.drawable.ic_save_outlined);
        }

        public void likePost(User user) {
            isLiked = !isLiked;
            if (isLiked) {
                likeAnimation.setVisibility(View.VISIBLE);
                likeAnimation.playAnimation();
                NotificationManager.sendNotification(post.getCreator(), "Your post liked by " + user.getName());
            } else {
                likeAnimation.setVisibility(View.INVISIBLE);
            }
            updateLikesData();
        }

        public void savePost() {
            isSaved = !isSaved;
            updateSavedData();
        }


        private void checkIfLiked() {
            isLiked = false;
            for (DocumentReference s : post.getLikes()) {
                if (s.equals(userReference)) {
                    isLiked = true;
                    break;
                }
            }
            update();
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
            update();
        }

        public void checkIfSaved() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            userReference.get().addOnSuccessListener(documentSnapshot -> {
                isSaved = false;
                User user = documentSnapshot.toObject(User.class);
                assert user != null;
                for (DocumentReference dr : user.getSaved()) {
                    if (dr.equals(postReference)) {
                        isSaved = true;
                        break;
                    }
                }
            });
            update();
        }

        private void updateSavedData() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            if (isSaved) {
                userReference.update("saved", FieldValue.arrayUnion(postReference));
            } else {
                userReference.update("saved", FieldValue.arrayRemove(postReference));
            }
            update();
        }
    }
}
