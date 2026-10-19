package com.trading.app.domain.model

data class IndicatorResult(
    val rsi: Double,          // مؤشر القوة النسبية (Relative Strength Index)
    val ema: Double,          // المتوسط المتحرك الأسي (Exponential Moving Average)
    val macd: Double,         // مؤشر التقارب والتباعد للمتوسطات المتحركة
    val bollingerUpper: Double, // البولنجر باند العلوي
    val bollingerLower: Double, // البولنجر باند السفلي
    val signal: TradingSignal   // التوصية الناتجة (BUY, SELL, HOLD)
)

enum class TradingSignal {
    BUY, SELL, HOLD
}
