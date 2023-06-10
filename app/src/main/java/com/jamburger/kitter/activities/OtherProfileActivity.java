package com.jamburger.kitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherProfileActivity extends AppCompatActivity {
    MyPictureAdapter myPictureAdapter;
    MyKittAdapter myKittAdapter;
    ImageView backgroundImage, profileImage;
    TextView name, username, bio, noOfPosts, noOfFollowers, noOfFollowing;
    Button followButton, messageButton;
    DocumentReference userdataReference, myUserdataReference;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    List<DocumentReference> pictures;
    List<DocumentReference> kitts;
    boolean amFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        String userid = getIntent().getStringExtra("userid");
        userdataReference = FirebaseFirestore.getInstance().collection("Users").document(userid);
        myUserdataReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());

        backgroundImage = findViewById(R.id.img_background);
        profileImage = findViewById(R.id.img_profile);
        name = findViewById(R.id.txt_name);
        username = findViewById(R.id.txt_username);
        bio = findViewById(R.id.txt_bio);

        noOfPosts = findViewById(R.id.txt_post_count);
        noOfFollowers = findViewById(R.id.txt_followers_count);
        noOfFollowing = findViewById(R.id.txt_following_count);

        followButton = findViewById(R.id.btn_follow);
        messageButton = findViewById(R.id.btn_message);
        picturesButton = findViewById(R.id.btn_my_pictures);
        kittsButton = findViewById(R.id.btn_my_kitts);

        pictures = new ArrayList<>();
        myPictureAdapter = new MyPictureAdapter(this, pictures);

        kitts = new ArrayList<>();
        myKittAdapter = new MyKittAdapter(this, kitts);

        recyclerViewMyPosts = findViewById(R.id.recyclerview_my_posts);
        recyclerViewMyPosts.setHasFixedSize(true);
        ((GridLayoutManager) recyclerViewMyPosts.getLayoutManager()).setSpanCount(3);
        recyclerViewMyPosts.setAdapter(myPictureAdapter);

        if (myUserdataReference.equals(userdataReference)) {
            followButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.GONE);
        }

        fillUserData();
        readPosts();

        View picturesIndicator = findViewById(R.id.indicator_pictures);
        View kittsIndicator = findViewById(R.id.indicator_kitts);

        followButton.setOnClickListener(v -> toggleFollow());
        messageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OtherProfileActivity.class);
            intent.putExtra("userid", userid);
            startActivity(intent);
        });
        picturesButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myPictureAdapter);
            ((GridLayoutManager) recyclerViewMyPosts.getLayoutManager()).setSpanCount(3);
            picturesIndicator.setVisibility(View.VISIBLE);
            kittsIndicator.setVisibility(View.INVISIBLE);
        });
        kittsButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myKittAdapter);
            ((GridLayoutManager) recyclerViewMyPosts.getLayoutManager()).setSpanCount(1);
            picturesIndicator.setVisibility(View.INVISIBLE);
            kittsIndicator.setVisibility(View.VISIBLE);
        });
    }

    private void toggleFollow() {
        amFollowing = !amFollowing;
        if (amFollowing) {
            myUserdataReference.update("following", FieldValue.arrayUnion(userdataReference));
            userdataReference.update("followers", FieldValue.arrayUnion(myUserdataReference));
            noOfFollowers.setText(String.valueOf(Integer.parseInt(noOfFollowers.getText().toString()) + 1));

            List<DocumentReference> posts = new ArrayList<>();
            posts.addAll(pictures);
            posts.addAll(kitts);
            for (DocumentReference postReference : posts) {
                Map<String, Object> map = new HashMap<>();
                map.put("postReference", postReference);
                map.put("visited", false);
                myUserdataReference.collection("feed").document(postReference.getId()).set(map);
            }
        } else {
            myUserdataReference.update("following", FieldValue.arrayRemove(userdataReference));
            userdataReference.update("followers", FieldValue.arrayRemove(myUserdataReference));
            noOfFollowers.setText(String.valueOf(Integer.parseInt(noOfFollowers.getText().toString()) - 1));

            List<DocumentReference> posts = new ArrayList<>();
            posts.addAll(pictures);
            posts.addAll(kitts);
            for (DocumentReference postReference : posts) {
                myUserdataReference.collection("feed").document(postReference.getId()).delete();
            }
        }
        updateFollowButton();
    }


    void updateFollowButton() {
        if (amFollowing) {
            followButton.setText("Following");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
        } else {
            followButton.setText("Follow");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
        }
    }

    void fillUserData() {
        userdataReference.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            assert user != null;
            String txt_username = "@" + user.getUsername();

            username.setText(txt_username);
            name.setText(user.getName());
            bio.setText(user.getBio());

            noOfPosts.setText(String.valueOf(user.getPosts().size()));
            noOfFollowers.setText(String.valueOf(user.getFollowers().size()));
            noOfFollowing.setText(String.valueOf(user.getFollowing().size()));

            amFollowing = user.getFollowers().contains(myUserdataReference);
            updateFollowButton();

            Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
            Glide.with(this).load(user.getBackgroundImageUrl()).into(backgroundImage);
        });
    }

    void readPosts() {
        userdataReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            pictures.clear();
            kitts.clear();
            for (DocumentReference postReference : user.getPosts()) {
                postReference.get().addOnSuccessListener(postSnapshot -> {
                    Post post = postSnapshot.toObject(Post.class);
                    assert post != null;
                    if (!post.getImageUrl().isEmpty()) {
                        pictures.add(0, postReference);
                        myPictureAdapter.notifyDataSetChanged();
                    }
                    if (!post.getKitt().isEmpty()) {
                        kitts.add(0, postReference);
                        myKittAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}