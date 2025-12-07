package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import com.project.backloggr.utils.FCMTokenManager

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileCircle: TextView
    private lateinit var continuePlayingContainer: LinearLayout
    private lateinit var tvTotalGamesCount: TextView
    private lateinit var tvBackloggedCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvPlayingCount: TextView
    private lateinit var btnAddGame: Button

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupListeners()
        fetchStatistics()
        fetchPlayingGames()
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token != null && !FCMTokenManager.isTokenSent(this)) {
            FCMTokenManager.retrieveAndSendToken(this, token)
        }
    }

    override fun onResume() {
        super.onResume()
        if (NetworkUtils.isOnline(this)) {
            SyncManager.syncPendingChanges(this) {
                fetchStatistics()
                fetchPlayingGames()
            }
        }
    }


    private fun initViews() {
        db = DatabaseHelper(this)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        profileCircle = findViewById(R.id.profileCircle)
        continuePlayingContainer = findViewById(R.id.continuePlayingContainer)
        tvTotalGamesCount = findViewById(R.id.tvTotalGamesCount)
        tvBackloggedCount = findViewById(R.id.tvBackloggedCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvPlayingCount = findViewById(R.id.tvPlayingCount)
        btnAddGame = findViewById(R.id.btnAddGame)
    }

    private fun setupListeners() {
        // Bottom navigation clicks
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_explore -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        btnAddGame.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        profileCircle.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun fetchStatistics() {
        // First, load from SQLite for instant display
        val cachedStats = db.getStatistics()
        if (cachedStats != null) {
            updateStatisticsUI(cachedStats)
        }

        // If online, fetch from server
        if (!NetworkUtils.isOnline(this)) {
            if (cachedStats == null) {
                Toast.makeText(this, "No cached data available. Please connect to internet.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val url = "${BuildConfig.BASE_URL}api/statistics"
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_LONG).show()
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
                    Log.d("HomeActivity", "Statistics Response: $response")
                    val statistics = response.getJSONObject("data").getJSONObject("statistics")

                    // Update SQLite cache
                    db.updateStatistics(statistics)

                    // Update UI
                    updateStatisticsUI(statistics)
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error parsing statistics", e)
                    Toast.makeText(this, "Error parsing statistics data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("HomeActivity", "Error fetching statistics", error)
                error.networkResponse?.let {
                    Log.e("HomeActivity", "Status code: ${it.statusCode}")
                    Log.e("HomeActivity", "Response: ${String(it.data)}")
                }
                if (cachedStats == null) {
                    Toast.makeText(this, "Error fetching statistics: ${error.message}", Toast.LENGTH_LONG).show()
                }
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
    private fun updateStatisticsUI(statistics: JSONObject) {
        val totalGames = statistics.optInt("total_games", 0)
        val backlogged = statistics.optInt("backlogged_games", 0)
        val playing = statistics.optInt("playing_games", 0)
        val completed = statistics.optInt("completed_games", 0)

        tvTotalGamesCount.text = totalGames.toString()
        tvBackloggedCount.text = backlogged.toString()
        tvPlayingCount.text = playing.toString()
        tvCompletedCount.text = completed.toString()
    }
    private fun fetchPlayingGames() {
        // First, load from SQLite
        val cachedGames = db.getGamesByStatus("playing")
        if (cachedGames.isNotEmpty()) {
            displayPlayingGames(cachedGames)
        }

        // If online, fetch from server
        if (!NetworkUtils.isOnline(this)) {
            if (cachedGames.isEmpty()) {
                addEmptyPlayingState()
            }
            return
        }

        val url = "${BuildConfig.BASE_URL}api/library?status=playing&limit=10&offset=0"
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    Log.d("HomeActivity", "Playing Games Response: $response")
                    val gamesArray = response.getJSONObject("data").getJSONArray("games")

                    // Update SQLite cache
                    for (i in 0 until gamesArray.length()) {
                        val game = gamesArray.getJSONObject(i)
                        db.insertOrUpdateGame(game)
                    }

                    // Convert to list and display
                    val games = mutableListOf<JSONObject>()
                    for (i in 0 until gamesArray.length()) {
                        games.add(gamesArray.getJSONObject(i))
                    }
                    displayPlayingGames(games)
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error parsing playing games", e)
                    if (cachedGames.isEmpty()) {
                        Toast.makeText(this, "Error loading playing games", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { error ->
                Log.e("HomeActivity", "Error fetching playing games", error)
                error.networkResponse?.let {
                    Log.e("HomeActivity", "Status code: ${it.statusCode}")
                    Log.e("HomeActivity", "Response: ${String(it.data)}")
                }
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
    private fun displayPlayingGames(games: List<JSONObject>) {
        continuePlayingContainer.removeAllViews()

        if (games.isEmpty()) {
            addEmptyPlayingState()
        } else {
            for (i in 0 until minOf(10, games.size)) {
                addGameCard(games[i])
            }
        }
    }

    private fun addGameCard(game: JSONObject) {
        val gameDetails = game.getJSONObject("game_details")
        val gameName = gameDetails.optString("name", "Unknown Game")
        val libraryId = game.getInt("id")

        // Create game card layout
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(140),
                dpToPx(220)
            ).apply {
                marginEnd = dpToPx(12)
            }
            setBackgroundResource(R.drawable.card_bg)
        }

        // Create ImageView for cover
        val coverImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Load cover image
        val coverObj = gameDetails.optJSONObject("cover")
        if (coverObj != null) {
            val coverUrl = "https:" + coverObj.optString("url", "").replace("t_thumb", "t_cover_big")
            Glide.with(this).load(coverUrl).into(coverImageView)
        }

        // Create TextView for game name
        val nameTextView = TextView(this).apply {
            text = gameName
            setTextColor(resources.getColor(android.R.color.white, null))
            textSize = 14f
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        cardLayout.addView(coverImageView)
        cardLayout.addView(nameTextView)

        // Set click listener to open game detail
        cardLayout.setOnClickListener {
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("LIBRARY_ID", libraryId)
            startActivity(intent)
        }

        continuePlayingContainer.addView(cardLayout)
    }

    private fun addEmptyPlayingState() {
        val emptyTextView = TextView(this).apply {
            text = "No games currently playing. Start a new game!"
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(32), dpToPx(16), dpToPx(32))
        }
        continuePlayingContainer.addView(emptyTextView)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}