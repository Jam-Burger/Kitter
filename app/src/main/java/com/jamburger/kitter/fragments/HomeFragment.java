package com.jamburger.kitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts);
        recyclerViewPosts.setAdapter(postAdapter);

        readPosts();
        return view;
    }

    void readPosts() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    posts.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}