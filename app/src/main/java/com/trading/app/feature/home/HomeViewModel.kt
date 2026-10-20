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
    val type: String,
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

    private val _isDemoAccount = MutableStateFlow(true)
    val isDemoAccount: StateFlow<Boolean> = _isDemoAccount.asStateFlow()

    private val _demoBalance = MutableStateFlow(100.0)
    val demoBalance: StateFlow<Double> = _demoBalance.asStateFlow()

    private val _realBalance = MutableStateFlow(2540.80)
    val realBalance: StateFlow<Double> = _realBalance.asStateFlow()

    private val _tradePercentage = MutableStateFlow<Double>(10.0)
    val tradePercentage: StateFlow<Double> = _tradePercentage.asStateFlow()

    private val _isAutoInvestActive = MutableStateFlow(false)
    val isAutoInvestActive: StateFlow<Boolean> = _isAutoInvestActive.asStateFlow()

    private val _autoInvestBudget = MutableStateFlow(50.0)
    val autoInvestBudget: StateFlow<Double> = _autoInvestBudget.asStateFlow()

    private val _autoInvestLogs = MutableStateFlow<List<String>>(emptyList())
    val autoInvestLogs: StateFlow<List<String>> = _autoInvestLogs.asStateFlow()

    private val _executedTrades = MutableStateFlow<List<ExecutedTrade>>(emptyList())
    val executedTrades: StateFlow<List<ExecutedTrade>> = _executedTrades.asStateFlow()

    init {
        loadCoins()
        startBackgroundServices()
    }

    fun toggleAccountType(isDemo: Boolean) {
        _isDemoAccount.value = isDemo
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

    private fun startBackgroundServices() {
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
        val currentBalance = if (_isDemoAccount.value) _demoBalance.value else _realBalance.value
        val investedAmount = currentBalance * (_tradePercentage.value / 100.0)

        if (_isDemoAccount.value) {
            if (_demoBalance.value < investedAmount) return
            _demoBalance.value -= investedAmount
        } else {
            if (_realBalance.value < investedAmount) return
            _realBalance.value -= investedAmount
        }

        val newTrade = ExecutedTrade(
            id = System.currentTimeMillis().toString().takeLast(5),
            symbol = coin.symbol,
            type = type,
            entryPrice = coin.price,
            amount = investedAmount,
            percentage = _tradePercentage.value,
            timestamp = android.text.format.DateFormat.format("HH:mm:ss", java.util.Date()).toString()
        )
        _executedTrades.value = listOf(newTrade) + _executedTrades.value
    }

    fun closeTrade(tradeId: String) {
        val trade = _executedTrades.value.find { it.id == tradeId } ?: return
        val currentCoin = _coins.value.find { it.symbol == trade.symbol }
        val exitPrice = currentCoin?.price ?: trade.entryPrice
        val priceDiff = if (trade.type.contains("BUY")) exitPrice - trade.entryPrice else trade.entryPrice - exitPrice
        val pnlPercent = (priceDiff / trade.entryPrice) * 100
        val pnlAmount = (pnlPercent / 100) * trade.amount
        val returnAmount = trade.amount + pnlAmount

        if (_isDemoAccount.value) {
            _demoBalance.value += returnAmount
        } else {
            _realBalance.value += returnAmount
        }

        _executedTrades.value = _executedTrades.value.filter { it.id != tradeId }
    }

    fun toggleAutoInvest(active: Boolean, budget: Double) {
        _isAutoInvestActive.value = active
        _autoInvestBudget.value = budget
    }
}
