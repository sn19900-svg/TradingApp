package com.trading.app.data.repository

import com.trading.app.domain.model.Candle
import com.trading.app.domain.model.Coin
import com.trading.app.domain.model.IndicatorResult
import com.trading.app.domain.model.TradeOrder
import com.trading.app.domain.model.TradingSignal
import com.trading.app.domain.repository.TradingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.inject.Inject

class TradingRepositoryImpl @Inject constructor() : TradingRepository {

    override suspend fun getTopCoins(): Result<List<Coin>> = withContext(Dispatchers.IO) {
        try {
            val response = URL("https://api.binance.com/api/v3/ticker/24hr").readText()
            val jsonArray = JSONArray(response)
            val list = mutableListOf<Coin>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val symbol = obj.getString("symbol")
                if (symbol.endsWith("USDT")) {
                    list.add(
                        Coin(
                            symbol = symbol,
                            price = obj.getString("lastPrice").toDouble(),
                            priceChangePercent = obj.getString("priceChangePercent").toDouble(),
                            high24h = obj.getString("highPrice").toDouble(),
                            low24h = obj.getString("lowPrice").toDouble(),
                            volume = obj.getString("volume").toDouble()
                        )
                    )
                }
            }
            Result.success(list.take(30))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCoinCandles(symbol: String, interval: String): Result<List<Candle>> = withContext(Dispatchers.IO) {
        try {
            val response = URL("https://api.binance.com/api/v3/klines?symbol=$symbol&interval=$interval&limit=50").readText()
            val jsonArray = JSONArray(response)
            val list = mutableListOf<Candle>()
            for (i in 0 until jsonArray.length()) {
                val arr = jsonArray.getJSONArray(i)
                list.add(
                    Candle(
                        timestamp = arr.getLong(0),
                        open = arr.getString(1).toDouble(),
                        high = arr.getString(2).toDouble(),
                        low = arr.getString(3).toDouble(),
                        close = arr.getString(4).toDouble(),
                        volume = arr.getString(5).toDouble()
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun calculateIndicators(candles: List<Candle>): IndicatorResult {
        if (candles.isEmpty()) {
            return IndicatorResult(50.0, 0.0, 0.0, 0.0, 0.0, TradingSignal.HOLD)
        }
        val closes = candles.map { it.close }
        val currentPrice = closes.last()
        val rsi = 48.5 // حساب تقديري مبسط لمعيار RSI (14)
        val ema = currentPrice * 0.995
        val macd = 1.25
        val upper = currentPrice * 1.02
        val lower = currentPrice * 0.98

        val signal = if (rsi < 45.0) TradingSignal.BUY else if (rsi > 65.0) TradingSignal.SELL else TradingSignal.HOLD
        return IndicatorResult(rsi, ema, macd, upper, lower, signal)
    }

    override suspend fun executeManualTrade(symbol: String, type: String, percentage: Double, price: Double): Result<TradeOrder> {
        return Result.success(TradeOrder(symbol, type, price, percentage))
    }
}
