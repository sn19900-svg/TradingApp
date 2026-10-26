package com.trading.app.feature.bot

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
import com.trading.app.core.theme.LossRed
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.feature.home.HomeViewModel

@Composable
fun BotScreen(viewModel: HomeViewModel) {
    val isAutoInvestActive by viewModel.isAutoInvestActive.collectAsState()
    val autoInvestBudget by viewModel.autoInvestBudget.collectAsState()
    var budgetInput by remember { mutableStateOf(autoInvestBudget.toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "🤖 إعدادات بوت الاستثمار التلقائي", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "مراقبة السوق وتنفيذ الصفقات تلقائياً في الخلفية بناءً على مؤشرات RSI و EMA.", color = Color.LightGray, fontSize = 12.sp)

                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("مبلغ الاستثمار (USDT)", color = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val b = budgetInput.toDoubleOrNull() ?: 50.0
                            viewModel.toggleAutoInvest(true, b)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تشغيل البوت", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.toggleAutoInvest(false, 0.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إيقاف البوت", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الحالة الحالية: ${if (isAutoInvestActive) "🟢 يعمل في الخلفية" else "🔴 متوقف"}",
                    color = if (isAutoInvestActive) ProfitGreen else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
