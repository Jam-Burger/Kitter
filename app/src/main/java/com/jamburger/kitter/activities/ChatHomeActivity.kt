package com.jamburger.kitter.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.ProfileAdapter
import com.jamburger.kitter.components.User

class ChatHomeActivity : AppCompatActivity() {
    private lateinit var recyclerViewProfiles: RecyclerView
    private var profiles: MutableList<User?>? = null
    private var allProfiles: List<User>? = null
    private var profileAdapter: ProfileAdapter? = null
    private lateinit var closeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_home)
        recyclerViewProfiles = findViewById(R.id.recyclerview_profiles)
        closeButton = findViewById(R.id.btn_close)

        profiles = ArrayList()
        allProfiles = ArrayList()
        profileAdapter = ProfileAdapter(this, profiles, "MESSAGE")
        recyclerViewProfiles.setHasFixedSize(true)
        recyclerViewProfiles.setAdapter(profileAdapter)

        readProfiles()
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun readProfiles() {
        val userReference = FirebaseFirestore.getInstance().collection("Users")
        userReference.get().addOnSuccessListener { usersSnapshots: QuerySnapshot ->
            profiles!!.clear()
            for (userSnapshot in usersSnapshots) {
                val user = userSnapshot.toObject(
                    User::class.java
                )
                if (user.id == FirebaseAuth.getInstance().uid) continue
                profiles!!.add(user)
            }
            profileAdapter!!.filterList(profiles)
        }
    }
}