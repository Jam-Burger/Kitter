package com.jamburger.kitter.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jamburger.kitter.R
import com.jamburger.kitter.activities.SavedPostsActivity
import com.jamburger.kitter.activities.SettingsActivity
import com.jamburger.kitter.components.User
import com.jamburger.kitter.utilities.ProfilePageManager

class ProfileFragment : Fragment() {
    private var userReference: DocumentReference? = null
    private lateinit var toolbar: Toolbar
    private lateinit var backgroundImageEditButton: ImageView
    private var db: FirebaseFirestore? = null
    private var user: FirebaseUser? = null
    private lateinit var profilePageManager: ProfilePageManager

    private var myActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    val uri = data.data
                    Glide.with(this).load(uri).into(profilePageManager.backgroundImage)
                    postImage(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser
        db = FirebaseFirestore.getInstance()
        userReference = db!!.collection("Users").document(user!!.uid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profilePageManager = ProfilePageManager(view)

        backgroundImageEditButton = view.findViewById(R.id.btn_change_background_img)
        toolbar = view.findViewById(R.id.top_menu)

        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.nav_saved) {
                startActivity(Intent(requireActivity(), SavedPostsActivity::class.java))
            } else if (itemId == R.id.nav_settings) {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            }
            true
        }

        fillUserData()
        readPosts()

        backgroundImageEditButton.setOnClickListener {
            selectImage()
        }

        return view
    }

    private fun postImage(filePath: Uri?) {
        if (filePath != null) {
            val storageReference = FirebaseStorage.getInstance().reference
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val postId = user!!.uid
            val ref = storageReference.child("Background Pictures/$postId")
            ref.putFile(filePath).addOnSuccessListener {
                Toast.makeText(requireActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show()
                storageReference.child("Background Pictures")
                    .child(postId).downloadUrl.addOnSuccessListener { uri: Uri ->
                        db!!.collection("Users").document(
                            user!!.uid
                        ).update("backgroundImageUrl", uri.toString())
                            .addOnCompleteListener {
                                progressDialog.dismiss()
                            }
                    }.addOnFailureListener { progressDialog.dismiss() }
            }.addOnFailureListener { e: Exception ->
                progressDialog.dismiss()
                Toast.makeText(activity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val progress =
                    ((100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount))
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            }
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun fillUserData() {
        userReference!!.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
            val user = documentSnapshot.toObject<User>()!!
            profilePageManager.fillUserData(user)
        }
    }

    private fun readPosts() {
        userReference?.let { profilePageManager.readPosts(it) }
    }
}