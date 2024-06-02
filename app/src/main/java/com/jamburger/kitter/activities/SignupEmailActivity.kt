package com.jamburger.kitter.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jamburger.kitter.R

class SignupEmailActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signupButton: Button
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_email)

        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        confirmPassword = findViewById(R.id.et_confirm_password)
        signupButton = findViewById(R.id.btn_signup)

        db = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()
        pd = ProgressDialog(this)
        signupButton.setOnClickListener {
            val strEmail = email.getText().toString()
            val strPassword = password.getText().toString()
            val strConfirmPassword = confirmPassword.getText().toString()
            if (validate(strEmail, strPassword, strConfirmPassword)) {
                signupWithEmail(strEmail, strPassword)
            } else {
                Toast.makeText(this, "Enter all details properly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validate(
        strEmail: String,
        strPassword: String,
        strConfirmPassword: String
    ): Boolean {
        return strEmail.isNotEmpty() && strPassword.isNotEmpty() && strPassword.length >= 6 && strPassword == strConfirmPassword
    }

    private fun signupWithEmail(strEmail: String, strPassword: String) {
        pd!!.setMessage("Please Wait")
        pd!!.show()
        auth!!.createUserWithEmailAndPassword(strEmail, strPassword)
            .addOnSuccessListener { authResult: AuthResult ->
                authResult.user!!.sendEmailVerification().addOnSuccessListener {
                    Toast.makeText(
                        this@SignupEmailActivity, "Verification mail sent to " + authResult.user!!
                            .email, Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
    }
}