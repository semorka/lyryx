package com.semorka.lyryx.ui.theme

import android.app.Activity
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.semorka.lyryx.R

private val DarkColorScheme = darkColorScheme(
    primary = darkPrimaryColor,
    secondary = PurpleGrey80,
    onBackground = Color.White,
    background = Color(0xFF131318),
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = lightPrimaryColor,
    secondary = PurpleGrey40,
    onBackground = Color.Black,
    background = Color(0xFFfcf8ff),
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

val ultraFontFamily = FontFamily(
    Font(R.font.ultra_regular, FontWeight.Normal)
)

val dmserifFontFamily = FontFamily(
    Font(R.font.dmserif_text_regular, weight = FontWeight.Normal)
)

@Composable
fun LyryxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}