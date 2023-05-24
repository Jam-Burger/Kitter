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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

    public static String dateIdToString(String dateId) {
        try {
            @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date date = format.parse(dateId);
            Date now = new Date();
            assert date != null;
            long difference_In_Millis = now.getTime() - date.getTime();
            long difference_In_Seconds = difference_In_Millis / 1000;
            long difference_In_Minutes = difference_In_Seconds / 60;
            long difference_In_Hours = difference_In_Minutes / 60;
            long difference_In_Days = difference_In_Hours / 24;

            String timeText = "";
            if (difference_In_Minutes == 0) {
                timeText = difference_In_Seconds + " second";
                if (difference_In_Seconds > 1) timeText += "s";
            } else if (difference_In_Hours == 0) {
                timeText = difference_In_Minutes + " minute";
                if (difference_In_Minutes > 1) timeText += "s";
            } else if (difference_In_Days == 0) {
                timeText = difference_In_Hours + " hour";
                if (difference_In_Hours > 1) timeText += "s";
            } else {
                timeText = difference_In_Days + " day";
                if (difference_In_Days > 1) timeText += "s";
            }
            timeText += " ago";
            return timeText;
        } catch (Exception e) {
            return null;
        }
    }

    public void startFragment(String fragment) {
        Fragment frag = null;
        switch (fragment) {
            case "PROFILE":
                frag = new ProfileFragment();
                currentPage = "PROFILE";
                break;
            case "SEARCH":
                frag = new SearchFragment();
                currentPage = "SEARCH";
                break;
            case "HOME":
                frag = new HomeFragment();
                currentPage = "HOME";
                break;
        }
        if (frag != null) startFragment(frag);
    }


    public static String dateIdToTime(String dateId) {
        String[] strings = dateId.split("-", 6);
        return strings[3] + ":" + strings[4];
    }
}