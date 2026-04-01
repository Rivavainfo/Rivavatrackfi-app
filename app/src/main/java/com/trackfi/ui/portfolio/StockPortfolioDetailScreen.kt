package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.trackfi.ui.portfolio.components.CashBalanceSection
import com.trackfi.ui.portfolio.components.PortfolioMetricsTable
import kotlinx.coroutines.delay
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.foundation.shape.CircleShape
import com.trackfi.ui.theme.PremiumGradientStart
import com.trackfi.ui.components.PremiumButton

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    onBack: () -> Unit,
    onNavigateToPdfViewer: (() -> Unit)? = null
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("$ticker ($exchange)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add to favorites */ }) {
                        Icon(Icons.Default.Star, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
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

            Spacer(modifier = Modifier.height(24.dp))

            val uriHandler = LocalUriHandler.current
            PremiumButton(
                text = "View Full Stock Price",
                onClick = {
                    val exchangeSuffix = if (exchange.equals("NSE", ignoreCase = true)) "NSE" else "NYSE"
                    val url = "https://www.google.com/search?q=$ticker+stock+price+$exchangeSuffix"
                    try {
                        uriHandler.openUri(url)
                    } catch (e: Exception) {
                        // Ignored if browser not found
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.ShowChart,
                colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
            )

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Metric Card 3
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("MARKET OPEN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("$${String.format("%.2f", lastPrice - 12.15)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("09:30:01 EST", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // Metric Card 4
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("52W CHANGE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("+284.15%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = com.trackfi.ui.theme.EmeraldGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Exponential Trend", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Wealth Insights Card / PDF Viewer for IREDA
            item {
                if (ticker == "IREDA") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(com.trackfi.ui.theme.DeepBlueVariant, com.trackfi.ui.theme.EmeraldGreen.copy(alpha = 0.8f))
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Text("Investment Thesis", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Color.White))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("IPO Price", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("₹32", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Selling Price", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("₹228.84", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Listing Gain", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("56.25%", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Returns", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("715%", color = com.trackfi.ui.theme.EmeraldGreen, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))
                                }
                                Button(
                                    onClick = { onNavigateToPdfViewer?.invoke() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = com.trackfi.ui.theme.DeepBlueVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("View PDF", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(com.trackfi.ui.theme.LightPink.copy(alpha = 0.7f), com.trackfi.ui.theme.DeepBlueVariant)
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Premium Analysis", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Color.White))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Unlock Rivava AI price predictions for 2024", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = com.trackfi.ui.theme.LightPink),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("UPGRADE NOW", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Redirect to External
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                            val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                            try { uriHandler.openUri(url) } catch (e: Exception) { }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp)
                    ) {
                        Text("VIEW FULL STOCK PRICE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    }
}
