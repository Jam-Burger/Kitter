package com.jamburger.kitter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jamburger.kitter.fragments.DetailsFragment;
import com.jamburger.kitter.fragments.ProfileImageFragment;
import com.jamburger.kitter.fragments.UsernameFragment;

import java.util.HashMap;

public class AddInfoActivity extends AppCompatActivity {

    public static Fragments current;
    public static HashMap<String, Object> data;
    ImageView nextButton;
    DocumentReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info);
        userReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid());
        nextButton = findViewById(R.id.btn_next);
        data = new HashMap<>();
        current = Fragments.USERNAME;
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new UsernameFragment()).commit();
        nextButton.setOnClickListener(view -> {
            changeFragment();
        });
    }

    public void changeFragment() {
        if (current == Fragments.USERNAME) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ProfileImageFragment(this)).commit();
            current = Fragments.PROFILE_IMAGE;
        } else if (current == Fragments.PROFILE_IMAGE) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new DetailsFragment()).commit();
            current = Fragments.DETAILS;
        } else if (current == Fragments.DETAILS) {
            userReference.update(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    public enum Fragments {USERNAME, PROFILE_IMAGE, DETAILS}
}