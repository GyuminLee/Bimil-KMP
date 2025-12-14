package com.imbavchenko.bimil.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bimil 골드/베이지 테마 색상
object BimilColors {
    // Primary (골드)
    val Primary = Color(0xFFD4A853)
    val PrimaryVariant = Color(0xFFC49A3D)
    val OnPrimary = Color.White

    // Secondary (베이지)
    val Secondary = Color(0xFFF5E6C8)
    val SecondaryVariant = Color(0xFFE8D4A8)
    val OnSecondary = Color(0xFF5D4E37)

    // 다크 테마 색상
    val DarkBackground = Color(0xFF1A1A1A)
    val DarkSurface = Color(0xFF2D2D2D)
    val DarkSurfaceVariant = Color(0xFF3D3D3D)

    // 라이트 테마 색상
    val LightBackground = Color(0xFFFAF8F5)
    val LightSurface = Color.White
    val LightSurfaceVariant = Color(0xFFF5F0E8)

    // 칩 색상
    val ChipActive = Color(0xFFF5E6C8)
    val ChipActiveText = Color(0xFF5D4E37)
    val ChipInactive = Color(0xFFE8E8E8)
    val ChipInactiveText = Color(0xFF9E9E9E)

    // 다크 모드 칩 색상
    val ChipActiveDark = Color(0xFF5D4E37)
    val ChipActiveTextDark = Color(0xFFF5E6C8)
    val ChipInactiveDark = Color(0xFF404040)
    val ChipInactiveTextDark = Color(0xFF808080)

    // 별점 색상
    val StarActive = Color(0xFFFFD700)
    val StarInactive = Color(0xFF9E9E9E)
}

private val LightColors = lightColorScheme(
    primary = BimilColors.Primary,
    onPrimary = BimilColors.OnPrimary,
    primaryContainer = BimilColors.Secondary,
    onPrimaryContainer = BimilColors.OnSecondary,
    secondary = BimilColors.Secondary,
    onSecondary = BimilColors.OnSecondary,
    secondaryContainer = BimilColors.SecondaryVariant,
    onSecondaryContainer = BimilColors.OnSecondary,
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = BimilColors.LightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = BimilColors.LightSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = BimilColors.LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5D5D5D),
    outline = Color(0xFFD1C4B0),
    outlineVariant = Color(0xFFE8DFD0)
)

private val DarkColors = darkColorScheme(
    primary = BimilColors.Primary,
    onPrimary = BimilColors.OnPrimary,
    primaryContainer = BimilColors.PrimaryVariant,
    onPrimaryContainer = BimilColors.OnPrimary,
    secondary = BimilColors.SecondaryVariant,
    onSecondary = BimilColors.OnSecondary,
    secondaryContainer = Color(0xFF4A3F2F),
    onSecondaryContainer = BimilColors.Secondary,
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = Color(0xFF047857),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFFB91C1C),
    onErrorContainer = Color(0xFFFEE2E2),
    background = BimilColors.DarkBackground,
    onBackground = Color(0xFFF5F5F5),
    surface = BimilColors.DarkSurface,
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = BimilColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF5D5D5D),
    outlineVariant = Color(0xFF404040)
)

@Composable
fun BimilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
