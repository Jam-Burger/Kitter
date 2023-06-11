package com.jamburger.kitter.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

public class SavedPostsActivity extends AppCompatActivity {
    RecyclerView savedPostsRecyclerview;
    ImageView closeButton;
    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        savedPostsRecyclerview = findViewById(R.id.recyclerview_saved_posts);
        closeButton = findViewById(R.id.btn_close);

        closeButton.setOnClickListener(view -> finish());

        savedPostsRecyclerview.setHasFixedSize(true);

        postAdapter = new PostAdapter(this);
        savedPostsRecyclerview.setAdapter(postAdapter);

        readPosts();
    }

    void readPosts() {
        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + FirebaseAuth.getInstance().getUid());
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            for (DocumentReference savedPostReference : user.getSaved()) {
                savedPostReference.get().addOnSuccessListener(savedPostSnapshot -> postAdapter.addPost(savedPostSnapshot.toObject(Post.class)));
            }
        });
    }
}