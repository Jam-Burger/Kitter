package com.jamburger.kitter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jamburger.kitter.R
import com.jamburger.kitter.adapters.ProfileAdapter
import com.jamburger.kitter.components.User
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var searchbar: SearchView
    private lateinit var recyclerViewProfiles: RecyclerView
    private lateinit var messageText: TextView
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profiles: MutableList<User>
    private lateinit var allProfiles: MutableList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerViewProfiles = view.findViewById(R.id.recyclerview_profiles)
        searchbar = view.findViewById(R.id.et_search)
        messageText = view.findViewById(R.id.txt_message)

        profiles = ArrayList()
        allProfiles = ArrayList()
        profileAdapter = ProfileAdapter(requireContext(), profiles, "PROFILE")
        recyclerViewProfiles.setHasFixedSize(true)
        recyclerViewProfiles.setAdapter(profileAdapter)

        readProfiles()

        searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        return view
    }

    private fun filter(text: String) {
        profiles.clear()
        messageText.visibility = View.GONE
        if (text.isEmpty()) return
        for (user in allProfiles) {
            if (user.name.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault())) || user.username.lowercase(
                    Locale.getDefault()
                ).contains(text.lowercase(Locale.getDefault()))
            ) {
                profiles.add(user)
            }
        }
        if (profiles.isEmpty()) {
            messageText.visibility = View.VISIBLE
        }
        profileAdapter.filterList(profiles)
    }

    private fun readProfiles() {
        val userReference = FirebaseFirestore.getInstance().collection("Users")
        userReference.get().addOnSuccessListener { usersSnapshots: QuerySnapshot ->
            allProfiles.clear()
            for (userSnapshot in usersSnapshots) {
                val user = userSnapshot.toObject(
                    User::class.java
                )
                allProfiles.add(user)
            }
        }
    }
}