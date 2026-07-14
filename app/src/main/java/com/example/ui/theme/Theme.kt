package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SportGreen,
    secondary = SportBlueLight,
    tertiary = GoldAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextLightSecondary
  )

private val LightColorScheme = DarkColorScheme // Force dark theme for sport premium look

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the premium sportsbook look
  dynamicColor: Boolean = false, // Disable dynamic material-you colors to preserve our brand color identity
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
