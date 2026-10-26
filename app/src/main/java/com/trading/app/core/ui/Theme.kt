package com.trading.app.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.trading.app.core.theme.BinanceDark
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.TextPrimary

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
