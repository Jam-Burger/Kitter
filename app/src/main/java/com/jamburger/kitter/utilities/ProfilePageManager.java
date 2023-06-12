package com.jamburger.kitter.utilities;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

public class ProfilePageManager {
    public TextView noOfFollowers;
    public ImageView backgroundImage;
    MyPictureAdapter myPictureAdapter;
    MyKittAdapter myKittAdapter;
    ImageView profileImage;
    TextView name, username, bio, noOfPosts, noOfFollowing;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    GridLayoutManager layoutManager;
    View picturesIndicator, kittsIndicator;
    Context context;

    public ProfilePageManager(View view) {
        backgroundImage = view.findViewById(R.id.img_background);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);

        noOfPosts = view.findViewById(R.id.txt_post_count);
        noOfFollowers = view.findViewById(R.id.txt_followers_count);
        noOfFollowing = view.findViewById(R.id.txt_following_count);

        picturesButton = view.findViewById(R.id.btn_my_pictures);
        kittsButton = view.findViewById(R.id.btn_my_kitts);

        recyclerViewMyPosts = view.findViewById(R.id.recyclerview_my_posts);

        picturesIndicator = view.findViewById(R.id.indicator_pictures);
        kittsIndicator = view.findViewById(R.id.indicator_kitts);

        context = view.getContext();
        handlePostsArea();
    }

    private void handlePostsArea() {

        myPictureAdapter = new MyPictureAdapter(context);
        myKittAdapter = new MyKittAdapter(context);

        layoutManager = new GridLayoutManager(context, 3);

        recyclerViewMyPosts.setHasFixedSize(true);
        recyclerViewMyPosts.setAdapter(myPictureAdapter);
        recyclerViewMyPosts.setLayoutManager(layoutManager);

        picturesButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myPictureAdapter);
            layoutManager.setSpanCount(3);
            picturesIndicator.setVisibility(View.VISIBLE);
            kittsIndicator.setVisibility(View.INVISIBLE);
        });
        kittsButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myKittAdapter);
            layoutManager.setSpanCount(1);
            picturesIndicator.setVisibility(View.INVISIBLE);
            kittsIndicator.setVisibility(View.VISIBLE);
        });
    }

    public void readPosts(DocumentReference userReference) {
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            myPictureAdapter.clearPosts();
            myKittAdapter.clearPosts();
            for (DocumentReference postReference : user.getPosts()) {
                postReference.get().addOnSuccessListener(postSnapshot -> {
                    Post post = postSnapshot.toObject(Post.class);
                    assert post != null;
                    if (!post.getImageUrl().isEmpty()) {
                        myPictureAdapter.addPost(postReference);
                    }
                    if (!post.getKitt().isEmpty()) {
                        myKittAdapter.addPost(postReference);
                    }
                });
            }
        });
    }

    public void fillUserData(User user) {
        String txt_username = "@" + user.getUsername();

        username.setText(txt_username);
        name.setText(user.getName());
        bio.setText(user.getBio());

        noOfPosts.setText(String.valueOf(user.getPosts().size()));
        noOfFollowers.setText(String.valueOf(user.getFollowers().size()));
        noOfFollowing.setText(String.valueOf(user.getFollowing().size()));


        Glide.with(context).load(user.getProfileImageUrl()).into(profileImage);
        Glide.with(context).load(user.getBackgroundImageUrl()).into(backgroundImage);
    }
}
