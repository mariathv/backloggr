package com.project.backloggr

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnAddGame = findViewById<Button>(R.id.btnAddGame)
        val navHome = findViewById<ImageView>(R.id.navHome)
        val navExplore = findViewById<ImageView>(R.id.navExplore)
        val navLibrary = findViewById<ImageView>(R.id.navLibrary)
        val navInsights = findViewById<ImageView>(R.id.navInsights)

        btnAddGame.setOnClickListener {
            Toast.makeText(this, "Add Game clicked!", Toast.LENGTH_SHORT).show()
        }

        navHome.setOnClickListener { Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show() }
        navExplore.setOnClickListener { Toast.makeText(this, "Explore", Toast.LENGTH_SHORT).show() }
        navLibrary.setOnClickListener { Toast.makeText(this, "Library", Toast.LENGTH_SHORT).show() }
        navInsights.setOnClickListener { Toast.makeText(this, "Insights", Toast.LENGTH_SHORT).show() }
    }
}


