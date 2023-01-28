package com.jamburger.kitter;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.components.User;

public class ChangePasswordActivity extends AppCompatActivity {
    ImageView savePasswordButton;
    Button forgetPasswordButton;
    EditText currentPassword, newPassword, confirmNewPassword;
    DocumentReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        savePasswordButton = findViewById(R.id.btn_save_password);
        forgetPasswordButton = findViewById(R.id.btn_forget_password);
        currentPassword = findViewById(R.id.et_current_password);
        newPassword = findViewById(R.id.et_new_password);
        confirmNewPassword = findViewById(R.id.et_confirm_new_password);

        userReference = FirebaseFirestore.getInstance().document("Users/" + FirebaseAuth.getInstance().getUid());
        User user = userReference.get().getResult().toObject(User.class);

        savePasswordButton.setOnClickListener(view -> {
            //TODO:complete this shit
        });
    }
}