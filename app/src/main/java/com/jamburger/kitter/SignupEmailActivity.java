package com.jamburger.kitter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;

public class SignupEmailActivity extends AppCompatActivity {
    EditText email, password, confirmPassword;
    Button signupButton;
    FirebaseFirestore db;
    FirebaseAuth auth;
    ProgressDialog pd;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_email);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        signupButton = findViewById(R.id.btn_signup);

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        signupButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            String strConfirmPassword = confirmPassword.getText().toString();
            if (validate(strEmail, strPassword, strConfirmPassword)) {
                signupWithEmail(strEmail, strPassword);
            } else {
                Toast.makeText(this, "Enter all details properly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate(String strEmail, String strPassword, String strConfirmPassword) {
        return !strEmail.isEmpty() && !strPassword.isEmpty() && strPassword.length() >= 6 && strPassword.equals(strConfirmPassword);
    }

    private void signupWithEmail(String strEmail, String strPassword) {
        pd.setMessage("Please Wait");
        pd.show();
        auth.createUserWithEmailAndPassword(strEmail, strPassword).addOnSuccessListener(authResult -> {
            User user = new User(auth.getCurrentUser().getUid(), "", "", strEmail, strPassword, getResources().getString(R.string.default_profile_img_url), getResources().getString(R.string.default_background_img_url));
            db.collection("Users").document(user.getId()).set(user).addOnSuccessListener(result -> {
                Intent intent = new Intent(SignupEmailActivity.this, AddInfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                pd.dismiss();
                finish();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(SignupEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            pd.dismiss();
        });

    }
}