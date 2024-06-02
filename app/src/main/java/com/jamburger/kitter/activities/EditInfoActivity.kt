package com.jamburger.kitter.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditInfoActivity extends AppCompatActivity {
    User user;
    DocumentReference userReference;
    EditText name, username, bio;
    ImageView profileImage;
    Uri profileImageUri = null;
    HashMap<String, Object> data = new HashMap<>();
    ImageView saveInfoButton, closeButton, profileImageEditButton;
    boolean valid;
    List<String> userNames;

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                profileImageUri = data.getData();
                Glide.with(this).load(profileImageUri).into(profileImage);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        profileImage = findViewById(R.id.img_profile);
        name = findViewById(R.id.et_name);
        username = findViewById(R.id.et_username);
        bio = findViewById(R.id.et_bio);
        saveInfoButton = findViewById(R.id.btn_save_info);
        closeButton = findViewById(R.id.btn_close);
        profileImageEditButton = findViewById(R.id.btn_change_profile_img);

        fillUserData();

        profileImageEditButton.setOnClickListener(view -> selectImage());
        closeButton.setOnClickListener(view -> finish());

        userNames = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                userNames.add(user.getUsername());
            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().matches("^\\w+$") || s.length() < 3 || s.length() > 15) {
                    valid = false;
                }
                for (String name : userNames) {
                    if (!name.equals(user.getUsername()) && name.equals(s.toString())) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    username.setTextColor(getResources().getColor(R.color.like));
                    saveInfoButton.setClickable(false);
                } else {
                    username.setTextColor(getResources().getColor(R.color.inverted));
                    saveInfoButton.setClickable(true);
                }
            }
        });

        saveInfoButton.setOnClickListener(view -> {
            data.put("name", name.getText().toString());
            data.put("username", username.getText().toString());
            data.put("bio", bio.getText().toString());
            if (profileImageUri != null)
                updateImageAndData();
            else
                updateData();
        });
    }

    private void updateData() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(data.get("name").toString())
                .setPhotoUri(profileImageUri)
                .build();
        FirebaseAuth.getInstance().getCurrentUser().updateProfile(request);
        userReference.update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intentMain = new Intent(this, MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentMain.putExtra("page", "PROFILE");
                startActivity(intentMain);
                finish();
            }
        });
    }

    private void fillUserData() {
        userReference = FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getUid());
        userReference.get().addOnSuccessListener(snapshot -> {
            user = snapshot.toObject(User.class);
            assert user != null;
            username.setText(user.getUsername());
            name.setText(user.getName());
            bio.setText(user.getBio());
            Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void updateImageAndData() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Profile...");
        progressDialog.show();

        String postId = user.getId();
        StorageReference ref = storageReference.child("Profile Pictures/" + postId);

        ref.putFile(profileImageUri).addOnSuccessListener(snapshot -> {
            Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
            storageReference.child("Profile Pictures/").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                data.put("profileImageUrl", uri.toString());
                updateData();
                progressDialog.dismiss();
            }).addOnFailureListener(e -> progressDialog.dismiss());
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress =
                    ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });
    }
}