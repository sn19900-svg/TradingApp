package com.trading.app.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDark
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.LossRed
import com.trading.app.core.theme.ProfitGreen
import com.trading.app.domain.model.TradingSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val coins by viewModel.coins.collectAsState()
    val selectedCoin by viewModel.selectedCoin.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val indicators by viewModel.indicators.collectAsState()
    val isDemoAccount by viewModel.isDemoAccount.collectAsState()
    val demoBalance by viewModel.demoBalance.collectAsState()
    val realBalance by viewModel.realBalance.collectAsState()
    val tradePercentage by viewModel.tradePercentage.collectAsState()
    val executedTrades by viewModel.executedTrades.collectAsState()
    val isAutoInvestActive by viewModel.isAutoInvestActive.collectAsState()
    val autoInvestBudget by viewModel.autoInvestBudget.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(listOf("BTCUSDT", "ETHUSDT")) }
    var selectedBottomTab by remember { mutableStateOf("الأسواق") }
    var marketTab by remember { mutableStateOf("التداول الفوري") }
    var currentScreen by remember { mutableStateOf("main") }

    var pinInput by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }

    var selectedAutoCoin by remember { mutableStateOf("BTCUSDT") }
    var botBudgetInput by remember { mutableStateOf("50") }

    var publicIp by remember { mutableStateOf("جاري جلب الـ IP...") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                publicIp = URL("https://api.ipify.org").readText()
            } catch (e: Exception) {
                publicIp = "تعذر الاتصال"
            }
        }
    }

    if (isLocked) {
        Surface(modifier = Modifier.fillMaxSize(), color = BinanceDark) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(text = "🔒 المنصة مقفلة أمنياً", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 5) pinInput = it },
                    label = { Text("أدخل رمز السر (5 أرقام)", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (pinInput.length == 5) {
                            isLocked = false
                            pinInput = ""
                        } else {
                            Toast.makeText(context, "الرجاء إدخال 5 أرقام صحيحة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                ) {
                    Text("فتح القفل", color = BinanceDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (currentScreen == "settings") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("الإعدادات والأمان واستثناءات الـ IP", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        TextButton(onClick = { currentScreen = "main" }) {
                            Text("← عودة", color = BinanceYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkCard)
                )
            },
            containerColor = BinanceDark
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "عنوان IP العالمي لجهازك (Global IP)", color = BinanceYellow, fontWeight = FontWeight.Bold)
                            Text(text = "قم بنسخ هذا الـ IP وإضافته في قائمة الاستثناءات في باينانس:", color = Color.LightGray, fontSize = 12.sp)
                            Text(text = publicIp, color = ProfitGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Global IP", publicIp))
                                    Toast.makeText(context, "تم نسخ الـ IP بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("نسخ الـ IP مباشرة", color = BinanceDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث عن أي عملة (مثال: BTC)", color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = BinanceDarkCard,
                                unfocusedContainerColor = BinanceDarkCard,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = "settings" }) {
                            Text("⚙️", fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDark)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = BinanceDarkCard) {
                    NavigationBarItem(
                        selected = selectedBottomTab == "الرئيسية",
                        onClick = { selectedBottomTab = "الرئيسية" },
                        icon = { Text("🏠", fontSize = 14.sp) },
                        label = { Text("الرئيسية", color = if (selectedBottomTab == "الرئيسية") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "الأسواق",
                        onClick = { selectedBottomTab = "الأسواق" },
                        icon = { Text("📊", fontSize = 14.sp) },
                        label = { Text("الأسواق", color = if (selectedBottomTab == "الأسواق") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "استثمار تلقائي",
                        onClick = { selectedBottomTab = "استثمار تلقائي" },
                        icon = { Text("🤖", fontSize = 14.sp) },
                        label = { Text("تلقائي", color = if (selectedBottomTab == "استثمار تلقائي") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "صفقاتي",
                        onClick = { selectedBottomTab = "صفقاتي" },
                        icon = { Text("📈", fontSize = 14.sp) },
                        label = { Text("صفقاتي", color = if (selectedBottomTab == "صفقاتي") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "تداول",
                        onClick = { selectedBottomTab = "تداول" },
                        icon = { Text("💱", fontSize = 14.sp) },
                        label = { Text("تداول", color = if (selectedBottomTab == "تداول") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "الأصول",
                        onClick = { selectedBottomTab = "الأصول" },
                        icon = { Text("💰", fontSize = 14.sp) },
                        label = { Text("الأصول", color = if (selectedBottomTab == "الأصول") BinanceYellow else Color.Gray, fontSize = 9.sp) }
                    )
                }
            },
            containerColor = BinanceDark
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDemoAccount) "🧪 الحساب التجريبي (100$)" else "💼 الحساب الحقيقي",
                            color = BinanceYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { viewModel.toggleAccountType(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDemoAccount) BinanceYellow else Color.DarkGray),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("تجريبي", color = if (isDemoAccount) BinanceDark else Color.White, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.toggleAccountType(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isDemoAccount) BinanceYellow else Color.DarkGray),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("حقيقي", color = if (!isDemoAccount) BinanceDark else Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                when (selectedBottomTab) {
                    "الرئيسية" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "مرحباً بك في منصة التداول الفوري", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "حالة الاتصال والخدمة نشطة 🟢", color = ProfitGreen, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "الرصيد المتاح: ${if (isDemoAccount) String.format("%.2f", demoBalance) else String.format("%.2f", realBalance)} USDT", color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                    "الأصول" -> {
                        item {
                            val activeInvestmentsTotal = executedTrades.sumOf { it.amount }
                            val totalAssets = (if (isDemoAccount) demoBalance else realBalance) + activeInvestmentsTotal

                            Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "إجمالي الأصول اللحظي", color = Color.Gray, fontSize = 13.sp)
                                    Text(text = "${String.format("%.2f", totalAssets)} USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    Divider(color = Color.DarkGray)
                                    Text(text = "• الكاش المتوافر: ${if (isDemoAccount) String.format("%.2f", demoBalance) else String.format("%.2f", realBalance)}", color = Color.LightGray, fontSize = 13.sp)
                                    Text(text = "• المستثمر بالصفقات: ${String.format("%.2f", activeInvestmentsTotal)} USDT", color = BinanceYellow, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    "استثمار تلقائي" -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "🤖 بوت الاستثمار التلقائي في الخلفية", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "اختر العملة وحدد المبلغ ليعمل البوت تلقائياً ويرسل لك إشعارات 🚨", color = Color.LightGray, fontSize = 11.sp)
                                    
                                    Text(text = "العملة المختارة: $selectedAutoCoin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(coins.take(6)) { c ->
                                            Button(
                                                onClick = { selectedAutoCoin = c.symbol },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedAutoCoin == c.symbol) BinanceYellow else Color.DarkGray),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(c.symbol, color = if (selectedAutoCoin == c.symbol) BinanceDark else Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = botBudgetInput,
                                        onValueChange = { botBudgetInput = it },
                                        label = { Text("المبلغ المخصص (USDT)", color = Color.White) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                val b = botBudgetInput.toDoubleOrNull() ?: 50.0
                                                viewModel.toggleAutoInvest(true, b)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("تشغيل البوت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.toggleAutoInvest(false, 0.0) },
                                            colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("إيقاف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "الحالة: ${if (isAutoInvestActive) "🟢 يعمل في الخلفية للعملة $selectedAutoCoin" else "🔴 متوقف"} ", color = if (isAutoInvestActive) ProfitGreen else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    "صفقاتي" -> {
                        item {
                            Text(text = "إدارة الصفقات المفتوحة", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        if (executedTrades.isEmpty()) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                        Text(text = "لا توجد صفقات منفذة حالياً", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }
                        } else {
                            items(executedTrades) { trade ->
                                val currentCoinObj = coins.find { data -> data.symbol == trade.symbol }
                                val currentPrice = currentCoinObj?.price ?: trade.entryPrice
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
                                                Text("إغلاق يدوي", color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                        Divider(color = Color.DarkGray, thickness = 0.5.dp)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "سعر الدخول: \$${trade.entryPrice}", color = Color.White, fontSize = 12.sp)
                                            Text(text = "السعر الحالي: \$${currentPrice}", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "تداول" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "تنفيذ الصفقات اليدوية الفورية", color = BinanceYellow, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "النسبة: ${tradePercentage.toInt()}%", color = Color.White)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(10.0, 25.0, 50.0, 100.0).forEach { pct ->
                                                Button(
                                                    onClick = { viewModel.setTradePercentage(pct) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (tradePercentage == pct) BinanceYellow else Color.DarkGray),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("${pct.toInt()}%", color = if (tradePercentage == pct) BinanceDark else Color.White)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Button(
                                                onClick = { viewModel.executeAdvancedTrade("BUY") },
                                                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("شراء (BUY)", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { viewModel.executeAdvancedTrade("SELL") },
                                                colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("بيع (SELL)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }

                                selectedCoin?.let { coin ->
                                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = "التحليل الفني: ${coin.symbol}", color = BinanceYellow, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = "السعر اللحظي: \$${coin.price}", color = Color.White)
                                            
                                            indicators?.let { ind ->
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = "• مؤشر RSI: ${String.format("%.2f", ind.rsi)}", color = Color.White, fontSize = 12.sp)
                                                Text(text = "• مؤشر EMA: ${String.format("%.2f", ind.ema)}", color = Color.White, fontSize = 12.sp)
                                                Text(text = "• التوصية الناتجة: ${ind.signal}", color = if (ind.signal == TradingSignal.BUY) ProfitGreen else LossRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "الأسواق" -> {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                listOf("المفضلة", "التداول الفوري").forEach { tab ->
                                    TextButton(onClick = { marketTab = tab }) {
                                        Text(
                                            text = tab,
                                            color = if (marketTab == tab) BinanceYellow else Color.Gray,
                                            fontWeight = if (marketTab == tab) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        val displayList = if (marketTab == "المفضلة") {
                            coins.filter { favorites.contains(it.symbol) }
                        } else {
                            if (searchQuery.isBlank()) coins else coins.filter { it.symbol.contains(searchQuery.uppercase()) }
                        }

                        items(displayList) { coin ->
                            val isFav = favorites.contains(coin.symbol)
                            val isPositive = coin.priceChangePercent >= 0
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.selectCoin(coin)
                                    selectedBottomTab = "تداول"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(text = "${coin.symbol} / USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (isFav) "⭐" else "☆", fontSize = 14.sp, modifier = Modifier.clickable {
                                                favorites = if (isFav) favorites - coin.symbol else favorites + coin.symbol
                                            })
                                        }
                                    }

                                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text(text = String.format("%.2f", coin.price), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Surface(
                                        color = if (isPositive) ProfitGreen else LossRed,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "${if (isPositive) "+" else ""}${coin.priceChangePercent}%",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
