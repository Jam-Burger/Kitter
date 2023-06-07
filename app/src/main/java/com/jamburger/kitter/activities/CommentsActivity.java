package com.jamburger.kitter.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.adapters.CommentAdapter;
import com.jamburger.kitter.components.Comment;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.utilities.KeyboardManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    RecyclerView recyclerViewComments;
    EditText commentText;
    ImageView sendButton, closeButton;
    CommentAdapter commentAdapter;
    List<Comment> comments;
    DocumentReference postReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        recyclerViewComments = findViewById(R.id.recyclerview_comments);
        commentText = findViewById(R.id.et_comment);
        sendButton = findViewById(R.id.btn_send_message);
        closeButton = findViewById(R.id.btn_close);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setAdapter(commentAdapter);
        String postId = getIntent().getExtras().getString("postid");
        postReference = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        readComments();
        boolean openKeyboard = getIntent().getExtras().getBoolean("openKeyboard");
        if (openKeyboard) {
            commentText.requestFocus();
            KeyboardManager.openKeyboard(this);
        }

        sendButton.setOnClickListener(v -> {
            String commentString = commentText.getText().toString();
            if (commentString.isEmpty()) return;
            DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());

            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.post_time_format));
            String commentId = sdf.format(new Date());
            Comment comment = new Comment(userReference, commentString, commentId);

            KeyboardManager.closeKeyboard(this);
            commentText.clearFocus();
            commentText.setText("");
            postReference.update("comments", FieldValue.arrayUnion(comment)).addOnSuccessListener(unused -> {
                comments.add(comment);
                commentAdapter.notifyDataSetChanged();
            });
        });
        closeButton.setOnClickListener(view -> finish());
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        KeyboardManager.closeKeyboard(this);
        return super.dispatchTouchEvent(event);
    }

    private void readComments() {
        postReference.get().addOnSuccessListener(documentSnapshot -> {
            Post post = documentSnapshot.toObject(Post.class);
            comments.addAll(post.getComments());
            commentAdapter.notifyDataSetChanged();
        });
    }
}