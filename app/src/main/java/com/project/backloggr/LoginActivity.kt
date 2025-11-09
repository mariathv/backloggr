package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(this, "Password recovery not implemented", Toast.LENGTH_SHORT).show()
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnGoogle.setOnClickListener { Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnPlayStation.setOnClickListener { Toast.makeText(this, "PlayStation sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnXbox.setOnClickListener { Toast.makeText(this, "Xbox sign-in coming soon", Toast.LENGTH_SHORT).show() }
        btnSteam.setOnClickListener { Toast.makeText(this, "Steam sign-in coming soon", Toast.LENGTH_SHORT).show() }
    }
}
