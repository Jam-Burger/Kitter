package com.jamburger.kitter.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User
import com.jamburger.kitter.services.AuthService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var googleButton: Button
    private lateinit var forgetPasswordButton: Button
    private lateinit var signupText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.btn_login)
        googleButton = findViewById(R.id.btn_google)
        forgetPasswordButton = findViewById(R.id.btn_forget_password)
        signupText = findViewById(R.id.txt_signup)

        auth = AuthService.auth

        usersReference = FirebaseFirestore.getInstance().collection("Users")

        loginButton.setOnClickListener {
            val strEmail = email.getText().toString()
            val strPassword = password.getText().toString()

            lifecycleScope.launch {
                loginWithEmailAndPassword(strEmail, strPassword)
            }
        }

        signupText.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity, SignupActivity::class.java
                )
            )
        }
        googleButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    AuthService.signInWithGoogle(this@LoginActivity)
                    doValidUserShit()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity, e.message, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        forgetPasswordButton.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private suspend fun loginWithEmailAndPassword(strEmail: String, strPassword: String) {
        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            AuthService.signInWithEmailAndPassword(strEmail, strPassword)
            if (auth.currentUser?.isEmailVerified == true) {
                doValidUserShit()
            } else {
                AuthService.sendEmailVerification()
                Toast.makeText(
                    this,
                    "Verify your email first\nLink sent to $strEmail",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this, e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun doValidUserShit() {
        val userReference = FirebaseFirestore.getInstance().document("Users/" + auth.uid)

        userReference.get().addOnCompleteListener { documentTask: Task<DocumentSnapshot> ->
            if (documentTask.isSuccessful) {
                var user = documentTask.result.toObject<User>()
                if (user == null) {
                    // first time on app
                    user = User(
                        auth.uid!!,
                        "",
                        "",
                        auth.currentUser?.email!!,
                        resources.getString(R.string.default_profile_img_url),
                        resources.getString(R.string.default_background_img_url)
                    )
                    userReference.set(user).addOnCompleteListener { task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                            Toast.makeText(this@LoginActivity, auth.uid, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                task1.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    startAddInfoActivity()
                } else {
                    if (user.username.isEmpty()) {
                        // not first time but use is not initialized
                        startAddInfoActivity()
                    } else {
                        // regular scenario
                        startMainActivity()
                    }
                }
            } else {
                Toast.makeText(this, documentTask.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recover Password")
        val linearLayout = LinearLayout(this)
        val emailEt = EditText(this)

        emailEt.text = email.text
        emailEt.minEms = 14
        emailEt.hint = "E-mail"
        emailEt.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        linearLayout.addView(emailEt)
        linearLayout.setPadding(30, 20, 30, 10)
        builder.setView(linearLayout)

        builder.setPositiveButton("Recover") { _: DialogInterface?, _: Int ->
            val email = emailEt.text.toString().trim { it <= ' ' }

            lifecycleScope.launch {
                if (email.isNotEmpty()) beginRecovery(email)
            }
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.create().show()
    }

    private suspend fun beginRecovery(email: String) {
        val loadingBar = ProgressDialog(this)
        loadingBar.setMessage("Sending Email....")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()

        AuthService.sendPasswordResetEmail(email)
    }

    private fun startAddInfoActivity() {
        val intent = Intent(this@LoginActivity, AddInfoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}