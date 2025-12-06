package com.project.backloggr

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class InsightsActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvTotalHours: TextView
    private lateinit var tvCompletedGames: TextView
    private lateinit var tvPlayingGames: TextView
    private lateinit var tvBackloggedGames: TextView
    private lateinit var tvOnHoldGames: TextView
    private lateinit var tvDroppedGames: TextView
    private lateinit var pieChart: PieChart

    private lateinit var legendItemPlaying: LinearLayout
    private lateinit var legendItemCompleted: LinearLayout
    private lateinit var legendItemBacklogged: LinearLayout
    private lateinit var legendItemOnHold: LinearLayout
    private lateinit var legendItemDropped: LinearLayout

    private lateinit var tvLegendPlaying: TextView
    private lateinit var tvLegendCompleted: TextView
    private lateinit var tvLegendBacklogged: TextView
    private lateinit var tvLegendOnHold: TextView
    private lateinit var tvLegendDropped: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        initViews()
        setupListeners()
        fetchStatistics()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_insights

        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvTotalHours = findViewById(R.id.tvTotalHours)
        tvCompletedGames = findViewById(R.id.tvCompletedGames)
        tvPlayingGames = findViewById(R.id.tvPlayingGames)
        tvBackloggedGames = findViewById(R.id.tvBackloggedGames)
        tvOnHoldGames = findViewById(R.id.tvOnHoldGames)
        tvDroppedGames = findViewById(R.id.tvDroppedGames)
        pieChart = findViewById(R.id.pieChart)

        legendItemPlaying = findViewById(R.id.legendItemPlaying)
        legendItemCompleted = findViewById(R.id.legendItemCompleted)
        legendItemBacklogged = findViewById(R.id.legendItemBacklogged)
        legendItemOnHold = findViewById(R.id.legendItemOnHold)
        legendItemDropped = findViewById(R.id.legendItemDropped)

        tvLegendPlaying = findViewById(R.id.tvLegendPlaying)
        tvLegendCompleted = findViewById(R.id.tvLegendCompleted)
        tvLegendBacklogged = findViewById(R.id.tvLegendBacklogged)
        tvLegendOnHold = findViewById(R.id.tvLegendOnHold)
        tvLegendDropped = findViewById(R.id.tvLegendDropped)
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_explore -> startActivity(Intent(this, SearchActivity::class.java))
                R.id.nav_library -> startActivity(Intent(this, LibraryActivity::class.java))
                R.id.nav_insights -> {}
            }
            true
        }
    }

    private fun fetchStatistics() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_LONG).show()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/statistics"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val data = response.getJSONObject("data")
                val stats = data.getJSONObject("statistics")

                tvTotalGames.text = stats.getInt("total_games").toString()
                tvTotalHours.text = "${stats.getString("total_hours")}h"
                tvCompletedGames.text = stats.getInt("completed_games").toString()
                tvPlayingGames.text = stats.getInt("playing_games").toString()
                tvBackloggedGames.text = stats.getInt("backlogged_games").toString()
                tvOnHoldGames.text = stats.getInt("on_hold_games").toString()
                tvDroppedGames.text = stats.getInt("dropped_games").toString()

                updateLegendAndChart(stats)
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

    private fun updateLegendAndChart(stats: JSONObject) {
        val statusMap = mapOf(
            "Playing" to stats.getInt("playing_games"),
            "Completed" to stats.getInt("completed_games"),
            "Backlogged" to stats.getInt("backlogged_games"),
            "On Hold" to stats.getInt("on_hold_games"),
            "Dropped" to stats.getInt("dropped_games")
        )

        val legendViews = mapOf(
            "Playing" to Pair(legendItemPlaying, tvLegendPlaying),
            "Completed" to Pair(legendItemCompleted, tvLegendCompleted),
            "Backlogged" to Pair(legendItemBacklogged, tvLegendBacklogged),
            "On Hold" to Pair(legendItemOnHold, tvLegendOnHold),
            "Dropped" to Pair(legendItemDropped, tvLegendDropped)
        )

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val statusColors = mapOf(
            "Playing" to Color.parseColor("#e57373"),
            "Completed" to Color.parseColor("#81c784"),
            "Backlogged" to Color.parseColor("#64b5f6"),
            "On Hold" to Color.parseColor("#ffb74d"),
            "Dropped" to Color.parseColor("#90a4ae")
        )

        var hasData = false
        for ((status, count) in statusMap) {
            val (item, label) = legendViews[status]!!
            if (count > 0) {
                item.visibility = View.VISIBLE
                label.text = "$status ($count)"
                entries.add(PieEntry(count.toFloat(), status))
                colors.add(statusColors[status]!!)
                hasData = true
            } else {
                item.visibility = View.GONE
            }
        }

        if (hasData) {
            pieChart.visibility = View.VISIBLE
            setupPieChart(entries, colors)
        } else {
            pieChart.visibility = View.GONE
        }
    }

    private fun setupPieChart(entries: List<PieEntry>, colors: List<Int>) {
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(false)
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 61f
            centerText = "Games"
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(20f)
            legend.isEnabled = false
            invalidate()
        }
    }
}
