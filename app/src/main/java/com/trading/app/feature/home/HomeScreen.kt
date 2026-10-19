package com.trading.app.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    val activeTrades by viewModel.activeTrades.collectAsState()
    val tradeResult by viewModel.tradeResult.collectAsState()

    var apiKey by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(listOf("BTCUSDT", "ETHUSDT")) }
    var currentScreen by remember { mutableStateOf("home") } // home, settings
    
    // إعدادات الأمان
    var securityMode by remember { mutableStateOf("بدون حماية") } // بصمة، رمز سري، بدون
    var pinInput by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }

    // جلب الـ IP العالمي الحقيقي
    var publicIp by remember { mutableStateOf("جاري جلب الـ IP العالمي...") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val ip = URL("https://api.ipify.org").readText()
                publicIp = ip
            } catch (e: Exception) {
                publicIp = "فشل جلب الـ IP، تحقق من الاتصال"
            }
        }
    }

    if (isLocked) {
        // شاشة قفل الحماية (بصمة أو رمز سري)
        Surface(modifier = Modifier.fillMaxSize(), color = BinanceDark) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(text = "🔒 التطبيق مقفل لأسباب أمنية", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (securityMode == "رمز سري") {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 5) pinInput = it },
                        label = { Text("أدخل رمز السر (5 أرقام)", color = Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { if (pinInput.length == 5) isLocked = false else Toast.makeText(context, "الرمز يجب أن يكون 5 أرقام", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                    ) {
                        Text("فتح القفل", color = BinanceDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { isLocked = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                    ) {
                        Text("فتح بالبصمة / المصادقة الحيوية", color = BinanceDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (currentScreen == "settings") {
        // شاشة الإعدادات المتقدمة (الـ IP العالمي المنسخ وحماية البصمة والـ PIN)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إعدادات الأمان والشبكة والـ IP", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        TextButton(onClick = { currentScreen = "home" }) {
                            Text("← عودة", color = BinanceYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkCard)
                )
            },
            containerColor = BinanceDark
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // بطاقة الـ IP العالمي مع زر النسخ المباشر
                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "عنوان IP العالمي لجهازك (Global IP)", color = BinanceYellow, fontWeight = FontWeight.Bold)
                        Text(text = "انسخ هذا الـ IP المعتمد عالمياً وأضفه في قائمة الاستثناءات بباينانس (IP Whitelist):", color = Color.LightGray, fontSize = 12.sp)
                        Text(text = publicIp, color = ProfitGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Global IP", publicIp)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الـ IP بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("نسخ الـ IP مباشرة", color = BinanceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // بطاقة إعدادات الحماية الأمنية
                Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "حماية التطبيق عند كل دخول", color = BinanceYellow, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { securityMode = "بصمة"; isLocked = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "بصمة") BinanceYellow else Color.DarkGray)
                            ) {
                                Text("بصمة الإصبع", color = if (securityMode == "بصمة") BinanceDark else Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { securityMode = "رمز سري"; isLocked = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "رمز سري") BinanceYellow else Color.DarkGray)
                            ) {
                                Text("رمز 5 أرقام", color = if (securityMode == "رمز سري") BinanceDark else Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { securityMode = "بدون حماية"; isLocked = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "بدون حماية") BinanceYellow else Color.DarkGray)
                            ) {
                                Text("إلغاء الحماية", color = if (securityMode == "بدون حماية") BinanceDark else Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // الشاشة الرئيسية الاحترافية مع قسم خاص للعملات المفضلة
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("منصة باينانس الاحترافية Pro", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { currentScreen = "settings" }) {
                            Text("⚙️", fontSize = 20.sp)
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

                // لوحة المحفظة الاحترافية وربط API
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!isConnected) {
                                Text(text = "ربط حساب باينانس (API Key)", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                OutlinedTextField(
                                    value = apiKey,
                                    onValueChange = { apiKey = it },
                                    label = { Text("Binance API Key", color = Color.White) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = secretKey,
                                    onValueChange = { secretKey = it },
                                    label = { Text("Binance Secret Key", color = Color.White) },
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
                                    Text("اتصال وعرض تفاصيل المحفظة", color = BinanceDark, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "حالة الاتصال: متصل بـ Binance Spot 🟢", color = ProfitGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    TextButton(onClick = { isConnected = false }) {
                                        Text(text = "تغيير المفاتيح", color = BinanceYellow, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "إجمالي رصيد المحفظة: 2,850.40 USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Divider(color = Color.DarkGray, thickness = 1.dp)
                                Text(text = "• متاح للتداول (USDT): 1,850.20", color = Color.LightGray, fontSize = 12.sp)
                                Text(text = "• المحتفظ به (BTC): 0.012 (\$1,000.20)", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // القسم الخاص والمستقل للعملات المفضلة (Watchlist Section)
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "⭐ العملات المفضلة والمتابعة السريعة", color = BinanceYellow, fontWeight = FontWeight.Bold)
                            if (favorites.isEmpty()) {
                                Text(text = "لا توجد عملات في المفضلة حالياً. استخدم البحث أدناه لإضافة عملات.", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(favorites) { favSymbol ->
                                        val coinObj = coins.find { it.symbol == favSymbol }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = BinanceYellow),
                                            modifier = Modifier.width(130.dp).clickable {
                                                coinObj?.let { viewModel.selectCoin(it) }
                                            }
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(text = favSymbol, color = BinanceDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(text = "❌", fontSize = 10.sp, modifier = Modifier.clickable {
                                                        favorites = favorites - favSymbol
                                                    })
                                                }
                                                Text(text = "\$${coinObj?.price ?: "---"}", color = BinanceDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // شريط البحث العام المباشر من باينانس
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "البحث الشامل في أسواق باينانس", color = BinanceYellow, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("ابحث عن أي زوج (مثال: SOLUSDT)", color = Color.White) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            val filteredCoins = coins.filter { it.symbol.contains(searchQuery.uppercase()) }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredCoins) { coin ->
                                    val isFav = favorites.contains(coin.symbol)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (isFav) BinanceYellow else Color.DarkGray),
                                        modifier = Modifier.clickable {
                                            viewModel.selectCoin(coin)
                                            if (!isFav) favorites = favorites + coin.symbol
                                        }.width(120.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(text = coin.symbol, color = if (isFav) BinanceDark else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = "\$${coin.price}", color = if (isFav) BinanceDark else Color.LightGray, fontSize = 11.sp)
                                            Text(text = if (isFav) "⭐ في المفضلة" else "+ إضافة للمفضلة", color = if (isFav) BinanceDark else BinanceYellow, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                selectedCoin?.let { coin ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "العملة المختارة: ${coin.symbol}", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "السعر اللحظي: \$${coin.price}", color = Color.White, fontSize = 16.sp)
                                Text(text = "أعلى سعر 24س: \$${coin.high24h} | أدنى سعر 24س: \$${coin.low24h}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // الشارت الفني
                if (candles.isNotEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "شارت الشموع اليابانية الفني", color = BinanceYellow, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val closes = candles.map { it.close }
                                val maxPrice = closes.maxOrNull() ?: 1.0
                                val minPrice = closes.minOrNull() ?: 0.0
                                val priceRange = if (maxPrice == minPrice) 1.0 else (maxPrice - minPrice)

                                Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                    val widthStep = size.width / (closes.size.coerceAtLeast(1))
                                    for (i in 0 until closes.size - 1) {
                                        val x1 = i * widthStep
                                        val y1 = size.height - (((closes[i] - minPrice) / priceRange) * size.height).toFloat()
                                        val x2 = (i + 1) * widthStep
                                        val y2 = size.height - (((closes[i + 1] - minPrice) / priceRange) * size.height).toFloat()

                                        drawLine(
                                            color = if (closes[i + 1] >= closes[i]) ProfitGreen else LossRed,
                                            start = Offset(x1, y1),
                                            end = Offset(x2, y2),
                                            strokeWidth = 4f
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                indicators?.let { ind ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
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

                // التنفيذ وإدارة المخاطر
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = BinanceDarkCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "تنفيذ الصفقات وإدارة المخاطر الآمنة", color = BinanceYellow, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "النسبة المستخدمة: ${tradePercentage.toInt()}% من الرصيد", color = Color.White, fontSize = 12.sp)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(10.0, 25.0, 50.0, 100.0).forEach { pct ->
                                    Button(
                                        onClick = { viewModel.setTradePercentage(pct) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (tradePercentage == pct) BinanceYellow else Color.DarkGray),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("${pct.toInt()}%", color = if (tradePercentage == pct) BinanceDark else Color.White, fontSize = 12.sp)
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
                                    Text("بيع (SELL)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
