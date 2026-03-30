package com.INF865.izondevices.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.INF865.izondevices.ui.IzonDevicesApp

private val DarkColorScheme = darkColorScheme(
    primary = CoralRed80,
    secondary = CoralRed40,

    background = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = CoralRed80,
    secondary = CoralRed40,

    background = CoralRedAppBackground,
)

@Composable
fun IzondevicesTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("izon_prefs", Context.MODE_PRIVATE)
    val isDarkStr = prefs.getString("theme_mode", null)
    val isDark = if (isDarkStr == "true") true else false
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}