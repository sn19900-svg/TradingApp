package com.trading.app.domain.model

data class Coin(
    val symbol: String,
    val price: Double,
    val priceChangePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: Double
)

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class TradingSignal {
    BUY, SELL, HOLD
}

data class IndicatorResult(
    val rsi: Double,
    val ema: Double,
    val macd: Double,
    val bollingerUpper: Double,
    val bollingerLower: Double,
    val signal: TradingSignal
)

data class TradeOrder(
    val symbol: String,
    val type: String,
    val price: Double,
    val percentage: Double
)
