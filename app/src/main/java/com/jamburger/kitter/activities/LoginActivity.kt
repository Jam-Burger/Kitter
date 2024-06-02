package com.jamburger.kitter.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User

class LoginActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var googleButton: Button
    private lateinit var forgetPasswordButton: Button
    private lateinit var signupText: TextView
    private var auth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient
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

        auth = FirebaseAuth.getInstance()
        usersReference = FirebaseFirestore.getInstance().collection("Users")
        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(
            this@LoginActivity,
            googleSignInOptions
        )

        loginButton.setOnClickListener(View.OnClickListener { view: View? ->
            val strEmail = email.getText().toString()
            val strPassword = password.getText().toString()
            login(strEmail, strPassword)
        })

        signupText.setOnClickListener(View.OnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@LoginActivity, SignupEmailActivity::class.java
                )
            )
        })
        googleButton.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, 100)
        })
        forgetPasswordButton.setOnClickListener(View.OnClickListener { view: View? ->
            showRecoverPasswordDialog()
        })
    }

    fun login(strEmail: String, strPassword: String) {
        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }
        auth!!.signInWithEmailAndPassword(strEmail, strPassword)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    if (auth!!.currentUser!!.isEmailVerified) {
                        doValidUserShit()
                    } else {
                        Toast.makeText(
                            this, "Verify your email first\nLink sent to " + auth!!.currentUser!!
                                .email, Toast.LENGTH_SHORT
                        ).show()
                        auth!!.currentUser!!.sendEmailVerification()
                    }
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun doValidUserShit() {
        val userReference = FirebaseFirestore.getInstance().document("Users/" + auth!!.uid)

        userReference.get().addOnCompleteListener { task0: Task<DocumentSnapshot> ->
            if (task0.isSuccessful) {
                var user = task0.result.toObject(
                    User::class.java
                )
                if (user == null) {
                    user = User(
                        auth!!.uid,
                        "",
                        "",
                        auth!!.currentUser!!
                            .email,
                        resources.getString(R.string.default_profile_img_url),
                        resources.getString(R.string.default_background_img_url)
                    )
                    userReference.set(user).addOnCompleteListener { task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                            Toast.makeText(this@LoginActivity, auth!!.uid, Toast.LENGTH_SHORT)
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
                        startAddInfoActivity()
                    } else {
                        startMainActivity()
                    }
                }
            } else {
                Toast.makeText(this, task0.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showRecoverPasswordDialog() {
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

        builder.setPositiveButton("Recover") { dialog: DialogInterface?, which: Int ->
            val email = emailEt.text.toString().trim { it <= ' ' }
            if (!email.isEmpty()) beginRecovery(email)
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        builder.create().show()
    }

    private fun beginRecovery(email: String) {
        val loadingBar = ProgressDialog(this)
        loadingBar.setMessage("Sending Email....")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()

        auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task: Task<Void?> ->
            loadingBar.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this@LoginActivity, "Recovery email sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LoginActivity, task.exception!!.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val signInAccountTask = GoogleSignIn
                .getSignedInAccountFromIntent(data)

            if (signInAccountTask.isSuccessful) {
                Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show()
                try {
                    val googleSignInAccount = signInAccountTask
                        .getResult(ApiException::class.java)
                    if (googleSignInAccount != null) {
                        val authCredential = GoogleAuthProvider
                            .getCredential(
                                googleSignInAccount.idToken,
                                null
                            )
                        auth!!.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                                if (task.isSuccessful) {
                                    doValidUserShit()
                                } else {
                                    Toast.makeText(
                                        this, "Authentication Failed :" +
                                                task.exception!!.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } catch (e: ApiException) {
                    Log.e("ApiException", e.message!!)
                    e.printStackTrace()
                }
            } else {
                Log.e("signInAccountTask", signInAccountTask.exception!!.message!!)
                signInAccountTask.exception!!.printStackTrace()
            }
        }
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