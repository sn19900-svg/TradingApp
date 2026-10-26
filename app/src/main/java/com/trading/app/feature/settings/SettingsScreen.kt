package com.trading.app.feature.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDark
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow
import com.trading.app.core.theme.ProfitGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات وأمان الـ IP", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
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
                        Text(text = "استخدم هذا الـ IP لإضافته في قائمة الاستثناءات في إعدادات أمان حسابك على باينانس:", color = Color.LightGray, fontSize = 12.sp)
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
                            Text("نسخ الـ IP", color = BinanceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
