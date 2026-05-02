package org.example.project.data

import kotlinx.coroutines.flow.Flow

interface ThemeStorage {
    val darkThemeFlow: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}