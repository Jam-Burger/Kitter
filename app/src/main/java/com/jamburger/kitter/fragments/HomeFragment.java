package com.jamburger.kitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.LoginActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    Toolbar toolbar;
    List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);
        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_saved:
                    Toast.makeText(requireContext(), "In Development", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    requireActivity().finish();
                    break;
            }
            return true;
        });

        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), posts);
        recyclerViewPosts.setAdapter(postAdapter);
        readPosts();
        return view;
    }

    void readPosts() {
        FirebaseFirestore.getInstance().collection("Posts").get().addOnSuccessListener(postSnapshots -> {
            posts.clear();
            for (DocumentSnapshot postSnapshot : postSnapshots) {
                Post post = postSnapshot.toObject(Post.class);
                posts.add(post);
            }
            postAdapter.notifyDataSetChanged();
        });
    }
}