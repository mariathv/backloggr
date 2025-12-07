package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AboutActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var profileCircleTop: ImageView
    private lateinit var appLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initViews()
        setupListeners()
        loadProfileImage()
    }

    private fun initViews() {
        backIcon = findViewById(R.id.backIcon)
        profileCircleTop = findViewById(R.id.profileCircleTop)
        appLogo = findViewById(R.id.appLogo)
    }

    private fun setupListeners() {
        // Back button
        backIcon.setOnClickListener {
            finish()
        }

        // Profile circle click - navigate to settings or profile
        profileCircleTop.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileImage() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token != null) {
            // Load profile image from SharedPreferences or API
            val cachedImage = prefs.getString("profile_image_cache", null)

            if (!cachedImage.isNullOrEmpty()) {
                try {
                    val decodedBytes = android.util.Base64.decode(cachedImage, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    profileCircleTop.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // If decode fails, keep default placeholder
                    profileCircleTop.setImageResource(R.drawable.profile_placeholder)
                }
            } else {
                // Load from network if needed
                loadProfileFromNetwork(token)
            }
        }
    }

    private fun loadProfileFromNetwork(token: String) {
        val url = "${BuildConfig.BASE_URL}api/auth/profile"

        val request = object : com.android.volley.toolbox.JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val userData = response.getJSONObject("data").getJSONObject("user")
                    val imageBase64 = userData.optString("profile_image_url", "")

                    if (imageBase64.isNotEmpty()) {
                        // Cache the image
                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit().putString("profile_image_cache", imageBase64).apply()

                        // Display the image
                        val decodedBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        profileCircleTop.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AboutActivity", "Failed to load profile image", e)
                }
            },
            { error ->
                android.util.Log.e("AboutActivity", "Failed to fetch profile", error)
            }
        ) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request)
    }

    override fun onResume() {
        super.onResume()
        // Reload profile image when returning to this activity
        loadProfileImage()
    }
}