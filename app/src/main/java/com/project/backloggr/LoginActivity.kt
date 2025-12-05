package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val btnPlayStation = findViewById<Button>(R.id.btnPlayStation)
        val btnXbox = findViewById<Button>(R.id.btnXbox)
        val btnSteam = findViewById<Button>(R.id.btnSteam)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val signUpText = findViewById<TextView>(R.id.signUpText)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)

        btnSignIn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val url = "${BuildConfig.BASE_URL}api/auth/login"

            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                { response ->
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

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
                    Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )

            Volley.newRequestQueue(this).add(request)
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(this, "Password recovery not implemented", Toast.LENGTH_SHORT).show()
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnGoogle.setOnClickListener { Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnPlayStation.setOnClickListener { Toast.makeText(this, "PlayStation sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnXbox.setOnClickListener { Toast.makeText(this, "Xbox sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnSteam.setOnClickListener { Toast.makeText(this, "Steam sign-in coming soon", Toast.LENGTH_SHORT).show() }
    }
}
