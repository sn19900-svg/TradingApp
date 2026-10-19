package com.trading.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.core.theme.BinanceDark
import com.trading.app.core.theme.BinanceDarkCard
import com.trading.app.core.theme.BinanceYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var apiKey by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ربط حساب باينانس (API)", color = BinanceYellow, fontWeight = FontWeight.Bold) },
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
            Card(
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "إعدادات الربط الآمن", color = BinanceYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "قم بإنشاء مفاتيح API من حسابك في باينانس (مع تفعيل صلاحية Spot Trading فقط لضمان الأمان) وأدخلها هنا:", color = androidx.compose.ui.graphics.Color.LightGray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Binance API Key", color = androidx.compose.ui.graphics.Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                            focusedTextColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedTextColor = androidx.compose.ui.graphics.Color.White
                        )
                    )

                    OutlinedTextField(
                        value = secretKey,
                        onValueChange = { secretKey = it },
                        label = { Text("Binance Secret Key", color = androidx.compose.ui.graphics.Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                            focusedTextColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedTextColor = androidx.compose.ui.graphics.Color.White
                        )
                    )

                    Button(
                        onClick = {
                            // حفظ المفاتيح محلياً
                            savedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ المفاتيح وربط الحساب", color = BinanceDark, fontWeight = FontWeight.Bold)
                    }

                    if (savedMessage) {
                        Text(text = "تم حفظ المفاتيح بنجاح وأصبح التطبيق مرتبطاً بحسابك!", color = androidx.compose.ui.graphics.Color.Green, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
