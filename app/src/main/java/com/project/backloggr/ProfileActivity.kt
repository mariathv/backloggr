package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
//        val homeButton = findViewById<LinearLayout>(R.id.homeButton)
//        val searchButton = findViewById<LinearLayout>(R.id.searchButton)
        // You can add the other buttons here as well

        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

//        homeButton.setOnClickListener {
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//            finish() // Optional: finish ProfileActivity so you can't go back to it
//        }
//
//        searchButton.setOnClickListener {
//            val intent = Intent(this, SearchActivity::class.java)
//            startActivity(intent)
//            finish() // Optional: finish ProfileActivity
//        }
    }
}
