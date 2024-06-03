package com.jamburger.kitter.services

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.jamburger.kitter.R
import kotlinx.coroutines.tasks.await


object AuthService {
    private val mAuth = FirebaseAuth.getInstance()
    val auth: FirebaseAuth get() = mAuth
    suspend fun signInWithGoogle(
        context: Context,
    ) {
        val credentialManager = CredentialManager.create(context)

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        ).build()

        val request =
            GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

        val credential: Credential = credentialManager.getCredential(context, request).credential
        val googleIdTokenCredential: GoogleIdTokenCredential =
            GoogleIdTokenCredential.createFrom(credential.data)
        val token: AuthCredential =
            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        mAuth.signInWithCredential(token).await()
    }

    suspend fun signInWithEmailAndPassword(
        email: String, password: String
    ) {
        mAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmailAndPassword(
        email: String, password: String
    ) {
        mAuth.createUserWithEmailAndPassword(email, password).await()
        sendEmailVerification()
    }

    suspend fun sendEmailVerification() {
        mAuth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        val request = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(request)
        mAuth.signOut()
    }
}