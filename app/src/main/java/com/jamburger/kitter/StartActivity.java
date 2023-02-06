package com.jamburger.kitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;

public class StartActivity extends AppCompatActivity {
    GoogleSignInClient gsc;
    ImageView logo;
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
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
                gsc = GoogleSignIn.getClient(StartActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + auth.getUid());
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
                                auth.signOut();
                                gsc.signOut();
                                intent = new Intent(StartActivity.this, LoginActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}