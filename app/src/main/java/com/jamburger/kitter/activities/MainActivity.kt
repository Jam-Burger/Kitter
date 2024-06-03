package com.jamburger.kitter.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.jamburger.kitter.R
import com.jamburger.kitter.fragments.HomeFragment
import com.jamburger.kitter.fragments.ProfileFragment
import com.jamburger.kitter.fragments.SearchFragment
import com.jamburger.kitter.services.AuthService

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    var user: FirebaseUser? = null
    private lateinit var currentPage: Fragment
    private lateinit var homeFragment: Fragment
    private lateinit var searchFragment: Fragment
    private lateinit var profileFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        user = AuthService.auth.currentUser

        homeFragment = HomeFragment()
        searchFragment = SearchFragment()
        profileFragment = ProfileFragment()


        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            when (itemId) {
                R.id.nav_home -> {
                    currentPage = homeFragment
                }

                R.id.nav_search -> {
                    currentPage = searchFragment
                }

                R.id.nav_profile -> {
                    currentPage = profileFragment
                }
            }
            updateFragment()
            true
        }

        val page = intent.getStringExtra("page")
        if (page != null) {
            if (page == "PROFILE") {
                currentPage = profileFragment
                bottomNavigationView.setSelectedItemId(R.id.nav_profile)
                updateFragment()
            }
        } else {
            currentPage = homeFragment
            updateFragment()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentPage == homeFragment) {
            super.onBackPressed()
        } else {
            currentPage = homeFragment
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun updateFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.frame_container, currentPage)
            .setReorderingAllowed(true)
            .commit()
    }
}
