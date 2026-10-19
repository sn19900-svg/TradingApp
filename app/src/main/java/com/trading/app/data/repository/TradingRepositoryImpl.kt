package com.trading.app.data.repository

import com.trading.app.data.remote.BinanceApi
import com.trading.app.domain.model.*
import com.trading.app.domain.repository.TradingRepository
import java.util.UUID
import javax.inject.Inject

class TradingRepositoryImpl @Inject constructor(
    private val api: BinanceApi
) : TradingRepository {

    override suspend fun getTopCoins(): Result<List<Coin>> {
        return try {
            val response = api.get24hrTicker()
            // تصفية العملات الشهيرة المقابلة للـ USDT
            val filtered = response.filter { it.symbol.endsWith("USDT") && 
                (it.symbol.startsWith("BTC") || it.symbol.startsWith("ETH") || 
                 it.symbol.startsWith("BNB") || it.symbol.startsWith("SOL") || 
                 it.symbol.startsWith("XRP") || it.symbol.startsWith("ADA") ||
                 it.symbol.startsWith("DOGE") || it.symbol.startsWith("AVAX"))
            }.map {
                Coin(
                    symbol = it.symbol,
                    baseAsset = it.symbol.removeSuffix("USDT"),
                    quoteAsset = "USDT",
                    price = it.lastPrice.toDoubleOrNull() ?: 0.0,
                    priceChangePercent = it.priceChangePercent.toDoubleOrNull() ?: 0.0,
                    high24h = it.highPrice.toDoubleOrNull() ?: 0.0,
                    low24h = it.lowPrice.toDoubleOrNull() ?: 0.0
                )
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCoinCandles(symbol: String, interval: String): Result<List<Candle>> {
        return try {
            val rawKlines = api.getCandles(symbol = symbol, interval = interval)
            val candles = rawKlines.map { row ->
                Candle(
                    timestamp = (row[0] as Number).toLong(),
                    open = (row[1] as String).toDouble(),
                    high = (row[2] as String).toDouble(),
                    low = (row[3] as String).toDouble(),
                    close = (row[4] as String).toDouble(),
                    volume = (row[5] as String).toDouble()
                )
            }
            Result.success(candles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun calculateIndicators(candles: List<Candle>): IndicatorResult {
        if (candles.isEmpty()) {
            return IndicatorResult(50.0, 0.0, 0.0, 0.0, 0.0, TradingSignal.HOLD)
        }

        val closes = candles.map { it.close }
        val currentClose = closes.last()

        // 1. حساب RSI المبسط (Relative Strength Index)
        var gains = 0.0
        var losses = 0.0
        for (i in 1 until closes.size) {
            val diff = closes[i] - closes[i - 1]
            if (diff >= 0) gains += diff else losses -= diff
        }
        val avgGain = if (closes.size > 1) gains / (closes.size - 1) else 0.0
        val avgLoss = if (closes.size > 1) losses / (closes.size - 1) else 1.0
        val rs = avgGain / (if (avgLoss == 0.0) 1.0 else avgLoss)
        val rsi = 100.0 - (100.0 / (1.0 + rs))

        // 2. حساب EMA (Exponential Moving Average) المبسط
        val ema = closes.takeLast(14).average()

        // 3. حساب Bollinger Bands المبسطة
        val sma20 = closes.takeLast(20).average()
        val variance = closes.takeLast(20).map { (it - sma20) * (it - sma20) }.average()
        val stdDev = kotlin.math.sqrt(if (variance.isNaN()) 0.0 else variance)
        val upperBand = sma20 + (2.0 * stdDev)
        val lowerBand = sma20 - (2.0 * stdDev)

        // 4. مؤشر MACD تقريبي
        val ema12 = closes.takeLast(12).average()
        val ema26 = closes.takeLast(26).average()
        val macd = ema12 - ema26

        // تحديد الإشارة بناءً على المؤشرات القوية
        val signal = when {
            rsi < 30.0 && currentClose <= lowerBand -> TradingSignal.BUY
            rsi > 70.0 && currentClose >= upperBand -> TradingSignal.SELL
            else -> TradingSignal.HOLD
        }

        return IndicatorResult(
            rsi = rsi,
            ema = ema,
            macd = macd,
            bollingerUpper = upperBand,
            bollingerLower = lowerBand,
            signal = signal
        )
    }

    override suspend fun executeManualTrade(
        symbol: String,
        type: String,
        amountPercentage: Double,
        price: Double
    ): Result<TradeOrder> {
        // محاكاة التنفيذ الفوري السبوت يدوياً (Spot Order Simulation) بناءً على الرصيد أو السعر اللحظي
        return try {
            val order = TradeOrder(
                orderId = UUID.randomUUID().toString(),
                symbol = symbol,
                type = if (type == "BUY") OrderType.BUY else OrderType.SELL,
                price = price,
                quantity = amountPercentage, // تمثل الكمية أو النسبة المئوية المختارة
                percentage = amountPercentage,
                timestamp = System.currentTimeMillis(),
                status = OrderStatus.COMPLETED
            )
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
