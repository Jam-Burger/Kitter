package com.jamburger.kitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.fragments.SelectSourceDialogFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostActivity extends AppCompatActivity {
    private static final byte REQUEST_IMAGE_CAPTURE = 69;
    ImageView imageView;

    TextView caption;
    Uri filePath = null;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseUser user;
    String currentPhotoPath;
    ActivityResultLauncher<Intent> fromGalleryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                filePath = data.getData();
                showActivity(true);
                Glide.with(this).load(filePath).into(imageView);
            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startHomeFragment();
        }
    });
    ActivityResultLauncher<Intent> fromCameraResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            showActivity(true);
            Glide.with(this).load(filePath).into(imageView);
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
        db = FirebaseFirestore.getInstance();

        closeButton.setOnClickListener(view -> {
            startHomeFragment();
        });
        postButton.setOnClickListener(view -> {
            publishPost();
        });

        String type = getIntent().getStringExtra("type");
        Toast.makeText(this, type, Toast.LENGTH_SHORT).show();

        if (type.equals("picture")) {
            showDialog();
            showActivity(false);
        } else {
            caption.setHint("Enter text content");
        }
    }

    void showDialog() {
        SelectSourceDialogFragment newFragment = SelectSourceDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                filePath = FileProvider.getUriForFile(this,
                        "com.jamburger.kitter.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                fromCameraResultLauncher.launch(Intent.createChooser(takePictureIntent, "Select Picture"));
            }
        }
    }

    private void publishPost() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String postId = sdf.format(new Date());
            StorageReference ref = storageReference.child("Posts/" + postId);

            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                storageReference.child("Posts").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    Post post = new Post(user.getUid(), postId, uri.toString(), caption.getText().toString());
                    DocumentReference postRef = db.collection("Posts").document(postId);

                    postRef.set(post).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        startHomeFragment();
                    });
                    db.collection("Users").document(user.getUid())
                            .update("posts", FieldValue.arrayUnion(postRef));
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress =
                        ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String postId = sdf.format(new Date());
            Post post = new Post(user.getUid(), postId, "", "");
            post.setKitt(caption.getText().toString());
            DocumentReference postRef = db.collection("Posts").document(postId);
            postRef.set(post).addOnCompleteListener(task -> {
                startHomeFragment();
            });
            db.collection("Users").document(user.getUid())
                    .update("posts", FieldValue.arrayUnion(postRef));
        }
    }

    public void startHomeFragment() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fromGalleryResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void showActivity(boolean set) {
        if (set) findViewById(R.id.post_parent).setVisibility(View.VISIBLE);
        else findViewById(R.id.post_parent).setVisibility(View.INVISIBLE);
    }

    public void selectFromCamera() {
        takePicture();
    }

    public void selectFromGallery() {
        selectPicture();
    }
}