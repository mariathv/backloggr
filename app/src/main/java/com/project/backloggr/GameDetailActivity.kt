package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class GameDetailActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_detail)

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initBottomNavigation()
        setupBottomNavigationListeners()
        initTabs()
        setupTabListeners()
        showTab("Progress")
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

        val tabs = listOf(tabProgress, tabPhotos, tabNotes, tabOverview)
        val contents = listOf(progressContent, photosContent, notesContent, overviewContent)

        tabs.forEachIndexed { index, tab ->
            tab.setOnClickListener {
                contents.forEachIndexed { i, content ->
                    content.visibility = if (i == index) View.VISIBLE else View.GONE
                }

                // Set selected background for the clicked tab, reset others
                tabs.forEachIndexed { i, t ->
                    if (i == index) {
                        t.setBackgroundResource(R.drawable.tab_selected)
                        t.setTextColor(resources.getColor(R.color.primary))
                    } else {
                        t.background = null
                        t.setTextColor(resources.getColor(R.color.unselected))
                    }
                }
            }
        }
    }


    private fun setupTabListeners() {
        tabProgress.setOnClickListener { showTab("Progress") }
        tabPhotos.setOnClickListener { showTab("Photos") }
        tabNotes.setOnClickListener { showTab("Notes") }
        tabOverview.setOnClickListener { showTab("Overview") }
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
            tab.setTextColor(resources.getColor(R.color.unselected))
        }

        // Show selected content and highlight tab
        when (tabName) {
            "Progress" -> {
                progressContent.visibility = View.VISIBLE
                tabProgress.setBackgroundResource(R.drawable.tab_selected)
                tabProgress.setTextColor(resources.getColor(R.color.primary))
            }
            "Photos" -> {
                photosContent.visibility = View.VISIBLE
                tabPhotos.setBackgroundResource(R.drawable.tab_selected)
                tabPhotos.setTextColor(resources.getColor(R.color.primary))
            }
            "Notes" -> {
                notesContent.visibility = View.VISIBLE
                tabNotes.setBackgroundResource(R.drawable.tab_selected)
                tabNotes.setTextColor(resources.getColor(R.color.primary))
            }
            "Overview" -> {
                overviewContent.visibility = View.VISIBLE
                tabOverview.setBackgroundResource(R.drawable.tab_selected)
                tabOverview.setTextColor(resources.getColor(R.color.primary))
            }
        }
    }

}
