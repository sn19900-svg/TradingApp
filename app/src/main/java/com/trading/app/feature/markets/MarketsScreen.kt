package com.trading.app.feature.markets

import androidx.compose.foundation.clickable
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
import com.trading.app.core.theme.LossRed
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.domain.model.Coin
import com.trading.app.feature.home.HomeViewModel

@Composable
fun MarketsScreen(viewModel: HomeViewModel, onCoinClick: (Coin) -> Unit) {
    val coins by viewModel.coins.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن عملة...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceDarkCard,
                unfocusedContainerColor = BinanceDarkCard,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val filteredList = if (searchQuery.isBlank()) coins else coins.filter { it.symbol.contains(searchQuery.uppercase()) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredList) { coin ->
                val isPositive = coin.priceChangePercent >= 0
                Card(
                    colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                    modifier = Modifier.fillMaxWidth().clickable { onCoinClick(coin) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${coin.symbol} / USDT", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "Vol: ${coin.volume}", color = Color.Gray, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(text = "\$${coin.price}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${if (isPositive) "+" else ""}${coin.priceChangePercent}%",
                                color = if (isPositive) ProfitGreen else LossRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
