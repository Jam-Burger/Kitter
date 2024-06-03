package com.jamburger.kitter.activities

import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.CommentAdapter
import com.jamburger.kitter.components.Comment
import com.jamburger.kitter.utilities.DateTimeFormatter
import com.jamburger.kitter.utilities.KeyboardManager

class CommentsActivity : AppCompatActivity() {
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var comments: MutableList<Comment>
    private lateinit var commentsReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        recyclerViewComments = findViewById(R.id.recyclerview_comments)
        commentText = findViewById(R.id.et_comment)
        sendButton = findViewById(R.id.btn_send_message)
        closeButton = findViewById(R.id.btn_close)

        comments = ArrayList()
        commentAdapter = CommentAdapter(this, comments)
        recyclerViewComments.setHasFixedSize(true)
        recyclerViewComments.setAdapter(commentAdapter)
        val postId = intent.extras!!.getString("postid")
        commentsReference =
            FirebaseDatabase.getInstance().reference.child("comments").child(postId!!)

        readComments()
        val openKeyboard = intent.extras!!.getBoolean("openKeyboard")
        if (openKeyboard) {
            commentText.requestFocus()
            KeyboardManager.openKeyboard(this)
        }

        sendButton.setOnClickListener {
            val commentString = commentText.getText().toString()
            if (commentString.isEmpty()) return@setOnClickListener
            val userReference = FirebaseFirestore.getInstance().collection("Users").document(
                FirebaseAuth.getInstance().uid!!
            )

            val commentId = DateTimeFormatter.currentTime
            val comment = Comment(userReference.id, commentString, commentId)

            KeyboardManager.closeKeyboard(this)
            commentText.clearFocus()
            commentText.setText("")
            commentsReference.child(commentId).setValue(comment)
                .addOnSuccessListener {
                    comments.add(comment)
                    commentAdapter.notifyDataSetChanged()
                }
        }
        closeButton.setOnClickListener { finish() }
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val focused = currentFocus
        if (focused != null) {
            KeyboardManager.closeKeyboard(this)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun readComments() {
        commentsReference.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                comments.clear()
                for (commentSnapshot in task.result.children) {
                    commentSnapshot.getValue<Comment>()?.let { comments.add(it) }
                }
                commentAdapter.notifyDataSetChanged()
            }
        }
    }
}