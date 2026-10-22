package com.appawake.app.data

import android.graphics.drawable.Drawable

enum class AppCategory(val label: String) {
    ALL("All Apps"),
    ACTIVE("Selected"),
    SOCIAL("Social"),
    RIDER("Rider / Delivery"),
    GAMES("Games & Media"),
    OTHER("Other")
}

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false,
    val isSelected: Boolean = false,
    val category: AppCategory = AppCategory.OTHER
)
