package com.jamburger.kitter.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.AddInfoActivity

class DetailsFragment(var parent: AddInfoActivity) : Fragment() {
    private lateinit var name: EditText
    private lateinit var bio: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        name = view.findViewById(R.id.et_name)
        bio = view.findViewById(R.id.et_bio)
        parent.headerText.text = "Add details"

        name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                parent.data["name"] = s.toString()
            }
        })
        bio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                parent.data["bio"] = s.toString()
            }
        })
        return view
    }
}