package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val btnPlayStation = findViewById<Button>(R.id.btnPlayStation)
        val btnXbox = findViewById<Button>(R.id.btnXbox)
        val btnSteam = findViewById<Button>(R.id.btnSteam)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val signInText = findViewById<TextView>(R.id.signInText)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val backButton = findViewById<ImageView>(R.id.backButton)

        btnSignUp.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val username = email.substringBefore("@")

            val jsonBody = JSONObject().apply {
                put("username", username)
                put("email", email)
                put("password", password)
            }

            val url = "${BuildConfig.BASE_URL}api/auth/register"

            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                { response ->
                    Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show()

                    val token = response.optJSONObject("data")?.optString("token")
                    if (!token.isNullOrEmpty()) {
                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()
                    }

                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )

            Volley.newRequestQueue(this).add(request)
        }

        signInText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        backButton.setOnClickListener {
            finish()
        }

        btnGoogle.setOnClickListener { Toast.makeText(this, "Google sign-up coming soon", Toast.LENGTH_SHORT).show() }
        btnPlayStation.setOnClickListener { Toast.makeText(this, "PlayStation sign-up coming soon", Toast.LENGTH_SHORT).show() }
        btnXbox.setOnClickListener { Toast.makeText(this, "Xbox sign-up coming soon", Toast.LENGTH_SHORT).show() }
        btnSteam.setOnClickListener { Toast.makeText(this, "Steam sign-up coming soon", Toast.LENGTH_SHORT).show() }
    }
}
