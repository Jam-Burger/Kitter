package com.jamburger.kitter.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.AddInfoActivity

class ProfileImageFragment(var parent: AddInfoActivity) : Fragment() {
    private lateinit var chooseButton: Button
    private lateinit var profileImage: ImageView
    private var myActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    val uri = data.data
                    Glide.with(this).load(uri).into(profileImage)
                    parent.profileImageUri = uri!!
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_image, container, false)

        chooseButton = view.findViewById(R.id.btn_choose)
        profileImage = view.findViewById(R.id.img_profile)
        parent.headerText.text = "Choose a profile image"
        Glide.with(requireContext())
            .load(resources.getString(R.string.default_profile_img_url))
            .into(profileImage)
        chooseButton.setOnClickListener {
            selectImage()
        }
        return view
    }

    private fun selectImage() {
        val intent = Intent()
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }
}