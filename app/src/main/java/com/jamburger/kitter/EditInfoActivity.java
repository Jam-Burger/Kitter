package com.jamburger.kitter;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditInfoActivity extends AppCompatActivity {
    FirebaseUser user;
    DocumentReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseFirestore.getInstance().document(user.getUid());
    }
}