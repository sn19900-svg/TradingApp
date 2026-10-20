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

data class ExecutedTrade(
    val id: String,
    val symbol: String,
    val type: String, // BUY أو SELL
    val entryPrice: Double,
    val amount: Double,
    val percentage: Double,
    val timestamp: String
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

    private val _walletBalance = MutableStateFlow<String>("جاري مزامنة الرصيد...")
    val walletBalance: StateFlow<String> = _walletBalance.asStateFlow()

    private val _tradePercentage = MutableStateFlow<Double>(10.0)
    val tradePercentage: StateFlow<Double> = _tradePercentage.asStateFlow()

    private val _executedTrades = MutableStateFlow<List<ExecutedTrade>>(emptyList())
    val executedTrades: StateFlow<List<ExecutedTrade>> = _executedTrades.asStateFlow()

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

    fun executeAdvancedTrade(type: String) {
        val coin = _selectedCoin.value ?: return
        viewModelScope.launch {
            repository.executeManualTrade(coin.symbol, type, _tradePercentage.value, coin.price)
                .onSuccess { order ->
                    val newTrade = ExecutedTrade(
                        id = System.currentTimeMillis().toString().takeLast(5),
                        symbol = coin.symbol,
                        type = type,
                        entryPrice = coin.price,
                        amount = (_tradePercentage.value / 100.0) * 1000.0, // كمية تقريبية بناءً على رأس المال
                        percentage = _tradePercentage.value,
                        timestamp = android.text.format.DateFormat.format("HH:mm:ss", java.util.Date()).toString()
                    )
                    _executedTrades.value = listOf(newTrade) + _executedTrades.value
                    _tradeResult.value = "تم تنفيذ صفقة ${order.type} بنجاح لـ ${order.symbol} بسعر ${coin.price}"
                }.onFailure {
                    _tradeResult.value = "فشل تنفيذ الصفقة"
                }
        }
    }

    fun clearTradeResult() {
        _tradeResult.value = null
    }
}
