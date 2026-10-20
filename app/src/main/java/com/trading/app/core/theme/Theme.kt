package com.trading.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BinanceDark = Color(0xFF0B0E11)
val BinanceDarkCard = Color(0xFF181A20)
val BinanceYellow = Color(0xFFF0B90B)
val ProfitGreen = Color(0xFF0ECB81)
val LossRed = Color(0xFFF6465D)
val TextPrimary = Color(0xFFEAECEF)
val TextSecondary = Color(0xFF848E9C)

private val DarkColorScheme = darkColorScheme(
    primary = BinanceYellow,
    background = BinanceDark,
    surface = BinanceDarkCard,
    onPrimary = BinanceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BinanceProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
