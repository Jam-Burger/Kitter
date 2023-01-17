package com.jamburger.kitter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jamburger.kitter.fragments.HomeFragment;
import com.jamburger.kitter.fragments.ProfileFragment;
import com.jamburger.kitter.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment selectorFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectorFragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    selectorFragment = new SearchFragment();
                    break;
                case R.id.nav_add:
                    selectorFragment = null;
                    startActivity(new Intent(this, PostActivity.class));
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
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new HomeFragment()).commit();
    }
}