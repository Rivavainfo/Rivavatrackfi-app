package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trackfi.ui.components.PortfolioStockCard
import com.trackfi.ui.components.SectionHeader
import com.trackfi.ui.theme.PremiumGradientStart
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.ui.text.style.TextOverflow

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

val portfolioItems = listOf(
    PortfolioItem("NSE", "HAL", "Hindustan Aeronautics Ltd", "₹3,995.00"),
    PortfolioItem("NSE", "TATAMOTORS", "Tata Motors (PV/CV)", "₹335.35"),
    PortfolioItem("NYSE", "RTX", "RTX Corporation", "$207.00"),
    PortfolioItem("NYSE", "WMT", "Walmart Inc.", "$125.12")
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

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(portfolioItems.map { it.ticker })
        cryptoViewModel.startPolling(cryptoIds)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Rivava Portfolio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumGradientStart.copy(alpha = 0.1f),
                    titleContentColor = MaterialTheme.colorScheme.primary
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
                SectionHeader(
                    title = "Market News"
                )
                if (marketNews.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(marketNews) { news ->
                            NewsCard(news = news)
                        }
                    }
                } else {
                    Text(
                        "Loading news...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader(
                    title = "Crypto Assets"
                )
                if (cryptoStates.isNotEmpty()) {
                    val availableCryptoIds = cryptoIds.filter { cryptoStates.containsKey(it) }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(availableCryptoIds) { id ->
                            cryptoStates[id]?.let { crypto ->
                                CryptoCard(id = id, data = crypto)
                            }
                        }
                    }
                } else {
                    Text(
                        "Loading crypto...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader(
                    title = "Equities Portfolio"
                )
                Text(
                    text = "Strictly For Educational Purposes",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = com.trackfi.ui.theme.LightPink
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(portfolioItems) { item ->
                val state = stockStates[item.ticker]
                val currency = if (item.exchange == "NSE") "₹" else "$"

                // Use live data if available, else fall back to initial static mock string
                val displayPrice = state?.data?.let {
                    currency + String.format(Locale.getDefault(), "%.2f", it.c)
                } ?: item.marketPrice

                val displayChange = state?.data?.let {
                    val sign = if (it.dp >= 0) "+" else ""
                    "$sign${String.format(Locale.getDefault(), "%.2f", it.dp)}%"
                } ?: "+0.00%"

                val isPositive = state?.data?.let { it.dp >= 0 } ?: true

                PortfolioStockCard(
                    exchange = item.exchange,
                    ticker = item.ticker,
                    companyName = item.companyName,
                    marketPrice = displayPrice,
                    isPremium = true,
                    isPositive = isPositive,
                    percentageChange = displayChange,
                    onValueClick = { focus ->
                        onNavigateToDetail(item.ticker, focus)
                    },
                    modifier = Modifier.clickable { onNavigateToDetail(item.ticker, null) }
                )
            }
        }
    }
}

@Composable
fun NewsCard(news: com.trackfi.domain.api.FinnhubNewsResponse) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(news.url)
                } catch (e: Exception) {}
            }
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (news.image.isNotBlank()) {
                AsyncImage(
                    model = news.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = news.source,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CryptoCard(id: String, data: CryptoData) {
    val isPositive = data.change24h >= 0
    val color = if (isPositive) com.trackfi.ui.theme.EmeraldGreen else com.trackfi.ui.theme.VibrantRed
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = id.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", data.price)}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", data.change24h)}%",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
