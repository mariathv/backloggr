package com.project.backloggr

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch
    private lateinit var switchAnalytics: Switch
    private lateinit var switchNotifications: Switch
    private lateinit var btnEdit: Button
    private lateinit var backIcon: ImageView
    private lateinit var privacySection: LinearLayout
    private lateinit var aboutSection: LinearLayout
    private lateinit var helpSupportSection: LinearLayout

    private lateinit var prefs: SharedPreferences
    private var currentProfile: JSONObject? = null
    private var isUpdatingDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Find views
        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchAnalytics = findViewById(R.id.switchAnalytics)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnEdit = findViewById(R.id.btnEdit)
        backIcon = findViewById(R.id.backIcon)
        privacySection = findViewById(R.id.privacySection)
        aboutSection = findViewById(R.id.aboutSection)
        helpSupportSection = findViewById(R.id.helpSupportSection)

        // Disable switches until profile loads
        setSwitchesEnabled(false)

        // Load saved local settings first
        loadLocalSettings()

        // Fetch full profile from backend
        loadUserProfileFromBackend()

        // Back navigation
        backIcon.setOnClickListener { finish() }

        // Live-save switches with delay for dark mode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingDarkMode) {
                prefs.edit { putBoolean("dark_mode", isChecked) }

                // Save to backend first, then toggle theme
                saveSwitchToBackend("dark_mode", isChecked) {
                    // Theme will auto-apply on next activity
                    toggleDarkModeGlobally(isChecked)
                }
            }
        }

        switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("analytics_enabled", isChecked) }
            saveSwitchToBackend("analytics_enabled", isChecked)
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("notifications_enabled", isChecked) }
            saveSwitchToBackend("notifications_enabled", isChecked)
        }

        // Edit Profile button
        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        privacySection.setOnClickListener {
            startActivity(Intent(this, PrivacynSecurityActivity::class.java))
        }

        aboutSection.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Help & Support section
        helpSupportSection.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }
    }

    private fun loadLocalSettings() {
        val darkMode = prefs.getBoolean("dark_mode", true)
        val analytics = prefs.getBoolean("analytics_enabled", false)
        val notifications = prefs.getBoolean("notifications_enabled", true)

        isUpdatingDarkMode = true
        switchDarkMode.isChecked = darkMode
        switchAnalytics.isChecked = analytics
        switchNotifications.isChecked = notifications
        isUpdatingDarkMode = false

        // Apply dark mode setting
        val mode = if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun toggleDarkModeGlobally(enabled: Boolean) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setSwitchesEnabled(enabled: Boolean) {
        switchDarkMode.isEnabled = enabled
        switchAnalytics.isEnabled = enabled
        switchNotifications.isEnabled = enabled
    }

    private fun loadUserProfileFromBackend() {
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/auth/profile"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    currentProfile = response.getJSONObject("data").getJSONObject("user")

                    // Backend might return 0/1 instead of true/false
                    isUpdatingDarkMode = true
                    switchDarkMode.isChecked = currentProfile?.optInt("dark_mode", 1) != 0
                    switchAnalytics.isChecked = currentProfile?.optInt("analytics_enabled", 0) != 0
                    switchNotifications.isChecked = currentProfile?.optInt("notifications_enabled", 1) != 0
                    isUpdatingDarkMode = false

                    // Enable switches now
                    setSwitchesEnabled(true)
                } catch (e: Exception) {
                    Log.e("SettingsActivity", "Profile parse error", e)
                    Toast.makeText(this, "Failed to parse profile", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("SettingsActivity", "Failed to load profile", error)
                if (error.networkResponse?.statusCode == 401) {
                    Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Authorization" to "Bearer $token")
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun saveSwitchToBackend(field: String, value: Boolean, onSuccess: (() -> Unit)? = null) {
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Send BOOLEAN, not 1/0
        val body = JSONObject().apply {
            put(field, value)
        }

        // Only disable switches for non-dark-mode fields
        if (field != "dark_mode") {
            setSwitchesEnabled(false)
        }

        val url = "${BuildConfig.BASE_URL}api/auth/settings"

        val request = object : JsonObjectRequest(Method.PUT, url, body,
            { response ->
                if (field != "dark_mode") {
                    setSwitchesEnabled(true)
                }

                runOnUiThread {
                    Snackbar.make(switchDarkMode, "$field updated!", Snackbar.LENGTH_SHORT).show()
                }

                // Call success callback if provided
                onSuccess?.invoke()
            },
            { error ->
                if (field != "dark_mode") {
                    setSwitchesEnabled(true)
                }

                Log.e("SettingsActivity", "Failed to save $field: " +
                        "${error.networkResponse?.statusCode} " +
                        "${error.networkResponse?.data?.let { String(it) }}")

                runOnUiThread {
                    Toast.makeText(this, "Failed to update $field", Toast.LENGTH_SHORT).show()
                }
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer $token",
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}