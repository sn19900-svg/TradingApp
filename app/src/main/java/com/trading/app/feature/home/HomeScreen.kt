package com.trading.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDark
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.LossRed
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.domain.model.TradingSignal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val coins by viewModel.coins.collectAsState()
    val selectedCoin by viewModel.selectedCoin.collectAsState()
    val indicators by viewModel.indicators.collectAsState()
    val tradePercentage by viewModel.tradePercentage.collectAsState()
    val tradeResult by viewModel.tradeResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تداول باينانس اللحظي", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkCard)
            )
        },
        containerColor = BinanceDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            tradeResult?.let { result ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = result, color = BinanceYellow, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.clearTradeResult() },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                            ) {
                                Text("إغلاق", color = BinanceDark)
                            }
                        }
                    }
                }
            }

            item {
                Text("العملات المتاحة", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(coins) { coin ->
                        val isSelected = selectedCoin?.symbol == coin.symbol
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BinanceYellow else BinanceDarkCard
                            ),
                            modifier = Modifier
                                .clickable { viewModel.selectCoin(coin) }
                                .width(130.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = coin.symbol,
                                    color = if (isSelected) BinanceDark else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\$${coin.price}",
                                    color = if (isSelected) BinanceDark else Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${if (coin.priceChangePercent >= 0) "+" else ""}${coin.priceChangePercent}%",
                                    color = if (coin.priceChangePercent >= 0) ProfitGreen else LossRed,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            selectedCoin?.let { coin ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "العملة المختارة: ${coin.symbol}", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "السعر اللحظي: \$${coin.price}", color = Color.White, fontSize = 16.sp)
                            Text(text = "أعلى سعر 24س: \$${coin.high24h} | أدنى سعر 24س: \$${coin.low24h}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            indicators?.let { ind ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "المؤشرات الفنية القوية (4 مؤشرات)", color = BinanceYellow, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "مؤشر RSI: ${String.format("%.2f", ind.rsi)}", color = Color.White)
                            Text(text = "مؤشر EMA: ${String.format("%.2f", ind.ema)}", color = Color.White)
                            Text(text = "مؤشر MACD: ${String.format("%.2f", ind.macd)}", color = Color.White)
                            Text(text = "البولنجر باند: ${String.format("%.2f", ind.bollingerUpper)} / ${String.format("%.2f", ind.bollingerLower)}", color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            val signalColor = when (ind.signal) {
                                TradingSignal.BUY -> ProfitGreen
                                TradingSignal.SELL -> LossRed
                                TradingSignal.HOLD -> Color.Yellow
                            }
                            Text(text = "التوصية الناتجة: ${ind.signal}", color = signalColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "تنفيذ الصفقات اليدوية السبوت", color = BinanceYellow, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "النسبة المئوية المحددة من الرصيد: ${tradePercentage.toInt()}%", color = Color.White)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10.0, 25.0, 50.0, 100.0).forEach { pct ->
                                Button(
                                    onClick = { viewModel.setTradePercentage(pct) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (tradePercentage == pct) BinanceYellow else Color.DarkGray
                                    ),
                                    modifier = Modifier.weight(1.0)
                                ) {
                                    Text("${pct.toInt()}%", color = if (tradePercentage == pct) BinanceDark else Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeTrade("BUY") },
                                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                                modifier = Modifier.weight(1.0)
                            ) {
                                Text("شراء (BUY)", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.executeTrade("SELL") },
                                colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                                modifier = Modifier.weight(1.0)
                            ) {
                                Text("بيع (SELL)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
