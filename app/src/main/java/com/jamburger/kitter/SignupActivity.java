package com.jamburger.kitter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    EditText email, name, username, password;
    Button signupButton;
    DatabaseReference dbRef;
    FirebaseAuth auth;
    ProgressDialog pd;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.et_email);
        name = findViewById(R.id.et_name);
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        signupButton = findViewById(R.id.btn_signup);

        dbRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        pd= new ProgressDialog(this);
        signupButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strName = name.getText().toString();
            String strUsername = username.getText().toString();
            String strPassword = password.getText().toString();
            if (validate(strEmail, strName, strUsername, strPassword)) {
                signupWithEmail(strEmail, strName, strUsername, strPassword);
            } else {
                Toast.makeText(this, "Enter all details properly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate(String strEmail, String strName, String strUsername, String strPassword) {
        return !strEmail.isEmpty() && !strName.isEmpty() && !strPassword.isEmpty() && strPassword.length() >= 6 && !strUsername.isEmpty();
    }

    private void signupWithEmail(String strEmail, String strName, String strUsername, String strPassword) {
        pd.setMessage("Please Wait");
        pd.show();
        auth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnSuccessListener(authResult -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", strName);
                    map.put("email", strEmail);
                    map.put("username", strUsername);
                    map.put("id", auth.getCurrentUser().getUid());

                    dbRef.child("Users").child(auth.getCurrentUser().getUid()).setValue(map)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    pd.dismiss();
                                    finish();
                                }
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                });

    }
}