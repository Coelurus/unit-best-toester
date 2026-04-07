package com.toester.toester

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkMode.current
    val toggle = LocalToggleDarkMode.current

    IconButton(
        onClick = toggle,
        modifier = modifier
            .size(44.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = if (isDark) "☀️" else "🌙",
            fontSize = 20.sp,
        )
    }
}


