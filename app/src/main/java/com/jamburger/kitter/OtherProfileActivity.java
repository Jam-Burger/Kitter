package com.jamburger.kitter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.List;

public class OtherProfileActivity extends AppCompatActivity {
    public MyPictureAdapter myPictureAdapter;
    public MyKittAdapter myKittAdapter;
    ImageView backgroundImage, profileImage;
    TextView name, username, bio;
    Toolbar toolbar;
    DocumentReference userdata;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    List<Post> pictures;
    List<Post> kitts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        String uid = getIntent().getStringExtra("userid");
        userdata = FirebaseFirestore.getInstance().collection("Users").document(uid);

        backgroundImage = findViewById(R.id.img_background);
        profileImage = findViewById(R.id.img_profile);
        name = findViewById(R.id.txt_name);
        username = findViewById(R.id.txt_username);
        bio = findViewById(R.id.txt_bio);

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

        fillUserData();
        readPosts();

        View picturesIndicator = findViewById(R.id.indicator_pictures);
        View kittsIndicator = findViewById(R.id.indicator_kitts);
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

    void fillUserData() {
        userdata.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            assert user != null;
            String txt_username = "@" + user.getUsername();
            username.setText(txt_username);
            name.setText(user.getName());
            bio.setText(user.getBio());
            try {
                Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
                Glide.with(this).load(user.getBackgroundImageUrl()).into(backgroundImage);
            } catch (Exception ignored) {

            }
        });
    }

    void readPosts() {
        userdata.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            List<Post> myPosts = new ArrayList<>();
            if (user.getPosts().size() == 0) return;
            for (DocumentReference documentReference : user.getPosts()) {
                documentReference.get().addOnSuccessListener(documentSnapshot -> {
                    myPosts.add(documentSnapshot.toObject(Post.class));
                    if (myPosts.size() == user.getPosts().size()) {
                        FirebaseFirestore.getInstance().collection("Posts").get().addOnSuccessListener(postSnapshots -> {
                            pictures.clear();
                            kitts.clear();
                            for (DocumentSnapshot postSnapshot : postSnapshots) {
                                Post post = postSnapshot.toObject(Post.class);
                                for (Post current : myPosts) {
                                    if (current.getPostid().equals(post.getPostid())) {
                                        if (post.getKitt().isEmpty()) {
                                            pictures.add(0, post);
                                        } else {
                                            kitts.add(0, post);
                                        }
                                    }
                                }
                            }
                            myKittAdapter.notifyDataSetChanged();
                            myPictureAdapter.notifyDataSetChanged();
                        });
                    }
                });
            }
        });
    }
}