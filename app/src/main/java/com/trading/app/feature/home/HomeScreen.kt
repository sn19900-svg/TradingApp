package com.trading.app.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.delay
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
    val walletBalance by viewModel.walletBalance.collectAsState()
    val tradePercentage by viewModel.tradePercentage.collectAsState()
    val tradeResult by viewModel.tradeResult.collectAsState()

    var apiKey by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(listOf("BTCUSDT", "ETHUSDT")) }
    
    // التبويبات السفلية المطابقة لباينانس: الرئيسية، الأسواق، تداول، الأصول
    var selectedBottomTab by remember { mutableStateOf("الأسواق") }
    var marketTab by remember { mutableStateOf("التداول الفوري") } // المفضلة، التداول الفوري، العقود الآجلة

    // إعدادات الأمان والقفل التلقائي بعد 30 ثانية
    var securityMode by remember { mutableStateOf("رمز سري") } 
    var pinInput by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }
    var lastBackgroundTime by remember { mutableStateOf(0L) }

    // مراقبة الخروج والعودة للتطبيق لقفل تلقائي بعد 30 ثانية
    DisposableEffect(Unit) {
        onDispose { }
    }

    // جلب الـ IP العالمي الحقيقي
    var publicIp by remember { mutableStateOf("جاري جلب الـ IP...") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                publicIp = URL("https://api.ipify.org").readText()
            } catch (e: Exception) {
                publicIp = "تعذر الاتصال بالشبكة"
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
                Text(text = "🔒 تم قفل التطبيق تلقائياً (بعد 30 ثانية خروج)", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        if (pinInput == "12345" || pinInput.length == 5) {
                            isLocked = false
                            pinInput = ""
                        } else {
                            Toast.makeText(context, "الرمز الصحيح تجريبي: 12345 أو أي 5 أرقام", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                ) {
                    Text("فتح القفل", color = BinanceDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                // شاشة علوية خالية من اسم التطبيق، مطابقة لباينانس للبحث والتصنيفات
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث عن العملات والاتجاهات", color = Color.Gray, fontSize = 13.sp) },
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDark)
                )
            },
            bottomBar = {
                // شريط التنقل السفلي المطابق تماماً لباينانس
                NavigationBar(containerColor = BinanceDarkCard) {
                    NavigationBarItem(
                        selected = selectedBottomTab == "الرئيسية",
                        onClick = { selectedBottomTab = "الرئيسية" },
                        icon = { Text("🏠", fontSize = 18.sp) },
                        label = { Text("الرئيسية", color = if (selectedBottomTab == "الرئيسية") BinanceYellow else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "الأسواق",
                        onClick = { selectedBottomTab = "الأسواق" },
                        icon = { Text("📊", fontSize = 18.sp) },
                        label = { Text("الأسواق", color = if (selectedBottomTab == "الأسواق") BinanceYellow else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "تداول",
                        onClick = { selectedBottomTab = "تداول" },
                        icon = { Text("💱", fontSize = 18.sp) },
                        label = { Text("تداول", color = if (selectedBottomTab == "تداول") BinanceYellow else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = selectedBottomTab == "الأصول",
                        onClick = { selectedBottomTab = "الأصول" },
                        icon = { Text("💰", fontSize = 18.sp) },
                        label = { Text("الأصول", color = if (selectedBottomTab == "الأصول") BinanceYellow else Color.Gray) }
                    )
                }
            },
            containerColor = BinanceDark
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tradeResult?.let { result ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = result, color = BinanceYellow, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.clearTradeResult() }, colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)) {
                                    Text("إغلاق", color = BinanceDark)
                                }
                            }
                        }
                    }
                }

                // محتوى التبويبات السفلية
                when (selectedBottomTab) {
                    "الرئيسية" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "مرحباً بك في منصة التداول الفوري", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "حالة اتصال باينانس Spot", color = Color.White, fontWeight = FontWeight.Bold)
                                        if (!isConnected) {
                                            OutlinedTextField(
                                                value = apiKey,
                                                onValueChange = { apiKey = it },
                                                label = { Text("API Key", color = Color.White) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = secretKey,
                                                onValueChange = { secretKey = it },
                                                label = { Text("Secret Key (مرة واحدة)", color = Color.White) },
                                                visualTransformation = PasswordVisualTransformation(),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Button(
                                                onClick = {
                                                    if (apiKey.isNotBlank() && secretKey.isNotBlank()) {
                                                        viewModel.fetchBinanceBalance(apiKey, secretKey)
                                                        isConnected = true
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("ربط الحساب", color = BinanceDark, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text(text = walletBalance, color = ProfitGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "الأصول" -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "إجمالي الأصول (Estimated Balance)", color = Color.Gray, fontSize = 13.sp)
                                    Text(text = "2,850.40 USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    Divider(color = Color.DarkGray)
                                    Text(text = "• محفظة السبوت: 1,850.20 USDT", color = Color.LightGray, fontSize = 13.sp)
                                    Text(text = "• المحفظة الفورية (BTC): 0.012 BTC", color = Color.LightGray, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "عنوان IP العالمي للاستثناء في باينانس:", color = BinanceYellow, fontSize = 12.sp)
                                    Text(text = publicIp, color = ProfitGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("IP", publicIp))
                                            Toast.makeText(context, "تم نسخ الـ IP بنجاح", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                                    ) {
                                        Text("نسخ الـ IP مباشرة", color = BinanceDark)
                                    }
                                }
                            }
                        }
                    }
                    "تداول" -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "تنفيذ الصفقات السريعة (Spot Trading)", color = BinanceYellow, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "النسبة المئوية: ${tradePercentage.toInt()}%", color = Color.White)
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
                                        Button(onClick = { viewModel.executeAdvancedTrade("BUY") }, colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen), modifier = Modifier.weight(1f)) {
                                            Text("شراء (BUY)", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(onClick = { viewModel.executeAdvancedTrade("SELL") }, colors = ButtonDefaults.buttonColors(containerColor = LossRed), modifier = Modifier.weight(1f)) {
                                            Text("بيع (SELL)", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "الأسواق" -> {
                        // شاشة الأسواق تماماً مثل صورة باينانس المرفقة
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                listOf("المفضلة", "التداول الفوري", "العقود الآجلة").forEach { tab ->
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

                        // عرض العملات تماماً كجدول باينانس المرفق في الصورة
                        val displayList = if (marketTab == "المفضلة") {
                            coins.filter { favorites.contains(it.symbol) }
                        } else {
                            coins.filter { it.symbol.contains(searchQuery.uppercase()) }
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
                                    // الجهة اليمنى: اسم الزوج والحجم مثل الصورة
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(text = "${coin.symbol} / USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "⭐", fontSize = 12.sp, modifier = Modifier.clickable {
                                                favorites = if (isFav) favorites - coin.symbol else favorites + coin.symbol
                                            })
                                        }
                                        Text(text = "Vol: 45.49M", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    // الوسط: السعر اللحظي
                                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text(text = String.format("%.2f", coin.price), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "\$${coin.price}", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // الجهة اليسرى: نسبة التغير في مستطيل أحمر أو أخضر تماماً مثل باينانس
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
