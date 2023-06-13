package com.jamburger.kitter.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.CommentsActivity;
import com.jamburger.kitter.activities.OtherProfileActivity;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;
import com.jamburger.kitter.utilities.DateFormatter;

import java.util.Comparator;
import java.util.TreeSet;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context mContext;
    TreeSet<Post> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds

    long lastClickTime = 0;

    public PostAdapter(Context mContext) {
        this.mContext = mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = new TreeSet<>(Comparator.comparing(Post::getPostid));
        }
    }

    public void addPost(Post post) {
        mPosts.add(post);
        notifyDataSetChanged();
    }

    public void clearPosts() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_post, parent, false);
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(FirebaseAuth.getInstance().getUid());
        ViewHolder.userReference = userReference;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.toArray(new Post[0])[position];

        db.collection("Users").document(post.getCreator()).get().addOnSuccessListener(snapshot -> {
            User creator = snapshot.toObject(User.class);
            Glide.with(mContext).load(creator.getProfileImageUrl()).into(holder.profileImage);
            holder.username.setText(creator.getUsername());
        });

        holder.post = post;
        holder.time.setText(DateFormatter.getTimeDifference(post.getPostid()));
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
        holder.checkIfSaved();

        DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference().child("comments").child(post.getPostid());
        commentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                if (commentCount >= 2) {
                    String commentsCountText = "View all " + commentCount + " comments";
                    holder.commentCount.setText(commentsCountText);
                    holder.commentCount.setVisibility(View.VISIBLE);
                } else {
                    holder.commentCount.setText("");
                    holder.commentCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.postImage.setOnClickListener(view -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                holder.toggleLike();
            }
            lastClickTime = clickTime;
        });
        holder.like.setOnClickListener(v -> holder.toggleLike());
        holder.save.setOnClickListener(v -> holder.toggleSave());

        holder.commentCount.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("openKeyboard", false);
            mContext.startActivity(intent);
        });

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("openKeyboard", true);
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
        public View header;
        LottieAnimationView likeAnimation;
        public static DocumentReference userReference;
        public TextView username, noOfLikes, caption, kitt, commentCount, time;
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
            commentCount = itemView.findViewById(R.id.txt_comment_count);
            kitt = itemView.findViewById(R.id.txt_kitt);

            likeAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(@NonNull Animator animation, boolean isReverse) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    likeAnimation.setVisibility(View.INVISIBLE);
                }
            });
        }

        void update() {
            int n = post.getLikes().size();
            if (n > 0) {
                noOfLikes.setVisibility(View.VISIBLE);
                String str = n + (n > 1 ? " likes" : " like");
                noOfLikes.setText(str);
            } else {
                noOfLikes.setVisibility(View.GONE);
            }

            if (isLiked) like.setImageResource(R.drawable.ic_heart);
            else like.setImageResource(R.drawable.ic_heart_outlined);

            if (isSaved) save.setImageResource(R.drawable.ic_save);
            else save.setImageResource(R.drawable.ic_save_outlined);
        }

        public void toggleLike() {
            isLiked = !isLiked;
            if (isLiked) {
                likeAnimation.playAnimation();
                likeAnimation.setVisibility(View.VISIBLE);
//                NotificationManager.sendNotification(post.getCreator(), "Your post liked by " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            } else {
                likeAnimation.setVisibility(View.INVISIBLE);
            }
            updateLikesData();
        }

        public void toggleSave() {
            isSaved = !isSaved;
            updateSavedData();
        }


        private void checkIfLiked() {
            isLiked = post.getLikes().contains(userReference);
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
                User user = documentSnapshot.toObject(User.class);
                assert user != null;
                isSaved = user.getSaved().contains(postReference);
                update();
            });
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
