package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// "Sleek Interface" Theme Configuration using standard Material Theme keys mapped to our palette
private val LightColorScheme = lightColorScheme(
    primary = AmberGold,
    secondary = AmberLight,
    tertiary = AccentBlue,
    background = SlateBlack,
    surface = SlateDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
