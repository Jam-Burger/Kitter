package com.jamburger.kitter.fragments;

import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.SavedPostsActivity;
import com.jamburger.kitter.activities.SettingsActivity;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

public class ProfileFragment extends Fragment {
    ImageView backgroundImage, profileImage, backgroundImageEditButton;
    TextView name, username, bio, noOfPosts, noOfFollowers, noOfFollowing;
    Toolbar toolbar;
    DocumentReference userdataReference;
    MyPictureAdapter myPictureAdapter;
    MyKittAdapter myKittAdapter;
    FirebaseFirestore db;
    FirebaseUser user;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    GridLayoutManager layoutManager;

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Glide.with(this).load(uri).into(backgroundImage);
                postImage(uri);
            }
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userdataReference = db.collection("Users").document(user.getUid());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        backgroundImage = view.findViewById(R.id.img_background);
        backgroundImageEditButton = view.findViewById(R.id.btn_change_background_img);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);

        noOfPosts = view.findViewById(R.id.txt_post_count);
        noOfFollowers = view.findViewById(R.id.txt_followers_count);
        noOfFollowing = view.findViewById(R.id.txt_following_count);

        picturesButton = view.findViewById(R.id.btn_my_pictures);
        kittsButton = view.findViewById(R.id.btn_my_kitts);

        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_saved:
                    startActivity(new Intent(requireActivity(), SavedPostsActivity.class));
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(requireActivity(), SettingsActivity.class));
                    break;
            }
            return true;
        });

        myPictureAdapter = new MyPictureAdapter(getContext());
        myKittAdapter = new MyKittAdapter(getContext());

        layoutManager = new GridLayoutManager(getContext(), 3);

        recyclerViewMyPosts = view.findViewById(R.id.recyclerview_my_posts);
        recyclerViewMyPosts.setHasFixedSize(true);
        recyclerViewMyPosts.setAdapter(myPictureAdapter);
        recyclerViewMyPosts.setLayoutManager(layoutManager);

        fillUserData();
        readPosts();

        View picturesIndicator = view.findViewById(R.id.indicator_pictures);
        View kittsIndicator = view.findViewById(R.id.indicator_kitts);
        picturesButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myPictureAdapter);
            layoutManager.setSpanCount(3);
            picturesIndicator.setVisibility(View.VISIBLE);
            kittsIndicator.setVisibility(View.INVISIBLE);
        });
        kittsButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myKittAdapter);
            layoutManager.setSpanCount(1);
            picturesIndicator.setVisibility(View.INVISIBLE);
            kittsIndicator.setVisibility(View.VISIBLE);
        });

        backgroundImageEditButton.setOnClickListener(v -> {
            selectImage();
        });

        return view;
    }

    private void postImage(Uri filePath) {
        if (filePath != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            ProgressDialog progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String postId = user.getUid();
            StorageReference ref = storageReference.child("Background Pictures/" + postId);
            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(requireActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                storageReference.child("Background Pictures").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Users").document(user.getUid()).update("backgroundImageUrl", uri.toString()).addOnCompleteListener(task -> {
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

    void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
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

            try {
                Glide.with(requireActivity()).load(user.getProfileImageUrl()).into(profileImage);
                Glide.with(requireActivity()).load(user.getBackgroundImageUrl()).into(backgroundImage);
            } catch (Exception ignored) {

            }
        });
    }

    void readPosts() {
        userdataReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            myPictureAdapter.clearPosts();
            myKittAdapter.clearPosts();
            for (DocumentReference postReference : user.getPosts()) {
                postReference.get().addOnSuccessListener(postSnapshot -> {
                    Post post = postSnapshot.toObject(Post.class);
                    assert post != null;
                    if (!post.getImageUrl().isEmpty()) {
                        myPictureAdapter.addPost(postReference);
                    }
                    if (!post.getKitt().isEmpty()) {
                        myKittAdapter.addPost(postReference);
                    }
                });
            }
        });
    }
}