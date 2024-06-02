package com.jamburger.kitter.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.ChatHomeActivity
import com.jamburger.kitter.activities.PostActivity
import com.jamburger.kitter.adapters.PostAdapter
import com.jamburger.kitter.components.Post

class HomeFragment : Fragment() {
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts)

        toolbar = view.findViewById(R.id.top_menu)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val intent: Intent
            val itemId = item.itemId
            when (itemId) {
                R.id.nav_post_image -> {
                    intent = Intent(requireActivity(), PostActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("type", "picture")
                    startActivity(intent)
                }

                R.id.nav_post_text -> {
                    intent = Intent(requireActivity(), PostActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("type", "text")
                    startActivity(intent)
                }

                R.id.nav_chat -> {
                    intent = Intent(requireActivity(), ChatHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
            true
        }

        postAdapter = PostAdapter(requireContext())
        recyclerViewPosts.setHasFixedSize(true)
        recyclerViewPosts.setAdapter(postAdapter)
        readPosts()
        return view
    }


    private fun readPosts() {
        val feedReference = FirebaseFirestore.getInstance().collection("Users").document(
            FirebaseAuth.getInstance().uid!!
        ).collection("feed")
        feedReference.get().addOnSuccessListener { feedSnapshots: QuerySnapshot ->
            postAdapter.clearPosts()
            for (feedSnapshot in feedSnapshots) {
                val postReference = feedSnapshot.getDocumentReference("postReference")
//                val isVisited = feedSnapshot.getBoolean("visited")
                assert(postReference != null)
                postReference!!.get().addOnSuccessListener { postSnapshot: DocumentSnapshot ->
                    postAdapter.addPost(
                        postSnapshot.toObject(
                            Post::class.java
                        )
                    )
                }
            }
        }
    }
}