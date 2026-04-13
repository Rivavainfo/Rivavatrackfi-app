package com.rivavafi.universal.ui.portfolio

import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val stockState by viewModel.stockState.collectAsState()
    val cryptoState by viewModel.cryptoState.collectAsState()
    val newsState by viewModel.newsState.collectAsState()

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

    LaunchedEffect(Unit) {
        viewModel.startUpdates()
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

            // News Carousel
            item {
                when (newsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Updating news...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    is UiState.Success -> {
                        val newsList = (newsState as UiState.Success<List<News>>).data
                        val displayNews = newsList.take(5)
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { displayNews.size })
                        val uriHandler = LocalUriHandler.current

                        LaunchedEffect(pagerState.currentPage) {
                            if (displayNews.size > 1) {
                                kotlinx.coroutines.delay(3000)
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
                                val newsUrl = newsItem.url
                                val newsImage = if (newsItem.imageUrl.isNotBlank()) newsItem.imageUrl else "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=60"
                                val newsHeadline = newsItem.title

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            try {
                                                if (newsUrl.isNotBlank()) {
                                                    uriHandler.openUri(newsUrl)
                                                }
                                            } catch (e: Exception) {}
                                        }
                                ) {
                                    AsyncImage(
                                        model = newsImage,
                                        contentDescription = "Finance News Image",
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
                                            text = "FINANCE NEWS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            ),
                                            color = com.rivavafi.universal.ui.theme.SecondaryPink
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
                                                    text = "READ MORE",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = com.rivavafi.universal.ui.theme.PrimarySky
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = com.rivavafi.universal.ui.theme.PrimarySky,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

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
                                                                .background(if (isSelected) com.rivavafi.universal.ui.theme.PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
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
            }

            // Stocks Section
            item {
                SectionHeader(title = "Stocks")
                when (stockState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Updating stocks...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            (stockState as UiState.Success<List<MarketItem>>).data.forEach { stock ->
                                MarketCard(item = stock, onNavigateToDetail = onNavigateToDetail)
                            }
                        }
                    }
                }
            }

            // Crypto Section
            item {
                SectionHeader(title = "Crypto")
                when (cryptoState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        Text("Updating crypto stream...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is UiState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            (cryptoState as UiState.Success<List<MarketItem>>).data.forEach { crypto ->
                                MarketCard(item = crypto, onNavigateToDetail = onNavigateToDetail)
                            }
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
            val formattedTicker = if (item.symbol.contains("IREDA")) "IREDA" else if (item.symbol.contains("RTX")) "RTX" else item.symbol
            onNavigateToDetail(formattedTicker, focus)
        }
    )
}
