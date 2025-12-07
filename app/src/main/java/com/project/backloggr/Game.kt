package com.project.backloggr

data class Game(
    val title: String,
    val coverUrl: String,
    val status: String = "",
    val id: Int? = null
)
