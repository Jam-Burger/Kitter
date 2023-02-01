package com.jamburger.kitter;

import android.annotation.SuppressLint;
import android.os.Bundle;

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
    Fragment selectorFragment;
    FirebaseUser user;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        user = FirebaseAuth.getInstance().getCurrentUser();

        String page = getIntent().getStringExtra("page");
        if (page != null && page.equals("PROFILE")) {
            selectorFragment = new ProfileFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        } else {
            selectorFragment = new HomeFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectorFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    selectorFragment = new SearchFragment();
                    break;
                case R.id.nav_profile:
                    selectorFragment = new ProfileFragment();
                    break;
            }
            if (selectorFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, selectorFragment).commit();
            }
            return true;
        });
        startFragment(selectorFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

}