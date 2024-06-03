package com.jamburger.kitter.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User
import com.jamburger.kitter.utilities.Constants
import com.jamburger.kitter.utilities.ForceUpdateChecker
import com.jamburger.kitter.utilities.ForceUpdateChecker.OnUpdateNeededListener
import com.jamburger.kitter.utilities.PermissionManager

class StartActivity : AppCompatActivity(), OnUpdateNeededListener {
    private var gsc: GoogleSignInClient? = null
    private var logo: ImageView? = null
    private var appName: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        PermissionManager.askPermissions(this)

        logo = findViewById(R.id.img_logo)
        appName = findViewById(R.id.txt_appname)

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

        val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true)
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        letTheShitBegin()
    }


    private fun letTheShitBegin() {
        val logoAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.logo_animation)
        val appNameAnimation = AnimationUtils.loadAnimation(
            applicationContext, R.anim.app_name_animation
        )
        logo!!.startAnimation(logoAnimation)
        appName!!.startAnimation(appNameAnimation)
        appNameAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                val required =
                    ForceUpdateChecker.with(this@StartActivity).onUpdateNeeded(this@StartActivity)
                        .check()
                if (required) return
                gsc =
                    GoogleSignIn.getClient(this@StartActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null || !auth.currentUser!!.isEmailVerified) {
                    val intent = Intent(this@StartActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else {
                    val userReference =
                        FirebaseFirestore.getInstance().document("Users/" + auth.uid)
                    try {
                        userReference.get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                            if (task.isSuccessful) {
                                val user = task.result.toObject<User>()
                                val intent: Intent
                                if (user != null) {
                                    intent = if (user.username.isEmpty()) {
                                        Intent(this@StartActivity, AddInfoActivity::class.java)
                                    } else {
                                        Intent(this@StartActivity, MainActivity::class.java)
                                    }
                                } else {
                                    Toast.makeText(
                                        this@StartActivity,
                                        "user is deleted on firestore",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    intent = Intent(this@StartActivity, LoginActivity::class.java)
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("signin", task.exception!!.message!!)
                                FirebaseAuth.getInstance().signOut()
                                gsc!!.signOut()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.TAG, "onAnimationEnd: $e")
                        Toast.makeText(
                            this@StartActivity,
                            "something's wrong again",
                            Toast.LENGTH_SHORT
                        ).show()
                        FirebaseAuth.getInstance().signOut()
                        gsc!!.signOut()
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
    }

    override fun onUpdateNeeded(updateUrl: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("New version available")
            .setMessage("Please, update app to new version to continue reposting.")
            .setCancelable(false)
            .setPositiveButton(
                "Update"
            ) { _: DialogInterface?, _: Int -> redirectStore(updateUrl) }
            .setNegativeButton(
                "No, thanks"
            ) { _: DialogInterface?, _: Int -> finish() }.create()
        dialog.show()
    }

    private fun redirectStore(updateUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}