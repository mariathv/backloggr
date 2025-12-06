package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import org.json.JSONArray

class LibraryActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var searchBar: EditText
    private lateinit var btnGrid: ImageButton
    private lateinit var btnList: ImageButton
    private lateinit var tvGamesCount: TextView

    private lateinit var chipAll: Chip
    private lateinit var chipPlaying: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var chipBacklogged: Chip

    private var isGridView = true
    private var currentStatus: String? = null
    private var currentSearch: String = ""
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private var categoryCounts = mapOf(
        "all" to 0,
        "playing" to 0,
        "completed" to 0,
        "backlogged" to 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        initViews()
        setupRecyclerView()
        setupListeners()
        fetchCategoryCounts()
        fetchLibrary()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)
        searchBar = findViewById(R.id.searchBar)
        btnGrid = findViewById(R.id.btnGrid)
        btnList = findViewById(R.id.btnList)
        tvGamesCount = findViewById(R.id.tvGamesCount)

        chipAll = findViewById(R.id.chipAll)
        chipPlaying = findViewById(R.id.chipPlaying)
        chipCompleted = findViewById(R.id.chipCompleted)
        chipBacklogged = findViewById(R.id.chipBacklogged)

        bottomNavigation.selectedItemId = R.id.nav_library
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(mutableListOf(), isGridView)
        gamesRecyclerView.adapter = libraryAdapter
        updateLayoutManager()
    }

    private fun updateLayoutManager() {
        gamesRecyclerView.layoutManager = if (isGridView) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_explore -> startActivity(Intent(this, SearchActivity::class.java))
                R.id.nav_library -> {}
                R.id.nav_insights -> startActivity(Intent(this, InsightsActivity::class.java))
            }
            true
        }

        btnGrid.setOnClickListener { setViewType(true) }
        btnList.setOnClickListener { setViewType(false) }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    currentSearch = s?.toString() ?: ""
                    fetchLibrary()
                }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        chipAll.setOnClickListener { selectChip(chipAll, null) }
        chipPlaying.setOnClickListener { selectChip(chipPlaying, "playing") }
        chipCompleted.setOnClickListener { selectChip(chipCompleted, "completed") }
        chipBacklogged.setOnClickListener { selectChip(chipBacklogged, "backlogged") }
    }

    private fun setViewType(grid: Boolean) {
        if (isGridView != grid) {
            isGridView = grid
            libraryAdapter.setViewType(isGridView)
            updateLayoutManager()
            updateViewButtons()
        }
    }

    private fun updateViewButtons() {
        btnGrid.setBackgroundResource(if (isGridView) R.drawable.active_button_background else R.drawable.inactive_button_background)
        btnList.setBackgroundResource(if (!isGridView) R.drawable.active_button_background else R.drawable.inactive_button_background)
    }

    private fun selectChip(selectedChip: Chip, status: String?) {
        chipAll.chipBackgroundColor = getColorStateList(R.color.chip_background_inactive)
        chipPlaying.chipBackgroundColor = getColorStateList(R.color.chip_background_inactive)
        chipCompleted.chipBackgroundColor = getColorStateList(R.color.chip_background_inactive)
        chipBacklogged.chipBackgroundColor = getColorStateList(R.color.chip_background_inactive)

        selectedChip.chipBackgroundColor = getColorStateList(R.color.chip_background_active)
        currentStatus = status
        fetchLibrary()
    }

    private fun fetchCategoryCounts() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return
        val url = "${BuildConfig.BASE_URL}api/statistics"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    val stats = response.getJSONObject("data").getJSONObject("statistics")
                    categoryCounts = mapOf(
                        "all" to stats.optInt("total_games", 0),
                        "playing" to stats.optInt("playing_games", 0),
                        "completed" to stats.optInt("completed_games", 0),
                        "backlogged" to stats.optInt("backlogged_games", 0)
                    )
                    updateChipLabels()
                } catch (_: Exception) {}
            },
            { /* Handle errors silently */ }) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun updateChipLabels() {
        chipAll.text = "All (${categoryCounts["all"]})"
        chipPlaying.text = "Playing (${categoryCounts["playing"]})"
        chipCompleted.text = "Completed (${categoryCounts["completed"]})"
        chipBacklogged.text = "Backlogged (${categoryCounts["backlogged"]})"
    }

    private fun fetchLibrary() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: run {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_LONG).show()
            return
        }

        var url = "${BuildConfig.BASE_URL}api/library?"
        currentStatus?.let { url += "status=$it&" }
        if (currentSearch.isNotEmpty()) url += "search=$currentSearch&"
        url += "limit=100"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    val gamesArray = response.getJSONObject("data").getJSONArray("games")
                    val newGames = mutableListOf<Game>()

                    for (i in 0 until gamesArray.length()) {
                        val gameJson = gamesArray.getJSONObject(i)
                        val details = gameJson.optJSONObject("game_details")
                        val title = details?.optString("name") ?: "Game #${gameJson.optInt("igdb_game_id", 0)}"
                        val libraryId = gameJson.getInt("id")
                        val igdbGameId = gameJson.getInt("igdb_game_id")

                        var coverUrl = ""
                        details?.optJSONObject("cover")?.let {
                            val urlPath = it.optString("url", "")
                            if (urlPath.isNotEmpty()) coverUrl = "https:" + urlPath.replace("t_thumb", "t_cover_big")
                        }

                        newGames.add(Game(libraryId, igdbGameId, title, coverUrl, gameJson.optString("status")))
                    }

                    Log.d("LibraryActivity", "Parsed ${newGames.size} games, updating adapter")
                    libraryAdapter.updateGames(newGames)

                    val totalGames = response.getJSONObject("data").optInt("total", newGames.size)
                    tvGamesCount.text = if (totalGames == 1) "1 game found" else "$totalGames games found"

                    if (newGames.isEmpty()) {
                        val msg = when {
                            currentSearch.isNotEmpty() -> "No games found matching \"$currentSearch\""
                            currentStatus != null -> "No games in this category"
                            else -> "Your library is empty"
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LibraryActivity", "Error parsing library data", e)
                    Toast.makeText(this, "Error parsing library data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("LibraryActivity", "Failed to fetch library", error)
                Toast.makeText(this, "Failed to fetch library: ${error.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(this).add(request)
    }
}
