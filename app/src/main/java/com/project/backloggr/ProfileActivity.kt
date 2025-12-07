package com.project.backloggr

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        fetchProfileData()

        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
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
    }

    private fun fetchProfileData() {
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
                    val user = data.getJSONObject("user")
                    val statistics = data.getJSONObject("statistics")

                    usernameTextView.text = user.getString("username")
                    emailTextView.text = user.getString("email")

                    // Format and set the join date
                    val rawDate = user.getString("createdAt").substring(0, 10) // "YYYY-MM-DD"
                    val year = rawDate.substring(0, 4)
                    val month = rawDate.substring(5, 7)
                    val monthName = when(month) {
                        "01" -> "January"
                        "02" -> "February"
                        "03" -> "March"
                        "04" -> "April"
                        "05" -> "May"
                        "06" -> "June"
                        "07" -> "July"
                        "08" -> "August"
                        "09" -> "September"
                        "10" -> "October"
                        "11" -> "November"
                        "12" -> "December"
                        else -> ""
                    }
                    joinDateTextView.text = "Joined $monthName $year"

                    totalGamesTextView.text = statistics.getString("total_games")
                    completedGamesTextView.text = statistics.getString("completed_games")
                    playtimeTextView.text = "${statistics.getInt("total_hours_played")}h"

                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch profile data: ${error.message}", Toast.LENGTH_LONG).show()
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
