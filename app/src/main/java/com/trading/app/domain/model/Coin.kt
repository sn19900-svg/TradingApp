package com.trading.app.domain.model

data class Coin(
    val symbol: String,          // مثل BTCUSDT
    val baseAsset: String,       // مثل BTC
    val quoteAsset: String,      // مثل USDT
    val price: Double,           // السعر اللحظي
    val priceChangePercent: Double, // نسبة التغير اللحظي
    val high24h: Double,
    val low24h: Double
)
