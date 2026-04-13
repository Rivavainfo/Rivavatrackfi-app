package com.rivavafi.universal.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val marketItems by viewModel.marketItems.collectAsState()
    val newsItems by viewModel.newsItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startRealtimeUpdates()
        viewModel.startNewsUpdates()
    }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is android.app.Activity) break
            ctx = ctx.baseContext
        }
        val window = (ctx as? android.app.Activity)?.window
        window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Text(
                    text = "Market Dashboard",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                item {
                    SectionHeader(title = "Stocks")
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        marketItems.filter { it.type == "stock" }.forEach { stock ->
                            MarketCard(item = stock, onNavigateToDetail = onNavigateToDetail)
                        }
                    }
                }

                item {
                    SectionHeader(title = "Crypto")
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        marketItems.filter { it.type == "crypto" }.forEach { crypto ->
                            MarketCard(item = crypto, onNavigateToDetail = onNavigateToDetail)
                        }
                    }
                }

                item {
                    SectionHeader(title = "Market News")
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        newsItems.forEach { news ->
                            DashboardNewsCard(news = news)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketCard(item: MarketItem, onNavigateToDetail: (ticker: String, focus: String?) -> Unit) {
    val isPositive = item.change >= 0
    val exchange = if (item.type == "crypto") "CRYPTO" else if (item.symbol.contains(".NS") || item.symbol == "IREDA") "NSE" else "NYSE"
    val prefix = if (exchange == "NSE") "₹" else "$"

    com.rivavafi.universal.ui.components.PortfolioStockCard(
        exchange = exchange,
        ticker = item.symbol,
        companyName = if (item.type == "crypto") "Crypto Asset" else "Stock Asset",
        marketPrice = "$prefix${String.format(Locale.US, "%.2f", item.price)}",
        isPositive = isPositive,
        percentageChange = "${if(isPositive) "+" else ""}${String.format(Locale.US, "%.2f", item.percentChange)}%",
        onValueClick = { focus ->
            // If it's IREDA or RTX, clicking goes to PDF/Detail
            val formattedTicker = if (item.symbol.contains("IREDA")) "IREDA" else if (item.symbol.contains("RTX")) "RTX" else item.symbol
            onNavigateToDetail(formattedTicker, focus)
        }
    )
}

@Composable
fun DashboardNewsCard(news: News) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                try {
                    uriHandler.openUri(news.url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (news.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = news.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val timeAgo = try {
                    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    format.format(Date(news.publishedAt * 1000))
                } catch(e: Exception) { "" }

                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
