package com.diary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5C7A5C),
    onPrimary = Color.White,
    secondary = Color(0xFF8A6A40),
    background = Color(0xFFFDF6E3),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333),
)

@Composable
fun DiaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = DiaryTypography,
        content = content,
    )
}
