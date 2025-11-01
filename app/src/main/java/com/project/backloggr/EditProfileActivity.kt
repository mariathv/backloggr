package com.example.backloggr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.example.backloggr.R


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // ===== Initialize Views =====
        profileImage = findViewById(R.id.profileImage)
        changePhotoButton = findViewById(R.id.changePhotoButton)
        saveButton = findViewById(R.id.btnSignIn)
        backIcon = findViewById(R.id.backIcon)

        // EditText fields
        displayNameInput = findViewById(R.id.basicInfoContainer)
            .findViewById(R.id.displayName)
        usernameInput = findViewById(R.id.basicInfoContainer)
            .findViewById(R.id.username)
        emailInput = findViewById(R.id.basicInfoContainer)
            .findViewById(R.id.email)
        bioInput = findViewById(R.id.basicInfoContainer)
            .findViewById(R.id.bio)

        // ===== Back Button =====
        backIcon.setOnClickListener {
            finish()
        }

        // ===== Change Photo =====
        changePhotoButton.setOnClickListener {
            openGallery()
        }

        // ===== Save Button =====
        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    // ===== Open Image Picker =====
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // ===== Handle Image Result =====
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            profileImage.setImageURI(imageUri)
        }
    }

    // ===== Save Profile Info =====
    private fun saveProfile() {
        val displayName = displayNameInput.text.toString()
        val username = usernameInput.text.toString()
        val email = emailInput.text.toString()
        val bio = bioInput.text.toString()

        if (displayName.isBlank() || username.isBlank() || email.isBlank()) {
            Snackbar.make(saveButton, "Please fill out all required fields.", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Simulate saving user profile (you can integrate Firebase or database here)
        Snackbar.make(saveButton, "Profile saved successfully!", Snackbar.LENGTH_SHORT).show()
    }
}
