package com.jamburger.kitter.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User

class EditInfoActivity : AppCompatActivity() {
    private var user: User? = null
    private lateinit var userReference: DocumentReference
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText
    private lateinit var profileImage: ImageView
    private var profileImageUri: Uri? = null
    private lateinit var data: HashMap<String, Any>
    private lateinit var saveInfoButton: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var profileImageEditButton: ImageView
    private var valid: Boolean = false
    private lateinit var userNames: MutableList<String>

    private var myActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    profileImageUri = data.data
                    Glide.with(this).load(profileImageUri).into(profileImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)

        profileImage = findViewById(R.id.img_profile)
        name = findViewById(R.id.et_name)
        username = findViewById(R.id.et_username)
        bio = findViewById(R.id.et_bio)
        saveInfoButton = findViewById(R.id.btn_save_info)
        closeButton = findViewById(R.id.btn_close)
        profileImageEditButton = findViewById(R.id.btn_change_profile_img)

        fillUserData()

        profileImageEditButton.setOnClickListener { selectImage() }
        closeButton.setOnClickListener { finish() }

        userNames = ArrayList()
        FirebaseFirestore.getInstance().collection("Users").get()
            .addOnSuccessListener { userSnapshots: QuerySnapshot ->
                for (userSnapshot in userSnapshots) {
                    val user = userSnapshot.toObject<User>()
                    userNames.add(user.username)
                }
            }
        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                valid = true
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().matches("^\\w+$".toRegex()) || s.length < 3 || s.length > 15) {
                    valid = false
                }
                for (name in userNames) {
                    if (name != user!!.username && name == s.toString()) {
                        valid = false
                        break
                    }
                }
                if (!valid) {
                    username.setTextColor(resources.getColor(R.color.like, theme))
                    saveInfoButton.isClickable = false
                } else {
                    username.setTextColor(resources.getColor(R.color.inverted, theme))
                    saveInfoButton.isClickable = true
                }
            }
        })

        saveInfoButton.setOnClickListener {
            data["name"] = name.getText().toString()
            data["username"] = username.getText().toString()
            data["bio"] = bio.getText().toString()
            if (profileImageUri != null) updateImageAndData()
            else updateData()
        }
    }

    private fun updateData() {
        val request = UserProfileChangeRequest.Builder().setDisplayName(data["name"].toString())
            .setPhotoUri(profileImageUri).build()
        FirebaseAuth.getInstance().currentUser!!.updateProfile(request)
        userReference.update(data).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                val intentMain = Intent(this, MainActivity::class.java)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intentMain.putExtra("page", "PROFILE")
                startActivity(intentMain)
                finish()
            }
        }
    }

    private fun fillUserData() {
        userReference = FirebaseFirestore.getInstance().collection("Users")
            .document(FirebaseAuth.getInstance().uid!!)
        userReference.get().addOnSuccessListener { snapshot: DocumentSnapshot ->
            user = snapshot.toObject<User>()
            username.setText(user!!.username)
            name.setText(user!!.name)
            bio.setText(user!!.bio)
            Glide.with(this).load(user!!.profileImageUrl).into(profileImage)
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun updateImageAndData() {
        val storageReference = FirebaseStorage.getInstance().reference

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Saving Profile...")
        progressDialog.show()

        val postId = user!!.id
        val ref = storageReference.child("Profile Pictures/$postId")

        ref.putFile(profileImageUri!!).addOnSuccessListener {
            Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show()
            storageReference.child("Profile Pictures/")
                .child(postId).downloadUrl.addOnSuccessListener { uri: Uri ->
                    data["profileImageUrl"] = uri.toString()
                    updateData()
                    progressDialog.dismiss()
                }.addOnFailureListener { progressDialog.dismiss() }
        }.addOnFailureListener { e: Exception ->
            progressDialog.dismiss()
            Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
            val progress = ((100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount))
            progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
        }
    }
}