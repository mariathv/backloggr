package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var totalGamesTextView: TextView
    private lateinit var completedGamesTextView: TextView
    private lateinit var playtimeTextView: TextView
    private lateinit var joinDateTextView: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        fetchUserData()
        fetchStatistics()

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            prefs.edit().remove("token").apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun initViews() {
        usernameTextView = findViewById(R.id.username)
        emailTextView = findViewById(R.id.email)
        totalGamesTextView = findViewById(R.id.totalGames)
        completedGamesTextView = findViewById(R.id.completedGames)
        playtimeTextView = findViewById(R.id.playTime)
        joinDateTextView = findViewById(R.id.joinDate)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun fetchUserData() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Authentication token not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/auth/me"

        val request = object : JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val data = response.getJSONObject("data")
                    val user = data.getJSONObject("user")

                    usernameTextView.text = user.getString("username")
                    emailTextView.text = user.getString("email")

                    // Note: The API doesn't return createdAt, so we'll set a placeholder
                    // You may need to add this field to your API response
                    joinDateTextView.text = "Member since 2024"

                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch user data: ${error.message}", Toast.LENGTH_LONG).show()
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

    private fun fetchStatistics() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Authentication token not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/statistics"

        val request = object : JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val data = response.getJSONObject("data")
                    val statistics = data.getJSONObject("statistics")

                    totalGamesTextView.text = statistics.getString("total_games")
                    completedGamesTextView.text = statistics.getString("completed_games")

                    // Parse total_hours which comes as "10.00"
                    val totalHours = statistics.getString("total_hours").toDoubleOrNull() ?: 0.0
                    playtimeTextView.text = "${totalHours.toInt()}h"

                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing statistics: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch statistics: ${error.message}", Toast.LENGTH_LONG).show()
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