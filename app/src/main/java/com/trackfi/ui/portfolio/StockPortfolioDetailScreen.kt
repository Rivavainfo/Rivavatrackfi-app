package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.trackfi.ui.portfolio.components.CashBalanceSection
import com.trackfi.ui.portfolio.components.PortfolioMetricsTable
import kotlinx.coroutines.delay
import com.trackfi.ui.theme.PremiumGradientStart
import com.trackfi.ui.components.PremiumButton

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    initialFocus: String? = null,
    onBack: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val exchange = if (ticker == "RTX" || ticker == "WMT") "NYSE" else "NSE"

    val stockStates by viewModel.stockStates.collectAsState()

    LaunchedEffect(ticker) {
        viewModel.startPolling(listOf(ticker))
    }

    val stockData = stockStates[ticker]?.data

    val lastPrice = stockData?.c ?: (if (ticker == "RTX") 205.00 else 150.00)
    val changeValue = stockData?.d ?: (if (ticker == "RTX") 1.96 else -1.20)
    val pnlPercent = stockData?.dp ?: (if (ticker == "RTX") 0.95 else -0.40)
    val dayHigh = stockData?.h ?: (lastPrice + 5.0)
    val dayLow = stockData?.l ?: (lastPrice - 5.0)
    val openPrice = stockData?.o ?: lastPrice

    val isPositive = changeValue >= 0
    val pnl = changeValue * 2.99 // Simulated position P&L

    val scrollState = rememberScrollState()

    val companyProfiles by viewModel.companyProfiles.collectAsState()
    val companyNews by viewModel.companyNews.collectAsState()

    val profile = companyProfiles[ticker]?.profile
    val newsList = companyNews[ticker] ?: emptyList()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (profile != null) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (!profile.logo.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = profile.logo,
                            contentDescription = profile.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column {
                        Text(
                            text = profile.name ?: ticker,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile.finnhubIndustry ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            PortfolioMetricsTable(
                ticker = ticker,
                exchange = exchange,
                change = String.format("%s%.2f", if (isPositive) "+" else "", changeValue),
                position = "2.99",
                avgVolume = "6.10M",
                avgPrice = "119.31",
                lastPrice = String.format("%.2f", lastPrice),
                dayHigh = String.format("%.2f", dayHigh),
                dayLow = String.format("%.2f", dayLow),
                openPrice = String.format("%.2f", openPrice),
                costBasis = "356.74",
                pnl = String.format("%s%.2f", if (isPositive) "+" else "", pnl),
                pnlPercent = String.format("%s%.2f%%", if (isPositive) "+" else "", pnlPercent),
                unrealizedPnl = "71.8%",
                isPositive = isPositive,
                focusedMetric = initialFocus
            )

            CashBalanceSection(
                usdCash = "8.90",
                totalCash = "8.90"
            )

            if (newsList.isNotEmpty()) {
                com.trackfi.ui.components.SectionHeader(title = "Company News")
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(newsList.size) { index ->
                        NewsCard(news = newsList[index])
                    }
                }
            }

            val uriHandler = LocalUriHandler.current
            Spacer(modifier = Modifier.height(24.dp))
            PremiumButton(
                text = "View Full Stock Price",
                onClick = {
                    val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                    val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                    try {
                        uriHandler.openUri(url)
                    } catch (e: Exception) {
                        // Ignored if browser not found
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.ShowChart
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
