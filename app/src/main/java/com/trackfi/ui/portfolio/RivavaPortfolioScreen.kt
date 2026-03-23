package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.trackfi.domain.api.FinnhubNewsResponse
import com.trackfi.ui.components.PortfolioStockCard
import com.trackfi.ui.components.SectionHeader
import com.trackfi.ui.theme.EmeraldGreen
import com.trackfi.ui.theme.VibrantRed
import com.trackfi.ui.theme.glassMorphism
import java.util.Locale

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: Double,
    val prefix: String = "",
)

val initialPortfolioItems = listOf(
    PortfolioItem("NSE", "HAL", "Hindustan Aeronautics Ltd", 3995.00, "₹"),
    PortfolioItem("NSE", "TATAMOTORS", "Tata Motors (PV/CV)", 335.35, "₹"),
    PortfolioItem("Nasdaq 100", "RTX", "RTX Corporation", 207.00, "$"),
    PortfolioItem("Nasdaq 100", "WMT", "Walmart Inc.", 125.12, "$")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    viewModel: StockViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel()
) {
    val stockStates by viewModel.stockStates.collectAsState()
    val marketNews by viewModel.marketNews.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()
    val uriHandler = LocalUriHandler.current

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(initialPortfolioItems.map { it.ticker })
        cryptoViewModel.startPolling(cryptoIds)
    }

    Scaffold(
        containerColor = Color(0xFF131313),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF353535)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Rivava Portfolio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = Color(0xFF98CBFF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131313),
                    titleContentColor = Color(0xFF98CBFF)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Net Worth Section ---
            item {
                Column {
                    Text(
                        "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = Color(0xFFBEC7D4)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$142,850", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                        Text(".42", style = MaterialTheme.typography.titleLarge, color = Color(0xFF98CBFF))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF00E471).copy(alpha = 0.1f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF00E471), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+12.4%", style = MaterialTheme.typography.labelMedium, color = Color(0xFF00E471))
                    }
                }
            }

            // --- Market News ---
            item {
                SectionHeader(title = "Market News")
                if (marketNews.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(marketNews) { news -> NewsCard(news = news) }
                    }
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                }
            }

            // --- Crypto Section ---
            item {
                SectionHeader(title = "Crypto Assets")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    cryptoIds.forEach { id ->
                        cryptoStates[id]?.let { data ->
                            item { CryptoCard(id = id, data = data) }
                        }
                    }
                }
            }

            // --- Equities Portfolio ---
            item {
                SectionHeader(title = "Equities Portfolio")
                Text("Real-time Tracking", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            items(initialPortfolioItems) { item ->
                val state = stockStates[item.ticker]
                val currency = if (item.exchange == "NSE") "₹" else "$"

                val displayPrice = state?.data?.let {
                    currency + String.format(Locale.getDefault(), "%.2f", it.c)
                } ?: (item.prefix + item.marketPrice.toString())

                val displayChange = state?.data?.let {
                    val sign = if (it.dp >= 0) "+" else ""
                    "$sign${String.format(Locale.getDefault(), "%.2f", it.dp)}%"
                } ?: "0.00%"

                val isPositive = state?.data?.let { it.dp >= 0 } ?: true

                PortfolioStockCard(
                    exchange = item.exchange,
                    ticker = item.ticker,
                    companyName = item.companyName,
                    marketPrice = displayPrice,
                    isPremium = true,
                    isPositive = isPositive,
                    percentageChange = displayChange,
                    onValueClick = { focus -> onNavigateToDetail(item.ticker, focus) },
                    modifier = Modifier.clickable { 
                        val url = "https://www.google.com/finance/quote/${item.ticker}:${if(item.exchange=="NSE") "NSE" else "NASDAQ"}"
                        try { uriHandler.openUri(url) } catch(_: Exception) {}
                        onNavigateToDetail(item.ticker, null) 
                    }
                )
            }
        }
    }
}

@Composable
fun NewsCard(news: FinnhubNewsResponse) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { try { uriHandler.openUri(news.url) } catch (_: Exception) {} }
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (news.image.isNotBlank()) {
                AsyncImage(
                    model = news.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = news.source, style = MaterialTheme.typography.bodySmall, color = Color(0xFF98CBFF))
        }
    }
}

@Composable
fun CryptoCard(id: String, data: CryptoData) {
    val isPositive = data.change24h >= 0
    Card(
        modifier = Modifier
            .width(160.dp)
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(id.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("$${String.format("%.2f", data.price)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "${if (isPositive) "+" else ""}${String.format("%.2f", data.change24h)}%",
                color = if (isPositive) EmeraldGreen else VibrantRed,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}