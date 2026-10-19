package com.trading.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.trading.app.core.theme.TradingAppTheme
import com.trading.app.feature.home.HomeScreen
import com.trading.app.feature.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradingAppTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
