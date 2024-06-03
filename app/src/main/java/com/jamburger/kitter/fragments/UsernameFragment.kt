package com.jamburger.kitter.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.AddInfoActivity
import com.jamburger.kitter.components.User

class UsernameFragment(var parent: AddInfoActivity) : Fragment() {
    private lateinit var username: EditText
    private lateinit var userNames: MutableList<String>
    var valid: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_username, container, false)
        username = view.findViewById(R.id.et_username)
        parent.headerText.text = "Choose a username"
        userNames = ArrayList()
        FirebaseFirestore.getInstance().collection("Users").get()
            .addOnSuccessListener { userSnapshots: QuerySnapshot ->
                for (userSnapshot in userSnapshots) {
                    val user = userSnapshot.toObject<User>()
                    userNames.add(user.username)
                }
            }
        parent.nextButton.isClickable = false
        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                valid = true
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                parent.data["username"] = s.toString()
                if (!s.toString().matches("^\\w+$".toRegex()) || s.length < 3 || s.length > 15) {
                    valid = false
                }
                for (name in userNames) {
                    if (name == s.toString()) {
                        valid = false
                        break
                    }
                }
                if (!valid) {
                    username.setTextColor(resources.getColor(R.color.like))
                    parent.nextButton.isClickable = false
                } else {
                    username.setTextColor(resources.getColor(R.color.inverted))
                    parent.nextButton.isClickable = true
                }
            }
        })
        return view
    }
}