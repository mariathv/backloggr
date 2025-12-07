package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException

class SearchActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var searchInput: EditText
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private val games = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_explore
        searchInput = findViewById(R.id.searchInput)
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(games)
        gamesRecyclerView.adapter = gameAdapter
        gamesRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                R.id.nav_explore -> { /* Do nothing */ }
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

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchGames(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchGames(query: String) {
        if (query.length < 2) {
            games.clear()
            gameAdapter.notifyDataSetChanged()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/games/search?q=$query"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val newGames = mutableListOf<Game>()
                    val data = response.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        val gameJson = data.getJSONObject(i)
                        val coverObject = gameJson.optJSONObject("cover")
                        // Ensure we get a valid URL, replacing thumb with a higher quality image
                        val coverUrl = coverObject?.getString("url")?.replace("t_thumb", "t_cover_big") ?: ""
                        val game = Game(
                            id = gameJson.getInt("id"),
                            title = gameJson.getString("name"),
                            coverUrl = coverUrl,
                            status = ""
                        )
                        newGames.add(game)
                    }
                    gameAdapter.updateGames(newGames)
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing search results", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Search failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
