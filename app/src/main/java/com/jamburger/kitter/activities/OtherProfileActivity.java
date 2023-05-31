package com.jamburger.kitter.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OtherProfileActivity extends AppCompatActivity {
    MyPictureAdapter myPictureAdapter;
    MyKittAdapter myKittAdapter;
    ImageView backgroundImage, profileImage;
    TextView name, username, bio;
    DocumentReference userdata;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    List<DocumentReference> pictures;
    List<DocumentReference> kitts;

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
            Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
            Glide.with(this).load(user.getBackgroundImageUrl()).into(backgroundImage);
        });
    }

    void readPosts() {
        userdata.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;

            if (user.getPictures().size() != 0) {
                pictures.clear();
                pictures.addAll(user.getPictures());
                Collections.reverse(pictures);
                myPictureAdapter.notifyDataSetChanged();
            }

            if (user.getKitts().size() != 0) {
                kitts.clear();
                kitts.addAll(user.getKitts());
                Collections.reverse(kitts);
                myKittAdapter.notifyDataSetChanged();
            }
        });
    }
}