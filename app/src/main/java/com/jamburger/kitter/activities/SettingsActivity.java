package com.jamburger.kitter.activities;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.jamburger.kitter.R;

public class SettingsActivity extends AppCompatActivity {
    ViewGroup settingsList;
    GoogleSignInClient googleSignInClient;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true);

        settingsList = findViewById(R.id.settings_list);

        TextView themeItem = findViewById(R.id.setting_theme);
        if (isDarkModeOn) {
            themeItem.setText("Dark Mode");
            themeItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dark, 0, 0, 0);
        } else {
            themeItem.setText("Light Mode");
            themeItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_light, 0, 0, 0);
        }

        for (int i = 0; i < settingsList.getChildCount(); i++) {
            View option = settingsList.getChildAt(i);
            if (!option.isClickable())

                option.setOnClickListener(v -> {
                    switch (option.getId()) {
                        case R.id.setting_edit_info:
                            startActivity(new Intent(this, EditInfoActivity.class));
                            break;
                        case R.id.setting_change_password:
                            showRecoverPasswordDialog();
                            break;
                        case R.id.setting_block_accounts:
                            break;
                        case R.id.setting_theme:
                            Intent intentMine = new Intent(this, SettingsActivity.class);
                            intentMine.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            finish();
                            startActivity(intentMine);
                            if (isDarkModeOn) {
                                editor.putBoolean("isDarkModeOn", false).apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            } else {
                                editor.putBoolean("isDarkModeOn", true).apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            }
                            break;
                        case R.id.setting_new_account:
                            break;
                        case R.id.setting_logout:
                            googleSignInClient.signOut().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intentLogin = new Intent(this, LoginActivity.class);
                                    intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intentLogin);
                                    finish();
                                }
                            });
                            break;
                        default:
                            Log.i(TAG, "nothing selected");
                    }
                });
        }

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        final LinearLayout linearLayout = new LinearLayout(this);
        final TextView emailEt = new TextView(this);
        final String txt = "We will send change password link to :\n" + email;
        emailEt.setText(txt);
        emailEt.setMinEms(16);
        linearLayout.setPadding(50, 20, 50, 0);
        linearLayout.addView(emailEt);
        builder.setView(linearLayout);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            beginRecovery(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void beginRecovery(String email) {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Link sent on " + email, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        });
    }
}