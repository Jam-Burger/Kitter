package com.jamburger.kitter.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.PostAdapter
import com.jamburger.kitter.components.Post
import com.jamburger.kitter.components.User
import com.jamburger.kitter.services.AuthService

class SavedPostsActivity : AppCompatActivity() {
    private lateinit var savedPostsRecyclerview: RecyclerView
    private lateinit var closeButton: ImageView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_posts)

        savedPostsRecyclerview = findViewById(R.id.recyclerview_saved_posts)
        closeButton = findViewById(R.id.btn_close)

        closeButton.setOnClickListener { finish() }

        savedPostsRecyclerview.setHasFixedSize(true)

        postAdapter = PostAdapter(this)
        savedPostsRecyclerview.setAdapter(postAdapter)

        readPosts()
    }

    private fun readPosts() {
        val userReference =
            FirebaseFirestore.getInstance().document("Users/" + AuthService.auth.uid)
        userReference.get().addOnSuccessListener { userSnapshot: DocumentSnapshot ->
            val user = userSnapshot.toObject<User>()!!
            for (savedPostReference in user.saved) {
                savedPostReference.get()
                    .addOnSuccessListener { savedPostSnapshot: DocumentSnapshot ->
                        savedPostSnapshot.toObject<Post>()?.let {
                            postAdapter.addPost(it)
                        }
                    }
            }
        }
    }
}