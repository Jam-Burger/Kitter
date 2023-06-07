package com.jamburger.kitter.fragments;

import static android.content.Context.MODE_PRIVATE;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.EditInfoActivity;
import com.jamburger.kitter.activities.LoginActivity;
import com.jamburger.kitter.activities.MainActivity;
import com.jamburger.kitter.activities.SavedPostsActivity;
import com.jamburger.kitter.adapters.MyKittAdapter;
import com.jamburger.kitter.adapters.MyPictureAdapter;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView backgroundImage, profileImage, backgroundImageEditButton;
    TextView name, username, bio, noOfPosts, noOfFollowers, noOfFollowing;
    Toolbar toolbar;
    DocumentReference userdataReference;
    MyPictureAdapter myPictureAdapter;
    MyKittAdapter myKittAdapter;
    FirebaseFirestore db;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    ImageView picturesButton, kittsButton;
    RecyclerView recyclerViewMyPosts;
    List<DocumentReference> pictures;
    List<DocumentReference> kitts;

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

        noOfPosts = view.findViewById(R.id.txt_post_count);
        noOfFollowers = view.findViewById(R.id.txt_followers_count);
        noOfFollowing = view.findViewById(R.id.txt_following_count);

        picturesButton = view.findViewById(R.id.btn_my_pictures);
        kittsButton = view.findViewById(R.id.btn_my_kitts);

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
                    intentMain.putExtra("page", "PROFILE");
                    requireActivity().finish();
                    startActivity(intentMain);
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

        pictures = new ArrayList<>();
        myPictureAdapter = new MyPictureAdapter(getContext(), pictures);

        kitts = new ArrayList<>();
        myKittAdapter = new MyKittAdapter(getContext(), kitts);

        recyclerViewMyPosts = view.findViewById(R.id.recyclerview_my_posts);
        recyclerViewMyPosts.setHasFixedSize(true);
        ((GridLayoutManager) recyclerViewMyPosts.getLayoutManager()).setSpanCount(3);
        recyclerViewMyPosts.setAdapter(myPictureAdapter);

        fillUserData();
        readPosts();

        View picturesIndicator = view.findViewById(R.id.indicator_pictures);
        View kittsIndicator = view.findViewById(R.id.indicator_kitts);
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

            noOfPosts.setText(String.valueOf(user.getPictures().size() + user.getKitts().size()));
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