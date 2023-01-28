package com.jamburger.kitter;

import static com.jamburger.kitter.MainActivity.bottomNavigationView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.components.User;
import com.jamburger.kitter.fragments.ProfileFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EditInfoActivity extends AppCompatActivity {
    User user;
    DocumentReference userReference;
    EditText name, username, bio;
    ImageView profileImage;
    Uri profileImageUri = null;
    HashMap<String, Object> data = new HashMap<>();
    ImageView saveInfoButton, closeButton, profileImageEditButton;

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

        profileImageEditButton.setOnClickListener(view -> {
            selectImage();
        });
        closeButton.setOnClickListener(view -> {
            finish();
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
        userReference.update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                bottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                MainActivity.selectorFragment = new ProfileFragment();
                startActivity(intent);
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
            String txt_username = user.getUsername();
            username.setText(txt_username);
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
        String postId = sdf.format(new Date());
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