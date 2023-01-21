package com.jamburger.kitter.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.PostAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    final byte PROFILE_IMG = 0;
    final byte BACKGROUND_IMG = 1;
    ImageView backgroundImage, profileImage;
    TextView name, username, bio;
    DocumentReference userdata;
    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseUser user;
    List<Post> posts;
    byte mode;
    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                postImage(data.getData());
            }
        }
    });

    public ProfileFragment(DocumentReference userdata) {
        this.userdata = userdata;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        backgroundImage = view.findViewById(R.id.img_background);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();


        recyclerViewPosts = view.findViewById(R.id.recyclerview_myposts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), posts);
        recyclerViewPosts.setAdapter(postAdapter);

        fillUserData();
        readPosts();

        profileImage.setOnClickListener(v -> {
            selectImage();
            mode = PROFILE_IMG;
        });
        backgroundImage.setOnClickListener(v -> {
            selectImage();
            mode = BACKGROUND_IMG;
        });
        return view;
    }

    private void postImage(Uri filePath) {
        if (filePath != null) {
            String folder = mode == PROFILE_IMG ? "Profile Pictures" : "Background Pictures";
            String attribute = mode == PROFILE_IMG ? "profileImageUrl" : "backgroundImageUrl";
            ProgressDialog progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            String postId = sdf.format(new Date());
            StorageReference ref = storageReference.child(folder + "/" + postId);

            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(requireActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                storageReference.child(folder).child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Users").document(user.getUid()).update(attribute, uri.toString()).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                    });
                }).addOnFailureListener(e -> progressDialog.dismiss());
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress =
                        ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void fillUserData() {
        userdata.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            assert user != null;
            String txt_username = "@" + user.getUsername();
            username.setText(txt_username);
            name.setText(user.getName());
            bio.setText(user.getBio());

            Glide.with(requireActivity()).load(user.getProfileImageUrl()).into(profileImage);
            Glide.with(requireActivity()).load(user.getBackgroundImageUrl()).into(backgroundImage);
        });
    }

    private void readPosts() {
        userdata.get().addOnSuccessListener(documentSnapshot -> {
            List<DocumentReference> postReferences = (List<DocumentReference>) documentSnapshot.get("posts");
            posts.clear();
            if (postReferences == null) return;
            for (DocumentReference postReference : postReferences) {
                postReference.get().addOnSuccessListener(postSnapshot -> {
                    Post post = postSnapshot.toObject(Post.class);
                    posts.add(post);
                    postAdapter.notifyDataSetChanged();
                });
            }
        });
    }
}