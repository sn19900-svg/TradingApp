package com.trading.app.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header

interface BinanceApi {

    @GET("api/v3/ticker/24hr")
    suspend fun get24hrTicker(): List<BinanceTickerDto>

    @GET("api/v3/klines")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1h",
        @Query("limit") limit: Int = 100
    ): List<List<Any>>

    @POST("api/v3/order")
    suspend fun executeOrder(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("side") side: String, // BUY أو SELL
        @Query("type") type: String = "MARKET", // أمر سبيوت فوري
        @Query("quantity") quantity: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): BinanceOrderResponseDto

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

data class BinanceOrderResponseDto(
    val orderId: Long,
    val symbol: String,
    val status: String,
    val executedQty: String
)
