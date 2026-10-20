package com.trading.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BinanceYellow,
    background = BinanceDark,
    surface = BinanceDarkCard,
    onPrimary = BinanceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TradingAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
