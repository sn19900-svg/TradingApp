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

    private val _tradePercentage = MutableStateFlow<Double>(10.0)
    val tradePercentage: StateFlow<Double> = _tradePercentage.asStateFlow()

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
                val ind = repository.calculateIndicators(candleList)
                _indicators.value = ind
            }
        }
    }

    fun setTradePercentage(percent: Double) {
        _tradePercentage.value = percent
    }

    fun executeTrade(type: String) {
        val coin = _selectedCoin.value ?: return
        viewModelScope.launch {
            repository.executeManualTrade(coin.symbol, type, _tradePercentage.value, coin.price)
                .onSuccess { order ->
                    _tradeResult.value = "تم تنفيذ أمر ${order.type} بنجاح لـ ${order.symbol} بنسبة ${order.percentage.toInt()}% بسعر ${order.price}"
                }.onFailure {
                    _tradeResult.value = "فشل تنفيذ الصفقة"
                }
        }
    }

    fun clearTradeResult() {
        _tradeResult.value = null
    }
}
