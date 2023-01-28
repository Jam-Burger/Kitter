package com.jamburger.kitter.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.AddInfoActivity;
import com.jamburger.kitter.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileImageFragment extends Fragment {
    Button chooseButton, skipButton;
    ImageView profileImage;
    AddInfoActivity parent;
    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Glide.with(this).load(uri).into(profileImage);
                postImage(uri);
            }
        }
    });

    public ProfileImageFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_image, container, false);

        chooseButton = view.findViewById(R.id.btn_choose);
        skipButton = view.findViewById(R.id.btn_skip);
        profileImage = view.findViewById(R.id.img_profile);

        chooseButton.setOnClickListener(v -> {
            selectImage();
        });
        skipButton.setOnClickListener(v -> {
            parent.changeFragment();
        });
        return view;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void postImage(Uri filePath) {
        if (filePath != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            String postId = sdf.format(new Date());
            StorageReference ref = storageReference.child("Profile Pictures/" + postId);

            ref.putFile(filePath).addOnCompleteListener(task0 -> {
                if (task0.isSuccessful()) {
                    Toast.makeText(requireContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                    storageReference.child("Profile Pictures/").child(postId).getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            AddInfoActivity.data.put("profileImageUrl", task.getResult().toString());
                        progressDialog.dismiss();
                    });
                } else {
                    Toast.makeText(requireContext(), "Failed " + task0.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(taskSnapshot -> {
                double progress = ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        }
    }
}