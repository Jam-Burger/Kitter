package com.jamburger.kitter.utilities

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.MyKittAdapter
import com.jamburger.kitter.adapters.MyPictureAdapter
import com.jamburger.kitter.components.Post
import com.jamburger.kitter.components.User

class ProfilePageManager(var view: View) {
    var noOfFollowers: TextView
    var backgroundImage: ImageView
    private var myPictureAdapter: MyPictureAdapter? = null
    private var myKittAdapter: MyKittAdapter? = null
    private var profileImage: ImageView
    private var name: TextView
    private var username: TextView
    private var bio: TextView
    private var noOfPosts: TextView
    private var noOfFollowing: TextView
    private var picturesButton: ImageView
    private var kittsButton: ImageView
    private var recyclerViewMyPosts: RecyclerView
    private var layoutManager: GridLayoutManager? = null
    private var picturesIndicator: View
    private var kittsIndicator: View
    var context: Context

    init {
        view.visibility = View.INVISIBLE

        backgroundImage = view.findViewById(R.id.img_background)
        profileImage = view.findViewById(R.id.img_profile)
        name = view.findViewById(R.id.txt_name)
        username = view.findViewById(R.id.txt_username)
        bio = view.findViewById(R.id.txt_bio)

        noOfPosts = view.findViewById(R.id.txt_post_count)
        noOfFollowers = view.findViewById(R.id.txt_followers_count)
        noOfFollowing = view.findViewById(R.id.txt_following_count)

        picturesButton = view.findViewById(R.id.btn_my_pictures)
        kittsButton = view.findViewById(R.id.btn_my_kitts)

        recyclerViewMyPosts = view.findViewById(R.id.recyclerview_my_posts)

        picturesIndicator = view.findViewById(R.id.indicator_pictures)
        kittsIndicator = view.findViewById(R.id.indicator_kitts)

        context = view.context
        handlePostsArea()
    }

    private fun handlePostsArea() {
        myPictureAdapter = MyPictureAdapter(context)
        myKittAdapter = MyKittAdapter(context)

        layoutManager = GridLayoutManager(context, 3)

        recyclerViewMyPosts.setHasFixedSize(true)
        recyclerViewMyPosts.adapter = myPictureAdapter
        recyclerViewMyPosts.layoutManager = layoutManager

        picturesButton.setOnClickListener {
            recyclerViewMyPosts.adapter = myPictureAdapter
            layoutManager!!.spanCount = 3
            picturesIndicator.visibility = View.VISIBLE
            kittsIndicator.visibility = View.INVISIBLE
        }
        kittsButton.setOnClickListener {
            recyclerViewMyPosts.adapter = myKittAdapter
            layoutManager!!.spanCount = 1
            picturesIndicator.visibility = View.INVISIBLE
            kittsIndicator.visibility = View.VISIBLE
        }
    }

    fun readPosts(userReference: DocumentReference) {
        userReference.get().addOnSuccessListener { userSnapshot: DocumentSnapshot ->
            val user = userSnapshot.toObject(
                User::class.java
            )!!
            myPictureAdapter!!.clearPosts()
            myKittAdapter!!.clearPosts()
            for (postReference in user.posts) {
                postReference.get().addOnSuccessListener { postSnapshot: DocumentSnapshot ->
                    val post = postSnapshot.toObject(Post::class.java)!!
                    if (post.imageUrl.isNotEmpty()) {
                        myPictureAdapter!!.addPost(postReference)
                    }
                    if (post.kitt.isNotEmpty()) {
                        myKittAdapter!!.addPost(postReference)
                    }
                }
            }
        }
    }

    fun fillUserData(user: User) {
        val txtUsername = "@" + user.username

        username.text = txtUsername
        name.text = user.name
        bio.text = user.bio

        noOfPosts.text = user.posts.size.toString()
        noOfFollowers.text = user.followers.size.toString()
        noOfFollowing.text = user.following.size.toString()

        Glide.with(context).load(user.profileImageUrl).into(profileImage)
        Glide.with(context).load(user.backgroundImageUrl).into(backgroundImage)

        view.visibility = View.VISIBLE
    }
}
