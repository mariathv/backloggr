package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileCircle: TextView
    private lateinit var gameCover: ImageView
    private lateinit var tvTotalGamesCount: TextView
    private lateinit var tvBackloggedCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvPlayingCount: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupListeners()
        fetchStatistics()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        profileCircle = findViewById(R.id.profileCircle)
        gameCover = findViewById(R.id.gameCover)
        tvTotalGamesCount = findViewById(R.id.tvTotalGamesCount)
        tvBackloggedCount = findViewById(R.id.tvBackloggedCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvPlayingCount = findViewById(R.id.tvPlayingCount)
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

    private fun fetchStatistics() {
        val url = "${BuildConfig.BASE_URL}api/statistics"
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_LONG).show()
            // Redirect to LoginActivity if no token
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val data = response.getJSONObject("data")
                    val totalGames = data.getInt("total_games")
                    val statusCounts = data.getJSONObject("status_counts")
                    val backlogged = statusCounts.optInt("backlogged", 0)
                    val playing = statusCounts.optInt("playing", 0)
                    val completed = statusCounts.optInt("completed", 0)

                    tvTotalGamesCount.text = totalGames.toString()
                    tvBackloggedCount.text = backlogged.toString()
                    tvPlayingCount.text = playing.toString()
                    tvCompletedCount.text = completed.toString()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing statistics data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching statistics: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
