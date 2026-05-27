package com.moneyapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Pilihan mode tema aplikasi.
 * - SYSTEM: mengikuti pengaturan sistem (default)
 * - LIGHT: selalu light mode
 * - DARK: selalu dark mode
 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnBackground
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimary,
    onBackground = OnBackgroundDark,
    onSurface = OnBackgroundDark
)

/**
 * Tema utama aplikasi Money.me.
 *
 * @param themeMode Mode tema yang digunakan (SYSTEM, LIGHT, atau DARK).
 * @param content Konten Composable yang akan dibungkus oleh tema ini.
 */
@Composable
fun MoneyAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
