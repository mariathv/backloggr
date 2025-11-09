package com.project.backloggr

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.project.backloggr.R
class HelpSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        // --- Toolbar Controls ---
        val backIcon: ImageView = findViewById(R.id.backIcon)
        val questionIcon: ImageView = findViewById(R.id.questionIcon)

        // --- Action Buttons ---
        //val userGuideCard: LinearLayout = findViewById(R.id.userGuideCard)
       // val contactSupportCard: LinearLayout = findViewById(R.id.contactSupportCard)

        // --- Search Field ---
        val searchFAQ: EditText = findViewById(R.id.searchFAQ)

        // --- Event Listeners ---

        // Go back to previous screen
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Show info when question icon tapped
        questionIcon.setOnClickListener {
            Toast.makeText(this, "Help & Support Info", Toast.LENGTH_SHORT).show()
        }

        // Open User Guide section
//        userGuideCard.setOnClickListener {
//            Toast.makeText(this, "Opening User Guide...", Toast.LENGTH_SHORT).show()
//            // Example navigation (if you have a UserGuideActivity):
//            // startActivity(Intent(this, UserGuideActivity::class.java))
//        }
//
//        // Open Contact Support section
//        contactSupportCard.setOnClickListener {
//            Toast.makeText(this, "Opening Contact Support...", Toast.LENGTH_SHORT).show()
//            // Example navigation (if you have a ContactSupportActivity):
//            // startActivity(Intent(this, ContactSupportActivity::class.java))
//        }

        // Handle FAQ Search
        searchFAQ.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                // Here you can implement filtering or a search dialog
                Toast.makeText(this, "Searching: $query", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
