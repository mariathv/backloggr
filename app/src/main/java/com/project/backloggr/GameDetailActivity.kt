package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameDetailActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    // UI Elements
    private lateinit var gameBanner: ImageView
    private lateinit var gameTitle: TextView
    private lateinit var statusBadge: TextView
    private lateinit var genreText: TextView
    private lateinit var ratingText: TextView
    private lateinit var platformsContainer: LinearLayout
    private lateinit var infoMessage: LinearLayout
    private lateinit var infoMessageText: TextView

    // Status Buttons
    private lateinit var btnCurrentlyPlaying: TextView
    private lateinit var btnCompleted: TextView
    private lateinit var btnBacklogged: TextView
    private lateinit var btnOnHold: TextView
    private lateinit var btnDropped: TextView

    // Tab TextViews
    private lateinit var tabProgress: TextView
    private lateinit var tabPhotos: TextView
    private lateinit var tabNotes: TextView
    private lateinit var tabOverview: TextView

    // Tab Content Containers
    private lateinit var progressContent: LinearLayout
    private lateinit var photosContent: LinearLayout
    private lateinit var notesContent: LinearLayout
    private lateinit var overviewContent: LinearLayout

    // Progress Tab Elements
    private lateinit var progressPercentage: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var hoursPlayedText: TextView

    // Notes Tab Elements
    private lateinit var notesTextView: TextView

    // Overview Tab Elements
    private lateinit var developerText: TextView
    private lateinit var releaseDateText: TextView
    private lateinit var genreOverviewText: TextView
    private lateinit var publisherText: TextView
    private lateinit var playtimeText: TextView

    // Photos Tab Elements
    private lateinit var screenshotImage: ImageView

    private var libraryId: Int = -1
    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_detail)

        // Get library ID from intent
        libraryId = intent.getIntExtra("LIBRARY_ID", -1)
        if (libraryId == -1) {
            Toast.makeText(this, "Invalid game ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initBottomNavigation()
        setupBottomNavigationListeners()
        initTabs()
        setupTabListeners()
        setupStatusButtons()
        showTab("Progress")

        fetchGameDetails()
    }

    private fun initViews() {
        gameBanner = findViewById(R.id.gameBanner)
        gameTitle = findViewById(R.id.gameTitle)
        statusBadge = findViewById(R.id.statusBadge)
        genreText = findViewById(R.id.genreText)
        ratingText = findViewById(R.id.ratingText)
        platformsContainer = findViewById(R.id.platformsContainer)
        infoMessage = findViewById(R.id.infoMessage)
        infoMessageText = findViewById(R.id.infoMessageText)

        btnCurrentlyPlaying = findViewById(R.id.btnCurrentlyPlaying)
        btnCompleted = findViewById(R.id.btnCompleted)
        btnBacklogged = findViewById(R.id.btnBacklogged)
        btnOnHold = findViewById(R.id.btnOnHold)
        btnDropped = findViewById(R.id.btnDropped)

        progressPercentage = findViewById(R.id.progressPercentage)
        progressBar = findViewById(R.id.progressBar)
        hoursPlayedText = findViewById(R.id.hoursPlayedText)

        notesTextView = findViewById(R.id.notesTextView)

        developerText = findViewById(R.id.developerText)
        releaseDateText = findViewById(R.id.releaseDateText)
        genreOverviewText = findViewById(R.id.genreOverviewText)
        publisherText = findViewById(R.id.publisherText)
        playtimeText = findViewById(R.id.playtimeText)

        screenshotImage = findViewById(R.id.screenshotImage)
    }

    private fun initBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = -1
    }

    private fun setupBottomNavigationListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_explore -> startActivity(Intent(this, SearchActivity::class.java))
                R.id.nav_library -> startActivity(Intent(this, LibraryActivity::class.java))
                R.id.nav_insights -> startActivity(Intent(this, InsightsActivity::class.java))
            }
            true
        }
    }

    private fun initTabs() {
        tabProgress = findViewById(R.id.tabProgress)
        tabPhotos = findViewById(R.id.tabPhotos)
        tabNotes = findViewById(R.id.tabNotes)
        tabOverview = findViewById(R.id.tabOverview)

        progressContent = findViewById(R.id.progressContent)
        photosContent = findViewById(R.id.photosContent)
        notesContent = findViewById(R.id.notesContent)
        overviewContent = findViewById(R.id.overviewContent)
    }

    private fun setupTabListeners() {
        tabProgress.setOnClickListener { showTab("Progress") }
        tabPhotos.setOnClickListener { showTab("Photos") }
        tabNotes.setOnClickListener { showTab("Notes") }
        tabOverview.setOnClickListener { showTab("Overview") }
    }

    private fun setupStatusButtons() {
        btnCurrentlyPlaying.setOnClickListener { updateGameStatus("playing") }
        btnCompleted.setOnClickListener { updateGameStatus("completed") }
        btnBacklogged.setOnClickListener { updateGameStatus("backlogged") }
        btnOnHold.setOnClickListener { updateGameStatus("on_hold") }
        btnDropped.setOnClickListener { updateGameStatus("dropped") }
    }

    private fun showTab(tabName: String) {
        // Hide all contents
        progressContent.visibility = View.GONE
        photosContent.visibility = View.GONE
        notesContent.visibility = View.GONE
        overviewContent.visibility = View.GONE

        // Reset all tabs
        val tabs = listOf(tabProgress, tabPhotos, tabNotes, tabOverview)
        tabs.forEach { tab ->
            tab.background = null
            tab.setTextColor(resources.getColor(R.color.unselected, null))
        }

        // Show selected content and highlight tab
        when (tabName) {
            "Progress" -> {
                progressContent.visibility = View.VISIBLE
                tabProgress.setBackgroundResource(R.drawable.tab_selected)
                tabProgress.setTextColor(resources.getColor(R.color.primary, null))
            }
            "Photos" -> {
                photosContent.visibility = View.VISIBLE
                tabPhotos.setBackgroundResource(R.drawable.tab_selected)
                tabPhotos.setTextColor(resources.getColor(R.color.primary, null))
            }
            "Notes" -> {
                notesContent.visibility = View.VISIBLE
                tabNotes.setBackgroundResource(R.drawable.tab_selected)
                tabNotes.setTextColor(resources.getColor(R.color.primary, null))
            }
            "Overview" -> {
                overviewContent.visibility = View.VISIBLE
                tabOverview.setBackgroundResource(R.drawable.tab_selected)
                tabOverview.setTextColor(resources.getColor(R.color.primary, null))
            }
        }
    }

    private fun fetchGameDetails() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: run {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/library/$libraryId"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    val gameData = response.getJSONObject("data").getJSONObject("game")
                    val gameDetails = gameData.getJSONObject("game_details")

                    populateGameDetails(gameData, gameDetails)
                } catch (e: Exception) {
                    Log.e("GameDetailActivity", "Error parsing game details", e)
                    Toast.makeText(this, "Error loading game details", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("GameDetailActivity", "Failed to fetch game details", error)
                Toast.makeText(this, "Failed to load game: ${error.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun populateGameDetails(gameData: org.json.JSONObject, gameDetails: org.json.JSONObject) {
        // Set game title
        gameTitle.text = gameDetails.optString("name", "Unknown Game")

        // Set cover image
        val coverObj = gameDetails.optJSONObject("cover")
        if (coverObj != null) {
            val coverUrl = "https:" + coverObj.optString("url", "").replace("t_thumb", "t_cover_big")
            Glide.with(this).load(coverUrl).into(gameBanner)
        }

        // Set status
        currentStatus = gameData.optString("status", "backlogged")
        updateStatusUI(currentStatus)

        // Set genres
        val genresArray = gameDetails.optJSONArray("genres")
        if (genresArray != null && genresArray.length() > 0) {
            val genre = genresArray.getJSONObject(0).optString("name", "Unknown")
            genreText.text = genre
            genreOverviewText.text = genre
        }

        // Set rating
        val rating = gameDetails.optDouble("rating", 0.0)
        ratingText.text = String.format("%.1f", rating / 10.0)

        // Set platforms
        platformsContainer.removeAllViews()
        val platformsArray = gameDetails.optJSONArray("platforms")
        if (platformsArray != null) {
            for (i in 0 until minOf(3, platformsArray.length())) {
                val platform = platformsArray.getJSONObject(i)
                val platformName = platform.optString("name", "")
                addPlatformTag(platformName)
            }
        }

        // Set developer and publisher
        val companiesArray = gameDetails.optJSONArray("involved_companies")
        if (companiesArray != null) {
            for (i in 0 until companiesArray.length()) {
                val company = companiesArray.getJSONObject(i)
                val companyName = company.getJSONObject("company").optString("name", "")
                if (company.optBoolean("developer", false)) {
                    developerText.text = companyName
                }
                if (company.optBoolean("publisher", false)) {
                    publisherText.text = companyName
                }
            }
        }

        // Set release date
        val releaseTimestamp = gameDetails.optLong("first_release_date", 0)
        if (releaseTimestamp > 0) {
            val date = Date(releaseTimestamp * 1000)
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
            releaseDateText.text = format.format(date)
        }

        // Set hours played and progress
        val hoursPlayed = gameData.optString("hours_played", "0.00")
        hoursPlayedText.text = "${hoursPlayed}h played total"

        // For now, calculate a simple progress percentage based on hours
        val hours = hoursPlayed.toFloatOrNull() ?: 0f
        val estimatedProgress = minOf((hours / 50 * 100).toInt(), 100)
        progressPercentage.text = "$estimatedProgress%"
        progressBar.progress = estimatedProgress

        // Set notes
        val notes = gameData.optString("notes", "")
        if (notes.isNotEmpty()) {
            notesTextView.text = notes
        } else {
            notesTextView.text = "No notes added yet."
        }

        // Set screenshots
        val screenshotsArray = gameDetails.optJSONArray("screenshots")
        if (screenshotsArray != null && screenshotsArray.length() > 0) {
            val firstScreenshot = screenshotsArray.getJSONObject(0)
            val screenshotUrl = "https:" + firstScreenshot.optString("url", "").replace("t_thumb", "t_screenshot_big")
            Glide.with(this).load(screenshotUrl).into(screenshotImage)
        }

        // Placeholder for playtime estimate
        playtimeText.text = "Varies"
    }

    private fun addPlatformTag(platformName: String) {
        val platformTag = TextView(this)
        platformTag.text = platformName
        platformTag.setTextColor(resources.getColor(R.color.white, null))
        platformTag.textSize = 12f
        platformTag.setBackgroundResource(R.drawable.platform_tag_blue)
        platformTag.setPadding(24, 8, 24, 8)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = 16
        platformTag.layoutParams = params

        platformsContainer.addView(platformTag)
    }

    private fun updateStatusUI(status: String) {
        // Reset all buttons
        val buttons = listOf(btnCurrentlyPlaying, btnCompleted, btnBacklogged, btnOnHold, btnDropped)
        buttons.forEach { it.setBackgroundResource(R.drawable.status_button_normal) }

        // Highlight current status
        when (status) {
            "playing" -> {
                btnCurrentlyPlaying.setBackgroundResource(R.drawable.status_button_selected)
                statusBadge.text = "Currently Playing"
                infoMessageText.text = "You're currently playing this game. Keep up the great progress!"
            }
            "completed" -> {
                btnCompleted.setBackgroundResource(R.drawable.status_button_selected)
                statusBadge.text = "Completed"
                infoMessageText.text = "You've completed this game. Congratulations!"
            }
            "backlogged" -> {
                btnBacklogged.setBackgroundResource(R.drawable.status_button_selected)
                statusBadge.text = "In Backlog"
                infoMessageText.text = "This game is in your backlog. Start playing when you're ready!"
            }
            "on_hold" -> {
                btnOnHold.setBackgroundResource(R.drawable.status_button_selected)
                statusBadge.text = "On Hold"
                infoMessageText.text = "You've put this game on hold. Resume when you're ready!"
            }
            "dropped" -> {
                btnDropped.setBackgroundResource(R.drawable.status_button_selected)
                statusBadge.text = "Dropped"
                infoMessageText.text = "You've dropped this game. You can always pick it up again!"
            }
        }
    }

    private fun updateGameStatus(newStatus: String) {
        if (newStatus == currentStatus) return

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val url = "${BuildConfig.BASE_URL}api/library/$libraryId"

        val params = mapOf("status" to newStatus)
        val jsonObject = org.json.JSONObject(params)

        val request = object : JsonObjectRequest(Method.PUT, url, jsonObject,
            { response ->
                currentStatus = newStatus
                updateStatusUI(newStatus)
                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Log.e("GameDetailActivity", "Failed to update status", error)
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(this).add(request)
    }
}