package com.jamburger.kitter.activities;

import static com.jamburger.kitter.utilities.Constants.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.jamburger.kitter.utilities.ForceUpdateChecker;
import com.jamburger.kitter.utilities.PermissionManager;

public class StartActivity extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener {
    GoogleSignInClient gsc;
    ImageView logo;
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        PermissionManager.askPermissions(this);

        logo = findViewById(R.id.img_logo);
        appName = findViewById(R.id.txt_appname);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true);
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        letTheShitBegin();
    }


    private void letTheShitBegin() {
        Animation logoAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_animation);
        Animation appNameAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_name_animation);
        logo.startAnimation(logoAnimation);
        appName.startAnimation(appNameAnimation);
        appNameAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                boolean required = ForceUpdateChecker.with(StartActivity.this).onUpdateNeeded(StartActivity.this).check();
                if (required) return;
                gsc = GoogleSignIn.getClient(StartActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null || !auth.getCurrentUser().isEmailVerified()) {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + auth.getUid());
                    try {
                        userReference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);
                                Intent intent;
                                if (user != null) {
                                    if (user.getUsername().isEmpty()) {
                                        intent = new Intent(StartActivity.this, AddInfoActivity.class);
                                    } else {
                                        intent = new Intent(StartActivity.this, MainActivity.class);
                                    }
                                } else {
                                    Toast.makeText(StartActivity.this, "user is deleted on firestore", Toast.LENGTH_SHORT).show();
                                    intent = new Intent(StartActivity.this, LoginActivity.class);
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("signin", task.getException().getMessage());
                                FirebaseAuth.getInstance().signOut();
                                gsc.signOut();
                            }
                        });
                    } catch (Exception e) {
                        Log.d(TAG, "onAnimationEnd: " + e);
                        Toast.makeText(StartActivity.this, "something's wrong again", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        gsc.signOut();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onUpdateNeeded(String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setCancelable(false)
                .setPositiveButton("Update",
                        (dialog1, which) -> redirectStore(updateUrl)).setNegativeButton("No, thanks",
                        (dialog12, which) -> finish()).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}