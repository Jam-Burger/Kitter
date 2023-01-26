package com.jamburger.kitter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.adapters.CommentAdapter;
import com.jamburger.kitter.components.Comment;
import com.jamburger.kitter.components.Post;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    RecyclerView recyclerViewComments;
    EditText commentText;
    ImageView sendButton, closeButton;
    CommentAdapter commentAdapter;
    List<Comment> comments;
    DocumentReference postReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        recyclerViewComments = findViewById(R.id.recyclerview_comments);
        commentText = findViewById(R.id.et_comment);
        sendButton = findViewById(R.id.btn_send_comment);
        closeButton = findViewById(R.id.btn_close);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setAdapter(commentAdapter);
        String postId = getIntent().getExtras().getString("postid");
        postReference = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        readComments();
        sendButton.setOnClickListener(v -> {
            String commentString = commentText.getText().toString();
            DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());
            Comment comment = new Comment(userReference, commentString);
            closeKeyboard();
            commentText.clearFocus();
            commentText.setText("");
            postReference.update("comments", FieldValue.arrayUnion(comment)).addOnSuccessListener(unused -> {
                comments.add(comment);
                commentAdapter.notifyDataSetChanged();
            });
        });
        closeButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void readComments() {
        postReference.get().addOnSuccessListener(documentSnapshot -> {
            Post post = documentSnapshot.toObject(Post.class);
            comments.addAll(post.getComments());
            commentAdapter.notifyDataSetChanged();
        });
    }
}