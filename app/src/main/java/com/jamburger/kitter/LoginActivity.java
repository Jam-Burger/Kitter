package com.jamburger.kitter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button loginButton;
    TextView signupText;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        signupText = findViewById(R.id.txt_signup);
        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            login(strEmail, strPassword);
        });
        signupText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    void login(String strEmail, String strPassword) {
        auth.signInWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}