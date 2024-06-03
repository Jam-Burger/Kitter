package com.jamburger.kitter.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User
import com.jamburger.kitter.services.AuthService
import com.jamburger.kitter.utilities.ProfilePageManager

class OtherProfileActivity : AppCompatActivity() {
    private lateinit var profilePageManager: ProfilePageManager
    var userReference: DocumentReference? = null
    private var myReference: DocumentReference? = null
    private lateinit var followButton: Button
    private lateinit var messageButton: Button
    private var amFollowing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        val userid = intent.getStringExtra("userid")
        userReference = FirebaseFirestore.getInstance().collection("Users").document(userid!!)
        myReference = FirebaseFirestore.getInstance().collection("Users")
            .document(AuthService.auth.uid!!)

        profilePageManager = ProfilePageManager(this.window.decorView)

        followButton = findViewById(R.id.btn_follow)
        messageButton = findViewById(R.id.btn_message)

        if (myReference == userReference) {
            followButton.visibility = View.GONE
            messageButton.visibility = View.GONE
        }

        fillUserData()
        readPosts()

        followButton.setOnClickListener { toggleFollow() }
        messageButton.setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("userid", userid)
            startActivity(intent)
        }
    }


    private fun toggleFollow() {
        amFollowing = !amFollowing
        if (amFollowing) {
            profilePageManager.noOfFollowers.text =
                (Integer.parseInt(profilePageManager.noOfFollowers.text.toString()) + 1).toString()

            myReference!!.update("following", FieldValue.arrayUnion(userReference))
            userReference!!.update("followers", FieldValue.arrayUnion(myReference))
                .addOnSuccessListener { readPosts() }
            userReference!!.get().addOnSuccessListener { userSnapshot: DocumentSnapshot ->
                val user = userSnapshot.toObject<User>()!!
                val posts = user.posts
                for (postReference in posts) {
                    val map: MutableMap<String, Any> = HashMap()
                    map["postReference"] = postReference
                    map["visited"] = false
                    myReference!!.collection("feed").document(postReference.id).set(map)
                }
            }
        } else {
            profilePageManager.noOfFollowers.text =
                (profilePageManager.noOfFollowers.text.toString().toInt() - 1).toString()

            myReference!!.update("following", FieldValue.arrayRemove(userReference))
            userReference!!.update("followers", FieldValue.arrayRemove(myReference))
                .addOnSuccessListener { readPosts() }
            userReference!!.get().addOnSuccessListener { userSnapshot: DocumentSnapshot ->
                val user = userSnapshot.toObject<User>()!!
                val posts = user.posts
                for (postReference in posts) {
                    myReference!!.collection("feed").document(postReference.id).delete()
                }
            }
        }
        updateFollowButton()
    }


    private fun updateFollowButton() {
        if (amFollowing) {
            followButton.text = "Following"
            followButton.setBackgroundColor(resources.getColor(R.color.button_gray, theme))
        } else {
            followButton.text = "Follow"
            followButton.setBackgroundColor(resources.getColor(R.color.button_blue, theme))
        }
    }

    private fun fillUserData() {
        userReference!!.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
            val user = documentSnapshot.toObject<User>()!!
            profilePageManager.fillUserData(user)
            amFollowing = user.followers.contains(myReference)
            updateFollowButton()
        }
    }

    private fun readPosts() {
        userReference?.let { profilePageManager.readPosts(it) }
    }
}
