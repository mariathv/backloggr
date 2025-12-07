package com.project.backloggr

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var changePhotoButton: Button
    private lateinit var saveButton: Button
    private lateinit var displayNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var bioInput: EditText
    private lateinit var backIcon: ImageView

    private val PICK_IMAGE_REQUEST = 101
    private var selectedImageUri: Uri? = null
    private var uploadedImageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        initViews()
        loadUserProfile()
        setupListeners()
    }

    private fun initViews() {
        profileImage = findViewById(R.id.profileImage)
        changePhotoButton = findViewById(R.id.changePhotoButton)
        saveButton = findViewById(R.id.btnsave)
        backIcon = findViewById(R.id.backIcon)

        displayNameInput = findViewById(R.id.displayName)
        usernameInput = findViewById(R.id.username)
        emailInput = findViewById(R.id.email)
        bioInput = findViewById(R.id.Bio)
    }

    private fun setupListeners() {
        backIcon.setOnClickListener { finish() }
        changePhotoButton.setOnClickListener { openGallery() }
        saveButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageThenSaveProfile()
            } else {
                saveProfile(uploadedImageBase64)
            }
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val url = "${BuildConfig.BASE_URL}api/auth/profile"

        val request = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val userData = response.getJSONObject("data").getJSONObject("user")
                    displayNameInput.setText(userData.optString("display_name", ""))
                    usernameInput.setText(userData.optString("username", ""))
                    emailInput.setText(userData.optString("email", ""))
                    bioInput.setText(userData.optString("bio", ""))

                    // Load existing profile image
                    val imageBase64 = userData.optString("profile_image_url", "")
                    if (imageBase64.isNotEmpty()) {
                        uploadedImageBase64 = imageBase64
                        val decodedBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        profileImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("EditProfile", "Error parsing profile", e)
                }
            },
            { error ->
                Log.e("EditProfile", "Failed to load profile", error)
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageThenSaveProfile() {
        val uri = selectedImageUri ?: return
        saveButton.isEnabled = false

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
            val imageBytes = baos.toByteArray()
            val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
            uploadedImageBase64 = base64Image

            // After encoding image, save profile
            saveProfile(base64Image)

        } catch (e: IOException) {
            saveButton.isEnabled = true
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile(profileImageBase64: String?) {
        val displayName = displayNameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val bio = bioInput.text.toString().trim()

        if (username.isEmpty() || email.isEmpty()) {
            Snackbar.make(saveButton, "Username and email are required", Snackbar.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            return
        }

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val jsonBody = JSONObject().apply {
            put("display_name", displayName)
            put("username", username)
            put("email", email)
            put("bio", bio)
            if (!profileImageBase64.isNullOrEmpty()) {
                put("profile_image_url", profileImageBase64)
            }
        }

        val url = "${BuildConfig.BASE_URL}api/auth/profile"

        val request = object : JsonObjectRequest(
            Method.PUT,
            url,
            jsonBody,
            { response ->
                saveButton.isEnabled = true
                Snackbar.make(saveButton, "Profile updated successfully!", Snackbar.LENGTH_SHORT).show()
            },
            { error ->
                saveButton.isEnabled = true
                Log.e("EditProfile", "Update failed", error)
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(this).add(request)
    }
}
