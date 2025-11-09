package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var gameCover: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize and setup
        initViews()
        setupListeners()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Set Explore as the default selected item
        bottomNavigation.selectedItemId = R.id.nav_explore
        gameCover = findViewById(R.id.gameCover)
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                R.id.nav_explore -> {
                    // Already on explore, do nothing
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    finish()
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    finish()
                }
            }
            true
        }
        gameCover.setOnClickListener {
            startActivity(Intent(this, GameDetailActivity::class.java))

    }}
}
