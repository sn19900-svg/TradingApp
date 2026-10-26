package com.trading.app

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.trading.app.feature.home.HomeScreen
import com.trading.app.feature.home.HomeViewModel

@Composable
fun AppRoot(viewModel: HomeViewModel = hiltViewModel()) {
    HomeScreen(viewModel = viewModel)
}
