package com.trading.app.feature.trades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.LossRed
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.feature.home.HomeViewModel

@Composable
fun TradesScreen(viewModel: HomeViewModel) {
    val executedTrades by viewModel.executedTrades.collectAsState()
    val coins by viewModel.coins.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "📈 صفقاتي المفتوحة والمنفذة", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        if (executedTrades.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(text = "لا توجد صفقات حالياً", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(executedTrades) { trade ->
                    val currentCoin = coins.find { it.symbol == trade.symbol }
                    val currentPrice = currentCoin?.price ?: trade.entryPrice
                    val priceDiff = if (trade.type.contains("BUY")) currentPrice - trade.entryPrice else trade.entryPrice - currentPrice
                    val pnlPercent = (priceDiff / trade.entryPrice) * 100
                    val pnlAmount = (pnlPercent / 100) * trade.amount
                    val isProfit = pnlPercent >= 0

                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "${trade.symbol} (${trade.type})", color = if (trade.type.contains("BUY")) ProfitGreen else LossRed, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { viewModel.closeTrade(trade.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("إغلاق الصفقة", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            Divider(color = Color.DarkGray, thickness = 0.5.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "سعر الدخول: \$${trade.entryPrice}", color = Color.White, fontSize = 12.sp)
                                Text(text = "السعر الحالي: \$${currentPrice}", color = Color.White, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "الكمية: ${String.format("%.2f", trade.amount)} USDT", color = Color.LightGray, fontSize = 12.sp)
                                Text(
                                    text = "الربح/الخسارة: ${if (isProfit) "+" else ""}${String.format("%.2f", pnlAmount)} (${String.format("%.2f", pnlPercent)}%)",
                                    color = if (isProfit) ProfitGreen else LossRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
