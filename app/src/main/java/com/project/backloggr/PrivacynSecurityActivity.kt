package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class PrivacynSecurityActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var profileCircleTop: ImageView
    private lateinit var profileVisibilityGroup: RadioGroup
    private lateinit var publicOption: RadioButton
    private lateinit var friendsOnlyOption: RadioButton
    private lateinit var privateOption: RadioButton
    private lateinit var gameActivitySwitch: Switch
    private lateinit var mainLayout: androidx.constraintlayout.widget.ConstraintLayout

    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        initViews()
        setupListeners()
        loadPrivacySettings()
    }

    private fun initViews() {
        backIcon = findViewById(R.id.backIcon)
        profileCircleTop = findViewById(R.id.profileCircleTop)
        profileVisibilityGroup = findViewById(R.id.profileVisibilityGroup)
        publicOption = findViewById(R.id.publicOption)
        friendsOnlyOption = findViewById(R.id.friendsOnlyOption)
        privateOption = findViewById(R.id.privateOption)
        gameActivitySwitch = findViewById(R.id.gameActivitySwitch)
        mainLayout = findViewById(R.id.main)

        // Disable controls until data loads
        setControlsEnabled(false)
    }

    private fun setupListeners() {
        backIcon.setOnClickListener { finish() }

        profileCircleTop.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Profile Visibility Change
        profileVisibilityGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isUpdating) {
                val visibility = when (checkedId) {
                    R.id.publicOption -> "public"
                    R.id.friendsOnlyOption -> "friends_only"
                    R.id.privateOption -> "private"
                    else -> "friends_only"
                }
                savePrivacySetting("profile_visibility", visibility)
            }
        }

        // Game Activity Switch
        gameActivitySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdating) {
                savePrivacySetting("game_activity_visible", isChecked)
            }
        }
    }

    private fun setControlsEnabled(enabled: Boolean) {
        publicOption.isEnabled = enabled
        friendsOnlyOption.isEnabled = enabled
        privateOption.isEnabled = enabled
        gameActivitySwitch.isEnabled = enabled
    }

    private fun loadPrivacySettings() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        val url = "${BuildConfig.BASE_URL}api/auth/privacy-settings"

        val request = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val settings = response.getJSONObject("data").getJSONObject("settings")

                    isUpdating = true

                    // Set profile visibility
                    when (settings.optString("profile_visibility", "friends_only")) {
                        "public" -> publicOption.isChecked = true
                        "friends_only" -> friendsOnlyOption.isChecked = true
                        "private" -> privateOption.isChecked = true
                    }

                    // Set game activity
                    gameActivitySwitch.isChecked = settings.optBoolean("game_activity_visible", true)

                    isUpdating = false

                    // Enable controls
                    setControlsEnabled(true)

                } catch (e: Exception) {
                    Log.e("PrivacySecurity", "Failed to parse settings", e)
                    Toast.makeText(this, "Failed to load settings", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("PrivacySecurity", "Failed to load privacy settings", error)
                if (error.networkResponse?.statusCode == 401) {
                    Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(this, "Failed to load settings", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun savePrivacySetting(field: String, value: Any) {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        val jsonBody = JSONObject().apply {
            put(field, value)
        }

        setControlsEnabled(false)

        val url = "${BuildConfig.BASE_URL}api/auth/privacy-settings"

        val request = object : JsonObjectRequest(
            Method.PUT,
            url,
            jsonBody,
            { response ->
                setControlsEnabled(true)
                Snackbar.make(mainLayout, "Privacy setting updated!", Snackbar.LENGTH_SHORT).show()
            },
            { error ->
                setControlsEnabled(true)
                Log.e("PrivacySecurity", "Failed to save $field", error)
                Toast.makeText(this, "Failed to update setting", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToLogin() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }

    private fun loadProfileImage() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val cachedImage = prefs.getString("profile_image_cache", null)

        if (!cachedImage.isNullOrEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(cachedImage, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                profileCircleTop.setImageBitmap(bitmap)
            } catch (e: Exception) {
                profileCircleTop.setImageResource(R.drawable.profile_placeholder)
            }
        }
    }
}