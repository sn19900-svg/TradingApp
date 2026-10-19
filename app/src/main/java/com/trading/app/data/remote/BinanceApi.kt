package com.trading.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    @GET("api/v3/ticker/24hr")
    suspend fun get24hrTicker(): List<BinanceTickerDto>

    @GET("api/v3/klines")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1h",
        @Query("limit") limit: Int = 100
    ): List<List<Any>>

    companion object {
        const val BASE_URL = "https://api.binance.com/"
    }
}

data class BinanceTickerDto(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String,
    val highPrice: String,
    val lowPrice: String
)
