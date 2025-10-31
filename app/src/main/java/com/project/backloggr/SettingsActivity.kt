package com.project.backloggr

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.project.backloggr.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch
    private lateinit var switchAnalytics: Switch
    private lateinit var switchNotifications: Switch
    private lateinit var btnEdit: Button
    private lateinit var backIcon: ImageView
    private lateinit var privacySection: LinearLayout

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Find views
        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchAnalytics = findViewById(R.id.switchAnalytics)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnEdit = findViewById(R.id.btnEdit)
        backIcon = findViewById(R.id.backIcon)
        privacySection = findViewById(R.id.settingsSection)

        // Load saved preferences
        loadSettings()

        // Back navigation
        backIcon.setOnClickListener {
            finish()
        }

        // Dark Mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean("dark_mode", isChecked)
            }
            toggleDarkMode(isChecked)
        }

        // Analytics toggle
        switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean("analytics_enabled", isChecked)
            }
            Toast.makeText(
                this,
                if (isChecked) "Analytics enabled" else "Analytics disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean("notifications_enabled", isChecked)
            }
            Toast.makeText(
                this,
                if (isChecked) "Notifications ON" else "Notifications OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Edit Profile button
        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit profile clicked", Toast.LENGTH_SHORT).show()
            // Example:
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Privacy & Security section click
        privacySection.setOnClickListener {
            Toast.makeText(this, "Privacy settings clicked", Toast.LENGTH_SHORT).show()
            // Example:
            // startActivity(Intent(this, PrivacyActivity::class.java))
        }
    }

    private fun loadSettings() {
        val darkMode = prefs.getBoolean("dark_mode", true)
        val analytics = prefs.getBoolean("analytics_enabled", false)
        val notifications = prefs.getBoolean("notifications_enabled", true)

        switchDarkMode.isChecked = darkMode
        switchAnalytics.isChecked = analytics
        switchNotifications.isChecked = notifications

        toggleDarkMode(darkMode)
    }

    private fun toggleDarkMode(enabled: Boolean) {
        val mode = if (enabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
