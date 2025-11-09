package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileCircle: TextView

    private lateinit var gameCover: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        initViews()
        setupListeners()

    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        profileCircle = findViewById(R.id.profileCircle)
        gameCover = findViewById(R.id.gameCover)


    }

    private fun setupListeners() {
        // Bottom navigation clicks
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {

                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
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
            finish()
        }


        profileCircle.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

    }
}


