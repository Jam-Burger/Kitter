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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.User;

public class SettingsActivity extends AppCompatActivity {
    ViewGroup settingsList;
    GoogleSignInClient googleSignInClient;
    DocumentReference userReference;
    SharedPreferences.Editor editor;
    boolean isDarkModeOn;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        userReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());

        settingsList = findViewById(R.id.settings_list);

        updateUserData();
        updateThemeOption();
        setOptionCLickListener();
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    private void updateUserData() {
        userReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                assert user != null;
                ((TextView) findViewById(R.id.txt_account_privacy)).setText(user.isPrivate() ? "Private" : "Public");
                ((TextView) findViewById(R.id.txt_blocked_count)).setText(String.valueOf(user.getBlockedAccounts().size()));
            }
        });
    }

    private void updateThemeOption() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true);
        editor = sharedPreferences.edit();

        TextView themeOption = findViewById(R.id.setting_theme);
        if (isDarkModeOn) {
            themeOption.setText("Dark mode");
            themeOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dark, 0, R.drawable.ic_next, 0);
        } else {
            themeOption.setText("Light mode");
            themeOption.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_light, 0, R.drawable.ic_next, 0);
        }
    }


    void setOptionCLickListener() {
        for (int i = 0; i < settingsList.getChildCount(); i++) {
            View option = settingsList.getChildAt(i);
            if (!option.isClickable()) continue;

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
                    case R.id.setting_add_account:
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