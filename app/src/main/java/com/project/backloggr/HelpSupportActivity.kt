package com.project.backloggr

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

class HelpSupportActivity : AppCompatActivity() {

    // Data structure for FAQs
    data class FAQ(val question: String, val answer: String)

    private val faqList = listOf(
        FAQ(
            "How do I add games to my library?",
            "To add games to your library, tap the '+' or 'Add Game' button on the home screen. Search for your game using the search bar, select it from the results, and choose 'Add to Library' or 'Add to Backlog'."
        ),
        FAQ(
            "How do I track my game progress?",
            "You can track your game progress by opening any game in your library and updating the status (Playing, Completed, On Hold, etc.). You can also add playtime hours and completion percentage if available."
        ),
        FAQ(
            "Can I sync my data across devices?",
            "Yes! Your data is automatically synced to the cloud when you're logged in. Simply log in with the same account on another device to access your complete game library and progress."
        ),
        FAQ(
            "How do I change my privacy settings?",
            "Go to Settings > Privacy & Security. From there, you can control who can see your profile, manage blocked users, and adjust your data sharing preferences."
        ),
        FAQ(
            "How do I add Personal Screenshots?",
            "Open any game in your library, tap on the 'Screenshots' tab or gallery icon, then tap the '+' button to add screenshots from your device's gallery or camera."
        ),
        FAQ(
            "How do I mark a game as completed?",
            "Long press on any game in your backlog or tap to open it, then select 'Mark as Completed' from the menu options or status dropdown."
        ),
        FAQ(
            "Can I export my game library?",
            "Yes, you can export your data as CSV or JSON from Settings > Account > Export Data. This allows you to backup or analyze your gaming history."
        ),
        FAQ(
            "How do I enable dark mode?",
            "Navigate to Settings > Preferences and toggle the 'Dark Mode' switch to enable or disable dark theme."
        )
    )

    private lateinit var searchFAQ: EditText
    private lateinit var faqContainer: LinearLayout
    private var filteredFaqList = faqList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        // --- Initialize Views ---
        val backIcon: ImageView = findViewById(R.id.backIcon)
        val questionIcon: ImageView = findViewById(R.id.questionIcon)
        searchFAQ = findViewById(R.id.searchFAQ)

        // Find the FAQ container
        // We'll create it dynamically and add it to the scroll view
        val scrollView: ScrollView = findViewById(R.id.helpScroll)
        val mainLayout = scrollView.getChildAt(0) as LinearLayout

        // Find the FAQ section (it's the last LinearLayout in the main layout)
        val faqSectionIndex = mainLayout.childCount - 1
        val faqSection = mainLayout.getChildAt(faqSectionIndex) as LinearLayout

        // The FAQ container is the second child (index 1) of the FAQ section
        faqContainer = faqSection.getChildAt(1) as LinearLayout

        // --- Event Listeners ---

        // Go back to previous screen
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Show info when question icon tapped
        questionIcon.setOnClickListener {
            Toast.makeText(this, "Browse FAQs or search for help", Toast.LENGTH_SHORT).show()
        }

        // Handle FAQ Search
        searchFAQ.addTextChangedListener { text ->
            val query = text.toString().trim()
            filterFAQs(query)
        }

        // Clear the static FAQ items from XML and populate dynamically
        clearStaticFAQs()

        // Display all FAQs initially
        displayFAQs(faqList)
    }

    private fun clearStaticFAQs() {
        // Remove all existing FAQ items (the static ones from XML)
        faqContainer.removeAllViews()
    }

    private fun filterFAQs(query: String) {
        filteredFaqList = if (query.isEmpty()) {
            faqList
        } else {
            faqList.filter { faq ->
                faq.question.contains(query, ignoreCase = true) ||
                        faq.answer.contains(query, ignoreCase = true)
            }
        }
        displayFAQs(filteredFaqList)
    }

    private fun displayFAQs(faqs: List<FAQ>) {
        faqContainer.removeAllViews()

        if (faqs.isEmpty()) {
            val noResultsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(32, 32, 32, 32)
            }

            val noResultsText = TextView(this).apply {
                text = "No results found"
                textSize = 16f
                setTextColor(0xFFAAAAAA.toInt())
            }

            val noResultsSubtext = TextView(this).apply {
                text = "Try different keywords"
                textSize = 14f
                setTextColor(0xFFAAAAAA.toInt())
                setPadding(0, 8, 0, 0)
            }

            noResultsLayout.addView(noResultsText)
            noResultsLayout.addView(noResultsSubtext)
            faqContainer.addView(noResultsLayout)
            return
        }

        faqs.forEachIndexed { index, faq ->
            // Create FAQ item container
            val faqItemContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Question row (clickable)
            val questionLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 24, 0, 24)
            }

            val questionText = TextView(this).apply {
                text = faq.question
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val arrowIcon = ImageView(this).apply {
                setImageResource(R.drawable.ic_expand_more)
                setColorFilter(0xFFB0B0B0.toInt())
                layoutParams = LinearLayout.LayoutParams(48, 48)
                tag = "collapsed"
            }

            questionLayout.addView(questionText)
            questionLayout.addView(arrowIcon)

            // Answer text (initially hidden)
            val answerText = TextView(this).apply {
                text = faq.answer
                textSize = 14f
                setTextColor(0xFFAAAAAA.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 24)
                }
                setPadding(0, 0, 0, 0)
                visibility = View.GONE
                setLineSpacing(4f, 1.2f)
            }

            // Toggle answer visibility on click
            questionLayout.setOnClickListener {
                if (answerText.visibility == View.GONE) {
                    // Expand
                    answerText.visibility = View.VISIBLE
                    arrowIcon.rotation = 180f
                    arrowIcon.tag = "expanded"
                } else {
                    // Collapse
                    answerText.visibility = View.GONE
                    arrowIcon.rotation = 0f
                    arrowIcon.tag = "collapsed"
                }
            }

            // Add views to container
            faqItemContainer.addView(questionLayout)
            faqItemContainer.addView(answerText)

            // Add divider if not last item
            if (index < faqs.size - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                    setBackgroundColor(0xFF2A2A2A.toInt())
                }
                faqItemContainer.addView(divider)
            }

            faqContainer.addView(faqItemContainer)
        }
    }
}