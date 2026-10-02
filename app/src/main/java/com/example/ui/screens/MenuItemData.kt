package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItemData(
    val title: String,
    val hindiTitle: String = "",
    val subtitle: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true,
    val isLocked: Boolean = false,
    val statusBadge: String = "",
    val testTag: String = "",
    val iconTint: Color = Color.White,
    val iconBgColor: Color = Color(0xFF0F2B4A),
    val gradientColors: List<Color> = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    val borderColor: Color = Color(0xFF60A5FA).copy(alpha = 0.4f)
)
