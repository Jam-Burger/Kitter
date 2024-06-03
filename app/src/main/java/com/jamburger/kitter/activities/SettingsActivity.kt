package com.jamburger.kitter.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jamburger.kitter.R
import com.jamburger.kitter.components.User
import com.jamburger.kitter.services.AuthService
import com.jamburger.kitter.utilities.Constants
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsList: ViewGroup
    private lateinit var googleSignInClient: GoogleSignInClient
    private var userReference: DocumentReference? = null
    private lateinit var editor: SharedPreferences.Editor
    private var isDarkModeOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        userReference = FirebaseFirestore.getInstance().collection("Users")
            .document(AuthService.auth.uid!!)

        settingsList = findViewById(R.id.settings_list)

        updateUserData()
        updateThemeOption()
        setOptionCLickListener()
        findViewById<View>(R.id.btn_close).setOnClickListener { finish() }
    }

    private fun updateUserData() {
        userReference!!.get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val user = task.result.toObject<User>()!!
                (findViewById<View>(R.id.txt_account_privacy) as TextView).text =
                    if (user.isPrivate) "Private" else "Public"
                (findViewById<View>(R.id.txt_blocked_count) as TextView).text =
                    user.blockedAccounts.size.toString()
            }
        }
    }

    private fun updateThemeOption() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true)
        editor = sharedPreferences.edit()

        val themeOption = findViewById<TextView>(R.id.setting_theme)
        if (isDarkModeOn) {
            themeOption.text = "Dark mode"
            themeOption.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_dark,
                0,
                R.drawable.ic_next,
                0
            )
        } else {
            themeOption.text = "Light mode"
            themeOption.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_light,
                0,
                R.drawable.ic_next,
                0
            )
        }
    }


    private fun setOptionCLickListener() {
        for (i in 0 until settingsList.childCount) {
            val option = settingsList.getChildAt(i)
            if (!option.isClickable) continue

            option.setOnClickListener {
                val optionId = option.id
                when (optionId) {
                    R.id.setting_edit_info -> {
                        startActivity(Intent(this, EditInfoActivity::class.java))
                    }

                    R.id.setting_change_password -> {
                        showRecoverPasswordDialog()
                    }

                    R.id.setting_block_accounts -> {
                        // Do nothing for now
                    }

                    R.id.setting_theme -> {
                        val intentMine = Intent(this, SettingsActivity::class.java)
                        intentMine.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        finish()
                        startActivity(intentMine)
                        if (isDarkModeOn) {
                            editor.putBoolean("isDarkModeOn", false).apply()
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        } else {
                            editor.putBoolean("isDarkModeOn", true).apply()
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }
                    }

                    R.id.setting_add_account -> {
                        // Do nothing for now
                    }

                    R.id.setting_logout -> {
                        lifecycleScope.launch {
                            try {
                                AuthService.signOut(this@SettingsActivity)
                                val intentLogin =
                                    Intent(this@SettingsActivity, LoginActivity::class.java)
                                intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                finish()
                                startActivity(intentLogin)
                            } catch (e: Exception) {
                                Log.e(Constants.TAG, e.message.toString())
                            }
                        }
                    }

                    else -> {
                        Log.i(Constants.TAG, "nothing selected")
                    }
                }
            }
        }
    }

    private fun showRecoverPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Password")
        val email = AuthService.auth.currentUser!!.email

        val linearLayout = LinearLayout(this)
        val emailEt = TextView(this)
        val txt = "We will send change password link to :\n$email"
        emailEt.text = txt
        emailEt.minEms = 16
        linearLayout.setPadding(50, 20, 50, 0)
        linearLayout.addView(emailEt)
        builder.setView(linearLayout)

        builder.setPositiveButton("Confirm") { _: DialogInterface?, _: Int ->
            beginRecovery(email)
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.create().show()
    }

    private fun beginRecovery(email: String?) {
        val loadingBar = ProgressDialog(this)
        loadingBar.setMessage("Sending Email....")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()

        AuthService.auth.sendPasswordResetEmail(email!!)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Link sent on $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
                loadingBar.dismiss()
            }
    }
}