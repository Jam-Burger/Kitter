package com.jamburger.kitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    ImageView backgroundImage, profileImage;
    TextView name, username, bio;
    DatabaseReference userdata;
    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    List<Post> posts;

    public ProfileFragment(DatabaseReference userdata) {
        this.userdata = userdata;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Inflate the layout for this fragment
        backgroundImage = view.findViewById(R.id.img_background);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);
        userdata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);
                assert user != null;
                String txt_username = "@" + user.getUsername();
                username.setText(txt_username);
                name.setText(user.getName());
                bio.setText(user.getBio());

                Glide.with(requireActivity())
                        .load(user.getProfileImageUrl())
                        .into(profileImage);
                Glide.with(requireActivity())
                        .load(user.getBackgroundImageUrl())
                        .into(backgroundImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        recyclerViewPosts = view.findViewById(R.id.recyclerview_myposts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), posts);
        recyclerViewPosts.setAdapter(postAdapter);

        readPosts();
        return view;
    }

    private void readPosts() {
        userdata.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot postNameSnapshot : snapshot.getChildren()) {
                    String name = postNameSnapshot.getValue().toString();
                    FirebaseDatabase.getInstance().getReference().child("Posts").child(name).get().addOnSuccessListener(postSnapshot -> {
                        Post post = postSnapshot.getValue(Post.class);
                        posts.add(post);
                        postAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}