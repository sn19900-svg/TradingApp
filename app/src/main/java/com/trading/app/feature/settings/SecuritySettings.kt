package com.trading.app.feature.settings

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    currentIp: String,
    onBack: () -> Unit
) {
    var securityMode by remember { mutableStateOf("بدون حماية") } // بدون حماية، بصمة، رمز سري
    var pinCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات الأمان والشبكة", color = BinanceYellow, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkCard)
            )
        },
        containerColor = BinanceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // قسم عرض الـ IP
            Card(
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "عنوان IP الخاص بجهازك", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "انسخ هذا الـ IP وأضفه في إعدادات باينانس (IP Whitelist) لتفعيل التداول الآمن:", color = Color.LightGray, fontSize = 12.sp)
                    Text(text = currentIp, color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // قسم الحماية والأمان
            Card(
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "حماية التطبيق عند الدخول", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = { securityMode = "بدون" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "بدون") BinanceYellow else Color.DarkGray)
                        ) {
                            Text("بدون حماية", color = if (securityMode == "بدون") BinanceDark else Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { securityMode = "بصمة" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "بصمة") BinanceYellow else Color.DarkGray)
                        ) {
                            Text("بصمة الإصبع", color = if (securityMode == "بصمة") BinanceDark else Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { securityMode = "PIN" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (securityMode == "PIN") BinanceYellow else Color.DarkGray)
                        ) {
                            Text("رمز 5 أرقام", color = if (securityMode == "PIN") BinanceDark else Color.White, fontSize = 11.sp)
                        }
                    }

                    if (securityMode == "PIN") {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 5) pinCode = it },
                            label = { Text("أدخل رمز سري من 5 أرقام", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ إعدادات الأمان والعودة", color = BinanceDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
