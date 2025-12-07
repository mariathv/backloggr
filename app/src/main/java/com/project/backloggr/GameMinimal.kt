package com.project.backloggr

data class GameMinimal(
    val title: String,
    val coverUrl: String,
    val status: String = "",
    val id: Int? = null
)
