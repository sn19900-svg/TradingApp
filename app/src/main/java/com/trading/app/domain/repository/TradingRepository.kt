package com.trading.app.domain.repository

import com.trading.app.domain.model.Candle
import com.trading.app.domain.model.Coin
import com.trading.app.domain.model.IndicatorResult
import com.trading.app.domain.model.TradeOrder

interface TradingRepository {
    suspend fun getTopCoins(): Result<List<Coin>>
    suspend fun getCoinCandles(symbol: String, interval: String): Result<List<Candle>>
    fun calculateIndicators(candles: List<Candle>): IndicatorResult
    suspend fun executeManualTrade(symbol: String, type: String, amountPercentage: Double, price: Double): Result<TradeOrder>
}
