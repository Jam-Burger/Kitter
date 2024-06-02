package com.jamburger.kitter.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jamburger.kitter.R;
import com.jamburger.kitter.fragments.HomeFragment;
import com.jamburger.kitter.fragments.ProfileFragment;
import com.jamburger.kitter.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseUser user;
    Fragment currentPage;
    private Fragment homeFragment, searchFragment, profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        user = FirebaseAuth.getInstance().getCurrentUser();

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        profileFragment = new ProfileFragment();


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                currentPage = homeFragment;
            } else if (itemId == R.id.nav_search) {
                currentPage = searchFragment;
            } else if (itemId == R.id.nav_profile) {
                currentPage = profileFragment;
            }
            updateFragment();
            return true;
        });

        String page = getIntent().getStringExtra("page");
        if (page != null) {
            if (page.equals("PROFILE")) {
                currentPage = profileFragment;
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                updateFragment();
            }
        } else {
            currentPage = homeFragment;
            updateFragment();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPage.equals(homeFragment)) {
            super.onBackPressed();
        } else {
            currentPage = homeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    void updateFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.frame_container, currentPage)
                .setReorderingAllowed(true)
                .commit();
    }
}
