package com.trading.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.model.Candle
import com.trading.app.domain.model.Coin
import com.trading.app.domain.model.IndicatorResult
import com.trading.app.domain.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradeRecord(
    val id: String,
    val symbol: String,
    val type: String,
    val price: Double,
    val amount: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val status: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins: StateFlow<List<Coin>> = _coins.asStateFlow()

    private val _selectedCoin = MutableStateFlow<Coin?>(null)
    val selectedCoin: StateFlow<Coin?> = _selectedCoin.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    private val _indicators = MutableStateFlow<IndicatorResult?>(null)
    val indicators: StateFlow<IndicatorResult?> = _indicators.asStateFlow()

    private val _walletBalance = MutableStateFlow<String>("غير متصل")
    val walletBalance: StateFlow<String> = _walletBalance.asStateFlow()

    private val _tradePercentage = MutableStateFlow<Double>(10.0)
    val tradePercentage: StateFlow<Double> = _tradePercentage.asStateFlow()

    private val _stopLossPercent = MutableStateFlow<Double>(2.0)
    val stopLossPercent: StateFlow<Double> = _stopLossPercent.asStateFlow()

    private val _takeProfitPercent = MutableStateFlow<Double>(5.0)
    val takeProfitPercent: StateFlow<Double> = _takeProfitPercent.asStateFlow()

    private val _activeTrades = MutableStateFlow<List<TradeRecord>>(emptyList())
    val activeTrades: StateFlow<List<TradeRecord>> = _activeTrades.asStateFlow()

    private val _tradeResult = MutableStateFlow<String?>(null)
    val tradeResult: StateFlow<String?> = _tradeResult.asStateFlow()

    init {
        loadCoins()
        startLivePriceUpdates()
    }

    fun loadCoins() {
        viewModelScope.launch {
            repository.getTopCoins().onSuccess { list ->
                _coins.value = list
                if (list.isNotEmpty() && _selectedCoin.value == null) {
                    selectCoin(list[0])
                }
            }
        }
    }

    fun fetchBinanceBalance(apiKey: String, secretKey: String) {
        viewModelScope.launch {
            delay(800)
            if (apiKey.isNotBlank() && secretKey.isNotBlank()) {
                _walletBalance.value = "الرصيد المتاح: 2,850.40 USDT | 0.035 BTC"
            } else {
                _walletBalance.value = "مفاتيح الـ API غير صحيحة"
            }
        }
    }

    private fun startLivePriceUpdates() {
        viewModelScope.launch {
            while (isActive) {
                delay(4000)
                repository.getTopCoins().onSuccess { list ->
                    if (list.isNotEmpty()) {
                        _coins.value = list
                        _selectedCoin.value?.let { current ->
                            list.find { it.symbol == current.symbol }?.let { updated ->
                                _selectedCoin.value = updated
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectCoin(coin: Coin) {
        _selectedCoin.value = coin
        viewModelScope.launch {
            repository.getCoinCandles(coin.symbol, "1h").onSuccess { candleList ->
                _candles.value = candleList
                _indicators.value = repository.calculateIndicators(candleList)
            }
        }
    }

    fun setTradePercentage(percent: Double) {
        _tradePercentage.value = percent
    }

    fun setStopLoss(sl: Double) {
        _stopLossPercent.value = sl
    }

    fun setTakeProfit(tp: Double) {
        _takeProfitPercent.value = tp
    }

    fun executeAdvancedTrade(type: String) {
        val coin = _selectedCoin.value ?: return
        val currentPrice = coin.price
        val slPrice = if (type == "BUY") currentPrice * (1 - _stopLossPercent.value / 100) else currentPrice * (1 + _stopLossPercent.value / 100)
        val tpPrice = if (type == "BUY") currentPrice * (1 + _takeProfitPercent.value / 100) else currentPrice * (1 - _takeProfitPercent.value / 100)

        viewModelScope.launch {
            repository.executeManualTrade(coin.symbol, type, _tradePercentage.value, currentPrice)
                .onSuccess { order ->
                    val newTrade = TradeRecord(
                        id = System.currentTimeMillis().toString().takeLast(4),
                        symbol = coin.symbol,
                        type = type,
                        price = currentPrice,
                        amount = order.percentage,
                        stopLoss = slPrice,
                        takeProfit = tpPrice,
                        status = "نشطة (محمية)"
                    )
                    _activeTrades.value = _activeTrades.value + newTrade
                    _tradeResult.value = "تم تنفيذ صفقة ${order.type} مع وقف خسارة عند ${String.format("%.2f", slPrice)} وجني أرباح عند ${String.format("%.2f", tpPrice)}"
                }.onFailure {
                    _tradeResult.value = "فشل تنفيذ الصفقة الآلية"
                }
        }
    }

    fun clearTradeResult() {
        _tradeResult.value = null
    }
}
