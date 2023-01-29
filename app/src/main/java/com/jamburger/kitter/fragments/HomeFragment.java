package com.jamburger.kitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.PostActivity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);

        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.nav_post_image:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "picture");
                    startActivity(intent);
                    break;
                case R.id.nav_post_text:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "text");
                    startActivity(intent);
                    break;
            }
            return true;
        });

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), posts);
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setAdapter(postAdapter);
        readPosts();
        return view;
    }


    void readPosts() {
        CollectionReference postsReference = FirebaseFirestore.getInstance().collection("Posts");
        postsReference.get().addOnSuccessListener(postSnapshots -> {
            posts.clear();
            for (DocumentSnapshot postSnapshot : postSnapshots) {
                Post post = postSnapshot.toObject(Post.class);
                posts.add(post);
            }
            postAdapter.notifyDataSetChanged();
        });
    }
}