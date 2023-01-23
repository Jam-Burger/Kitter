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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.LoginActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.MyPostAdapter;
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
    Toolbar toolbar;

    DocumentReference userdata;
    RecyclerView recyclerViewPosts;
    MyPostAdapter myPostAdapter;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userdata = db.collection("Users").document(user.getUid());
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        backgroundImage = view.findViewById(R.id.img_background);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);

        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_edit:
                    break;
                case R.id.nav_saved:
                    Toast.makeText(requireContext(), "In Development", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_change_password:
                    break;
                case R.id.nav_logout:
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        // Check condition
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    });
                    break;
            }
            return true;
        });

        recyclerViewPosts = view.findViewById(R.id.recyclerview_myposts);
        recyclerViewPosts.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        recyclerViewPosts.setLayoutManager(gridLayoutManager);

        posts = new ArrayList<>();
        myPostAdapter = new MyPostAdapter(requireContext(), posts);
        recyclerViewPosts.setAdapter(myPostAdapter);

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
        userdata.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            List<Post> myPosts = new ArrayList<>();
            if (user.getPosts().size() == 0) return;

            for (DocumentReference documentReference : user.getPosts()) {
                documentReference.get().addOnSuccessListener(documentSnapshot -> {
                    myPosts.add(documentSnapshot.toObject(Post.class));
                    if (myPosts.size() == user.getPosts().size()) {
                        FirebaseFirestore.getInstance().collection("Posts").get().addOnSuccessListener(postSnapshots -> {
                            posts.clear();
                            for (DocumentSnapshot postSnapshot : postSnapshots) {
                                Post post = postSnapshot.toObject(Post.class);
                                for (Post current : myPosts) {
                                    if (!current.getPostid().equals(post.getPostid())) continue;
                                    posts.add(0, post);
                                    break;
                                }
                            }
                            myPostAdapter.notifyDataSetChanged();
                        });
                    }
                });
            }
        });
    }
}