package com.jamburger.kitter.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.jamburger.kitter.R
import com.jamburger.kitter.components.Post
import com.jamburger.kitter.components.User
import com.jamburger.kitter.fragments.SelectSourceDialogFragment
import com.jamburger.kitter.utilities.DateTimeFormatter
import java.io.File
import java.io.IOException

class PostActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var caption: TextView? = null
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null
    private var db: FirebaseFirestore? = null
    private var user: FirebaseUser? = null
    private var currentPhotoPath: String? = null
    private var fromGalleryResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    filePath = data.data
                    showActivity(true)
                    Glide.with(this).load(filePath).into(imageView!!)
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                startMainActivity()
            }
        }
    private var fromCameraResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                showActivity(true)
                Glide.with(this).load(filePath).into(imageView!!)
            } else if (result.resultCode == RESULT_CANCELED) {
                startMainActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        imageView = findViewById(R.id.post_img)
        caption = findViewById(R.id.et_caption)

        val closeButton = findViewById<ImageView>(R.id.btn_close)
        val postButton = findViewById<ImageView>(R.id.btn_post)

        user = FirebaseAuth.getInstance().currentUser
        storageReference = FirebaseStorage.getInstance().reference
        db = FirebaseFirestore.getInstance()

        closeButton.setOnClickListener {
            startMainActivity()
        }
        postButton.setOnClickListener {
            closeKeyboard()
            publishPost()
        }

        val type = intent.getStringExtra("type")

        if (type == "picture") {
            showDialog()
            showActivity(false)
        } else {
            val captionLayout = findViewById<TextInputLayout>(R.id.layout_caption)
            captionLayout.hint = "Enter text content"
        }
    }

    private fun showDialog() {
        val newFragment = SelectSourceDialogFragment.newInstance()
        newFragment.show(supportFragmentManager, "dialog")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = DateTimeFormatter.getCurrentTime()
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        currentPhotoPath = image.absolutePath
        return image
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                filePath = FileProvider.getUriForFile(
                    this,
                    "com.jamburger.kitter.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
                fromCameraResultLauncher.launch(
                    Intent.createChooser(
                        takePictureIntent,
                        "Select Picture"
                    )
                )
            }
        }
    }

    private fun publishPost() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val postId = DateTimeFormatter.getCurrentTime()
            val ref = storageReference!!.child("Posts/$postId")

            ref.putFile(filePath!!).addOnSuccessListener {
                Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show()
                storageReference!!.child("Posts")
                    .child(postId).downloadUrl.addOnSuccessListener { uri: Uri ->
                        val post =
                            Post(user!!.uid, postId, uri.toString(), caption!!.text.toString())
                        val postRef = db!!.collection("Posts").document(postId)

                        postRef.set(post).addOnCompleteListener {
                            progressDialog.dismiss()
                            startMainActivity()
                        }
                        updateUserPostsAndFeed(postRef)
                    }
            }.addOnFailureListener { e: Exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val progress =
                    ((100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount))
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            }
        } else {
            val postId = DateTimeFormatter.getCurrentTime()
            val post = Post(user!!.uid, postId, "", "")
            post.kitt = caption!!.text.toString()
            val postRef = db!!.collection("Posts").document(postId)
            postRef.set(post).addOnCompleteListener {
                startMainActivity()
            }
            updateUserPostsAndFeed(postRef)
        }
    }

    private fun updateUserPostsAndFeed(postReference: DocumentReference) {
        val userReference = db!!.collection("Users").document(
            user!!.uid
        )
        userReference.update("posts", FieldValue.arrayUnion(postReference))

        val map: MutableMap<String, Any> = HashMap()
        map["postReference"] = postReference
        map["visited"] = false
        userReference.collection("feed").document(postReference.id).set(map)
        userReference.get().addOnSuccessListener { userSnapshot: DocumentSnapshot ->
            val me = userSnapshot.toObject(
                User::class.java
            )!!
            for (follower in me.followers) {
                follower.collection("feed").document(postReference.id).set(map)
            }
        }
    }

    fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun selectPicture() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        fromGalleryResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun showActivity(set: Boolean) {
        if (set) findViewById<View>(R.id.post_parent).visibility = View.VISIBLE
        else findViewById<View>(R.id.post_parent).visibility = View.INVISIBLE
    }

    fun selectFromCamera() {
        takePicture()
    }

    fun selectFromGallery() {
        selectPicture()
    }
}