package com.jamburger.kitter.services;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthService {
    private final FirebaseAuth mAuth;

    public FirebaseAuthService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUp(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public void signOut() {
        mAuth.signOut();
    }

    // Additional methods for password reset, profile update, etc.
}

