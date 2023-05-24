package com.jamburger.kitter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button loginButton, googleButton, forgetPasswordButton;
    TextView signupText;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    CollectionReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        googleButton = findViewById(R.id.btn_google);
        forgetPasswordButton = findViewById(R.id.btn_forget_password);
        signupText = findViewById(R.id.txt_signup);

        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection("Users");
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this
                , googleSignInOptions);


        loginButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            login(strEmail, strPassword);
        });

        signupText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupEmailActivity.class)));
        googleButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, 100);
        });
        forgetPasswordButton.setOnClickListener(view -> {
            showRecoverPasswordDialog();
        });
    }

    void login(String strEmail, String strPassword) {
        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser().isEmailVerified()) {
                    DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + auth.getUid());
                    userReference.get().addOnCompleteListener(task0 -> {
                        if (task0.isSuccessful()) {
                            User user = task0.getResult().toObject(User.class);
                            if (user == null) {
                                user = new User(auth.getUid(), "", "", email.getText().toString(), "", getResources().getString(R.string.default_profile_img_url), getResources().getString(R.string.default_background_img_url));
                                userReference.set(user).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, auth.getUid(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                startAddInfoActivity();
                            } else {
                                if (user.getUsername().isEmpty()) {
                                    startAddInfoActivity();
                                } else {
                                    startMainActivity();
                                }
                            }
                        } else {
                            Toast.makeText(this, task0.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Verify your email first\nLink sent to " + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    auth.getCurrentUser().sendEmailVerification();
                }
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);

        emailEt.setText(email.getText());
        emailEt.setMinEms(14);
        emailEt.setHint("E-mail");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(30, 20, 30, 10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", (dialog, which) -> {
            String email = emailEt.getText().toString().trim();
            if (!email.isEmpty()) beginRecovery(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void beginRecovery(String email) {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            loadingBar.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Recovery email sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {
                Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask
                            .getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        , null);
                        auth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        usersReference.document(auth.getUid()).get().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                User user = task1.getResult().toObject(User.class);
                                                if (user != null) {
                                                    startMainActivity();
                                                } else {
                                                    user = new User(auth.getUid(), "", "", googleSignInAccount.getEmail(), "", getResources().getString(R.string.default_profile_img_url), getResources().getString(R.string.default_background_img_url));
                                                    usersReference.document(auth.getUid()).set(user);
                                                    startAddInfoActivity();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "Authentication Failed :" +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                } catch (ApiException e) {
                    Log.e("ApiException", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.e("signInAccountTask", signInAccountTask.getException().getMessage());
                signInAccountTask.getException().printStackTrace();
            }
        }
    }

    private void startAddInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, AddInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}