package com.jamburger.kitter.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jamburger.kitter.R
import com.jamburger.kitter.fragments.DetailsFragment
import com.jamburger.kitter.fragments.ProfileImageFragment
import com.jamburger.kitter.fragments.UsernameFragment

class AddInfoActivity : AppCompatActivity() {
    lateinit var nextButton: ImageView
    lateinit var data: HashMap<String, Any>
    lateinit var profileImageUri: Uri
    lateinit var headerText: TextView
    private lateinit var current: Fragments
    var userReference: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_info)
        userReference = FirebaseFirestore.getInstance().collection("Users")
            .document(FirebaseAuth.getInstance().uid!!)
        nextButton = findViewById(R.id.btn_next)
        headerText = findViewById(R.id.txt_header)

        data = HashMap()
        current = Fragments.USERNAME
        supportFragmentManager.beginTransaction().replace(
            R.id.frame_container, UsernameFragment(
                this
            )
        ).commit()
        nextButton.setOnClickListener {
            nextFragment()
        }
    }

    private fun nextFragment() {
        when (current) {
            Fragments.USERNAME -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.frame_container, ProfileImageFragment(
                        this
                    )
                ).commit()
                current = Fragments.PROFILE_IMAGE
            }

            Fragments.PROFILE_IMAGE -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.frame_container, DetailsFragment(
                        this
                    )
                ).commit()
                current = Fragments.DETAILS
            }

            Fragments.DETAILS -> {
                updateDataWithImage()
            }
        }
    }

    private fun updateData() {
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(data["name"].toString())
            .setPhotoUri(profileImageUri)
            .build()
        FirebaseAuth.getInstance().currentUser!!.updateProfile(request)

        userReference!!.update(data).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateDataWithImage() {
        val storageReference = FirebaseStorage.getInstance().reference
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Saving Profile...")
        progressDialog.show()

        val postId = userReference!!.id
        val ref = storageReference.child("Profile Pictures/$postId")

        ref.putFile(profileImageUri)
            .addOnCompleteListener { task0: Task<UploadTask.TaskSnapshot?> ->
                if (task0.isSuccessful) {
                    storageReference.child("Profile Pictures/")
                        .child(postId).downloadUrl.addOnCompleteListener { task: Task<Uri> ->
                            if (task.isSuccessful) {
                                data["profileImageUrl"] = task.result.toString()
                                updateData()
                            }
                            progressDialog.dismiss()
                        }
                } else {
                    Toast.makeText(this, "Failed " + task0.exception!!.message, Toast.LENGTH_SHORT)
                        .show()
                    progressDialog.dismiss()
                }
            }.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val progress =
                    ((100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount))
                progressDialog.setMessage("Saving " + progress.toInt() + "%")
            }
    }

    enum class Fragments {
        USERNAME, PROFILE_IMAGE, DETAILS
    }
}