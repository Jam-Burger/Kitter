package com.jamburger.kitter.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.List;

public class SavedPostsActivity extends AppCompatActivity {
    RecyclerView savedPostsRecyclerview;
    ImageView closeButton;
    PostAdapter postAdapter;
    List<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        savedPostsRecyclerview = findViewById(R.id.recyclerview_saved_posts);
        closeButton = findViewById(R.id.btn_close);

        closeButton.setOnClickListener(view -> {
            finish();
        });

        savedPostsRecyclerview.setHasFixedSize(true);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(this, posts);
        savedPostsRecyclerview.setAdapter(postAdapter);

        readPosts();
    }

    void readPosts() {
        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + FirebaseAuth.getInstance().getUid());
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            List<Post> myPosts = new ArrayList<>();
            if (user.getSaved().size() == 0) return;
            for (DocumentReference documentReference : user.getSaved()) {
                documentReference.get().addOnSuccessListener(documentSnapshot -> {
                    myPosts.add(documentSnapshot.toObject(Post.class));
                    if (myPosts.size() == user.getSaved().size()) {
                        FirebaseFirestore.getInstance().collection("Posts").get().addOnSuccessListener(postSnapshots -> {
                            posts.clear();
                            for (DocumentSnapshot postSnapshot : postSnapshots) {
                                Post post = postSnapshot.toObject(Post.class);
                                for (Post current : myPosts) {
                                    if (!current.getPostid().equals(post.getPostid()))
                                        continue;
                                    posts.add(0, post);
                                    break;
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                        });
                    }
                });
            }
        });
    }
}