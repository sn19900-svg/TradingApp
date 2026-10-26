package com.trading.app.feature.assets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.feature.home.HomeViewModel

@Composable
fun AssetsScreen(viewModel: HomeViewModel) {
    val isDemoAccount by viewModel.isDemoAccount.collectAsState()
    val demoBalance by viewModel.demoBalance.collectAsState()
    val realBalance by viewModel.realBalance.collectAsState()
    val executedTrades by viewModel.executedTrades.collectAsState()

    val activeInvestmentsTotal = executedTrades.sumOf { it.amount }
    val currentCash = if (isDemoAccount) demoBalance else realBalance
    val totalAssets = currentCash + activeInvestmentsTotal

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "💰 الأصول والمحفظة", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "إجمالي الأصول اللحظي", color = Color.Gray, fontSize = 13.sp)
                Text(text = "${String.format("%.2f", totalAssets)} USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Divider(color = Color.DarkGray)
                Text(text = "• الكاش المتوافر (USDT): ${String.format("%.2f", currentCash)}", color = Color.LightGray, fontSize = 13.sp)
                Text(text = "• المستثمر بالصفقات: ${String.format("%.2f", activeInvestmentsTotal)} USDT", color = BinanceYellow, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "العملات المستثمر بها:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (executedTrades.isEmpty()) {
                    Text(text = "لا توجد أصول مستثمرة حالياً", color = Color.Gray, fontSize = 11.sp)
                } else {
                    executedTrades.forEach { tr ->
                        Text(text = "• ${tr.symbol} (${tr.type}) بقيمة ${String.format("%.2f", tr.amount)} USDT", color = ProfitGreen, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
