package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trackfi.ui.portfolio.components.CashBalanceSection
import com.trackfi.ui.portfolio.components.PortfolioMetricsTable
import kotlinx.coroutines.delay
import com.trackfi.ui.theme.PremiumGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    onBack: () -> Unit
) {
    // Mock Data based on ticker
    val exchange = if (ticker == "RTX" || ticker == "WMT") "NYSE" else "NSE"

    // States that will simulate real-time updates
    var lastPrice by remember { mutableStateOf(if (ticker == "RTX") 205.00 else 150.00) }
    var changeValue by remember { mutableStateOf(if (ticker == "RTX") 1.96 else -1.20) }
    var pnl by remember { mutableStateOf(if (ticker == "RTX") 5.86 else -2.30) }
    var pnlPercent by remember { mutableStateOf(if (ticker == "RTX") 0.95 else -0.40) }

    val isPositive = changeValue >= 0

    // Simulate 10-15 second updates
    LaunchedEffect(ticker) {
        while (true) {
            delay(12000)
            val fluctuation = (Math.random() - 0.5) * 2.0 // Random value between -1.0 and 1.0
            lastPrice += fluctuation
            changeValue += fluctuation
            pnl += fluctuation * 2
            pnlPercent += fluctuation * 0.1
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumGradientStart.copy(alpha = 0.1f),
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PortfolioMetricsTable(
                    ticker = ticker,
                    exchange = exchange,
                    change = String.format("%s%.2f", if (isPositive) "+" else "", changeValue),
                    position = "2.99",
                    avgVolume = "6.10M",
                    avgPrice = "119.31",
                    lastPrice = String.format("%.2f", lastPrice),
                    costBasis = "356.74",
                    pnl = String.format("%s%.2f", if (isPositive) "+" else "", pnl),
                    pnlPercent = String.format("%s%.2f%%", if (isPositive) "+" else "", pnlPercent),
                    unrealizedPnl = "71.8%",
                    isPositive = isPositive
                )
            }

            item {
                CashBalanceSection(
                    usdCash = "8.90",
                    totalCash = "8.90"
                )
            }
        }
    }
}
