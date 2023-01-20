package com.jamburger.kitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {
    ImageView imageView;
    TextView caption;
    Uri filePath = null;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseUser user;
    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                filePath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startHomeFragment();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        imageView = findViewById(R.id.post_img);
        caption = findViewById(R.id.et_caption);

        ImageView closeButton = findViewById(R.id.btn_close);
        ImageView postButton = findViewById(R.id.btn_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        selectImage();

        closeButton.setOnClickListener(view -> {
            startHomeFragment();
        });
        postButton.setOnClickListener(view -> {
            postImage();
        });
    }

    private void postImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            String postId = sdf.format(new Date());
            StorageReference ref = storageReference.child("Posts/" + postId);

            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                HashMap<String, String> map = new HashMap<>();
                map.put("postid", postId);
                map.put("creator", user.getUid());
                map.put("caption", caption.getText().toString());
                databaseReference.child("Users").child(user.getUid()).child("posts").push().setValue(postId);
                storageReference.child("Posts").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    map.put("imageUrl", uri.toString());
                    databaseReference.child("Posts").child(postId).setValue(map).addOnCompleteListener(task -> {
                        startHomeFragment();
                        progressDialog.dismiss();
                    });
                    startHomeFragment();
                });
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

    private void startHomeFragment() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
}