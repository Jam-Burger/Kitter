package com.jamburger.kitter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jamburger.kitter.fragments.HomeFragment;
import com.jamburger.kitter.fragments.ProfileFragment;
import com.jamburger.kitter.fragments.SearchFragment;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "com.jamburger.kitter";
    BottomNavigationView bottomNavigationView;
    FirebaseUser user;
    String currentPage;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        user = FirebaseAuth.getInstance().getCurrentUser();

        String page = getIntent().getStringExtra("page");
        if (page != null) {
            if (page.equals("PROFILE")) {
                currentPage = "PROFILE";
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            }
        } else {
            currentPage = "HOME";
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    currentPage = "HOME";
                    break;
                case R.id.nav_search:
                    currentPage = "SEARCH";
                    break;
                case R.id.nav_profile:
                    currentPage = "PROFILE";
                    break;
            }
            startFragment(currentPage);
            return true;
        });
        startFragment(currentPage);
        Log.d(TAG, "onCreate: " + currentPage);
    }

    @Override
    public void onBackPressed() {
        if (currentPage.equals("HOME")) {
            super.onBackPressed();
        } else {
            currentPage = "HOME";
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    public void startFragment(String fragment) {
        Fragment frag = null;
        if (fragment.equals("PROFILE")) {
            frag = new ProfileFragment();
            currentPage = "PROFILE";
        } else if (fragment.equals("SEARCH")) {
            frag = new SearchFragment();
            currentPage = "SEARCH";
        } else if (fragment.equals("HOME")) {
            frag = new HomeFragment();
            currentPage = "HOME";
        }
        if (frag != null) startFragment(frag);
    }

    void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

}