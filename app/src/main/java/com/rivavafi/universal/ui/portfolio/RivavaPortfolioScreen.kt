package com.rivavafi.universal.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rivavafi.universal.ui.components.PortfolioStockCard
import com.rivavafi.universal.ui.theme.PrimaryContainerSky
import com.rivavafi.universal.ui.components.SectionHeader
import com.rivavafi.universal.ui.theme.PremiumGradientStart
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Refresh
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.TertiaryEmerald
import com.rivavafi.universal.ui.theme.SecondaryPink
import com.rivavafi.universal.ui.theme.PrimarySky
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import android.view.WindowManager
import android.content.ContextWrapper
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

val portfolioItems = listOf(
    PortfolioItem("NSE", "IREDA", "Buy Rate: ₹32.00  •  Returns: 715%", "₹228.84"),
    PortfolioItem("NYSE", "RTX", "Buy Rate: $74.00  •  Returns: 62.8%", "$207.00")
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    viewModel: StockViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel(),
    marketViewModel: MarketViewModel = hiltViewModel(),
    portfolioViewModel: PortfolioViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
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

    val stockData by marketViewModel.stocks.collectAsState()
    val isLoading by marketViewModel.isLoading.collectAsState()
    val stockStates by viewModel.stockStates.collectAsState()
    val marketNews by viewModel.marketNews.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(portfolioItems.map { it.ticker })
        cryptoViewModel.startPolling(cryptoIds)
    }



    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 140.dp), // Provide enough bottom padding for the floating nav bar
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                // Custom Logo Header and Refresh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.refresh()
                        cryptoViewModel.refresh()
                    }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.rivavafi.universal.R.drawable.rivava_logo),
                        contentDescription = "Rivava Logo",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                // Total Valuation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TOTAL VALUATION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "$142,850.42",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(TertiaryEmerald.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = TertiaryEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+2.4%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TertiaryEmerald
                            )
                        }
                    }
                }
            }

            item {
                // Market News Hero Carousel
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                if (marketNews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val displayNews = marketNews.take(5)
                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { displayNews.size })

                    LaunchedEffect(pagerState.currentPage) {
                        if (displayNews.size > 1) {
                            delay(4000)
                            val nextPage = (pagerState.currentPage + 1) % displayNews.size
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }

                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp,
                        beyondBoundsPageCount = 1
                    ) { page ->
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))) {
                        val newsItem = displayNews[page]
                        val newsUrl = newsItem.url.ifBlank { "https://www.google.com/search?q=${newsItem.headline}" }
                        val newsImage = if (newsItem.image.isNotBlank()) newsItem.image else "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=60"
                        val newsHeadline = newsItem.headline

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    try {
                                        uriHandler.openUri(newsUrl)
                                    } catch (e: Exception) {}
                                }
                        ) {
                            coil.compose.AsyncImage(
                                model = newsImage,
                                contentDescription = "Abstract digital visualization",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.4f
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                    )
                            )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "MARKET NEWS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = SecondaryPink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = newsHeadline,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                lineHeight = 32.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "READ ANALYSIS",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimarySky
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = PrimarySky,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Pager Indicators
                                if (displayNews.size > 1) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(displayNews.size) { index ->
                                            val isSelected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .height(6.dp)
                                                    .width(if (isSelected) 16.dp else 6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(if (isSelected) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
                }
            }


            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(title = "My Portfolio")

                    if (isLoading && stockData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (stockData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Updating...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Ensure both RTX and IREDA show up. If one is missing from API, we use cached version (MarketRepo handles this).
                            // If MarketRepo returns empty for one, we force "Updating..."
                            val iredaItem = stockData.find { it.symbol == "IREDA.NS" }
                            val rtxItem = stockData.find { it.symbol == "RTX" }

                            val displayList = listOf(
                                Pair("NSE", iredaItem ?: com.rivavafi.universal.data.network.YahooStock("IREDA.NS", null, null)),
                                Pair("NYSE", rtxItem ?: com.rivavafi.universal.data.network.YahooStock("RTX", null, null))
                            )

                            displayList.forEach { (exchange, stock) ->
                                val price = stock.regularMarketPrice ?: 0.0
                                val changePercent = stock.regularMarketChangePercent ?: 0.0

                                val isPositive = changePercent >= 0
                                val prefix = if (exchange == "NSE") "₹" else "$"
                                val formattedPrice = if (price > 0) "$prefix${String.format(java.util.Locale.US, "%.2f", price)}" else "Updating..."
                                val formattedChange = "${if(isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", changePercent)}%"

                                PortfolioStockCard(
                                    exchange = exchange,
                                    ticker = stock.symbol.replace(".NS", ""),
                                    companyName = "",
                                    marketPrice = formattedPrice,
                                    isPositive = isPositive,
                                    percentageChange = formattedChange,
                                    onValueClick = { focus ->
                                        onNavigateToDetail(stock.symbol.replace(".NS", ""), focus)
                                    }
                                )
                            }
                        }
                    }
                }
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
                Text(
                    text = "Rates may not be updated, kindly check the redirect to see real prices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NewsCard(news: com.rivavafi.universal.domain.api.FinnhubNewsResponse) {
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
                coil.compose.AsyncImage(
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
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val isPositive = data.change24h >= 0
    val color = if (isPositive) com.rivavafi.universal.ui.theme.EmeraldGreen else com.rivavafi.universal.ui.theme.VibrantRed
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                val url = "https://www.google.com/search?q=$id+crypto+price"
                try {
                    uriHandler.openUri(url)
                } catch(e: Exception) {}
            }
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
