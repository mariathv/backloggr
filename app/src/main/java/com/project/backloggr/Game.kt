package com.project.backloggr

data class Game(
    val libraryId: Int? = null,      // from HEAD
    val igdbGameId: Int? = null,     // renamed for Kotlin naming convention
    val id: Int? = null,             // from origin/main
    val title: String,
    val coverUrl: String,
    val status: String = ""
)
