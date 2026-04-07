package com.toester.toester

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides the dark-mode toggle callback.
 * Call it from anywhere to flip the theme.
 */
val LocalToggleDarkMode = compositionLocalOf<() -> Unit> { {} }

/**
 * CompositionLocal that provides whether dark mode is currently active.
 */
val LocalIsDarkMode = compositionLocalOf { false }

