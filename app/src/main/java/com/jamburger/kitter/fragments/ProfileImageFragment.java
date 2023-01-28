package com.jamburger.kitter.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.jamburger.kitter.AddInfoActivity;
import com.jamburger.kitter.R;

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
                parent.profileImagePath = uri;
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
        parent.headerText.setText("Choose a profile image");
        Glide.with(requireContext())
                .load(getResources().getString(R.string.default_profile_img_url))
                .into(profileImage);
        chooseButton.setOnClickListener(v -> {
            selectImage();
        });
        skipButton.setOnClickListener(v -> {
            parent.nextFragment();
        });
        return view;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
}