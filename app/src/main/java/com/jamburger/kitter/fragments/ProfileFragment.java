package com.jamburger.kitter.fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.jamburger.kitter.MainActivity.bottomNavigationView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
import com.jamburger.kitter.EditInfoActivity;
import com.jamburger.kitter.LoginActivity;
import com.jamburger.kitter.MainActivity;
import com.jamburger.kitter.R;
import com.jamburger.kitter.SavedPostsActivity;
import com.jamburger.kitter.adapters.MyPostAdapter;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.components.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    ImageView backgroundImage, profileImage, backgroundImageEditButton;
    TextView name, username, bio;
    Toolbar toolbar;
    DocumentReference userdata;
    RecyclerView recyclerViewPosts;
    MyPostAdapter myPostAdapter;
    FirebaseFirestore db;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    List<Post> posts;
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
        userdata = db.collection("Users").document(user.getUid());
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
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

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true);
        toolbar = view.findViewById(R.id.top_menu);

        MenuItem themeItem = toolbar.getMenu().findItem(R.id.nav_change_theme);
        if (isDarkModeOn) {
            themeItem.setTitle("Dark Mode");
            themeItem.setIcon(R.drawable.ic_dark);
        } else {
            themeItem.setTitle("Light Mode");
            themeItem.setIcon(R.drawable.ic_light);
        }

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_edit:
                    startActivity(new Intent(requireActivity(), EditInfoActivity.class));
                    break;
                case R.id.nav_saved:
                    startActivity(new Intent(requireActivity(), SavedPostsActivity.class));
                    break;
                case R.id.nav_change_password:
                    showRecoverPasswordDialog();
                    break;
                case R.id.nav_change_theme:
                    Intent intentMain = new Intent(requireActivity(), MainActivity.class);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MainActivity.selectorFragment = new ProfileFragment();
                    bottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                    startActivity(intentMain);
                    requireActivity().finish();
                    if (isDarkModeOn) {
                        editor.putBoolean("isDarkModeOn", false).apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        editor.putBoolean("isDarkModeOn", true).apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    break;
                case R.id.nav_logout:
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intentLogin = new Intent(requireActivity(), LoginActivity.class);
                            intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentLogin);
                            requireActivity().finish();
                        }
                    });
                    break;
            }
            return true;
        });

        recyclerViewPosts = view.findViewById(R.id.recyclerview_myposts);
        recyclerViewPosts.setHasFixedSize(true);

        posts = new ArrayList<>();
        myPostAdapter = new MyPostAdapter(requireContext(), posts);
        recyclerViewPosts.setAdapter(myPostAdapter);

        fillUserData();
        readPosts();

        backgroundImageEditButton.setOnClickListener(v -> {
            selectImage();
        });
        return view;
    }

    void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Password");
        String email = user.getEmail();

        final LinearLayout linearLayout = new LinearLayout(getContext());
        final TextView emailEt = new TextView(getContext());
        final String txt = "We will send change password link to :\n" + email;
        emailEt.setText(txt);
        emailEt.setMinEms(16);
        linearLayout.setPadding(50, 20, 50, 0);
        linearLayout.addView(emailEt);
        builder.setView(linearLayout);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            beginRecovery(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void beginRecovery(String email) {
        ProgressDialog loadingBar = new ProgressDialog(getContext());
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "Link sent on " + email, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        });
    }

    private void postImage(Uri filePath) {
        if (filePath != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            ProgressDialog progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            String postId = sdf.format(new Date());
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
                                    if (!current.getPostid().equals(post.getPostid()) || post.getImageUrl().isEmpty())
                                        continue;
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