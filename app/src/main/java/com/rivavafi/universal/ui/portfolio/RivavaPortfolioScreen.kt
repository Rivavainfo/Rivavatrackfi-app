package com.rivavafi.universal.ui.portfolio

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.components.PortfolioStockCard
import com.rivavafi.universal.ui.components.SectionHeader
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.VibrantRed
import com.rivavafi.universal.ui.theme.glassMorphism
import java.util.Locale

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    viewModel: StockViewModel = hiltViewModel(),
    alphaViewModel: AlphaVantageViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel(),
    portfolioViewModel: PortfolioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        var ctx = context
        while (ctx is ContextWrapper && ctx !is Activity) {
            ctx = ctx.baseContext
        }

        val window = (ctx as? Activity)?.window
        window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    if (showUnlockDialog) {
        PremiumUnlockDialog(
            userName = "",
            onDismiss = { showUnlockDialog = false },
            onUnlockSuccess = {
                prefs.edit().putBoolean("portfolio_unlocked", true).apply()
                isUnlocked = true
                showUnlockDialog = false
            },
            onPayClick = {
                paymentLauncher.launch(Intent(context, PaymentActivity::class.java))
                showUnlockDialog = false
            }
        )
    }

    val marketNews by viewModel.marketNews.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()
    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(emptyList())
        cryptoViewModel.startPolling(cryptoIds)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
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
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.rivava_logo),
                        contentDescription = "Rivava Logo",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                if (marketNews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(newsItems) { newsItem ->
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        runCatching { uriHandler.openUri(newsItem.url) }
                                    }
                                    .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (newsItem.image.isNotBlank()) {
                                        AsyncImage(
                                            model = newsItem.image,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
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

                    val liveStocks = portfolioViewModel.liveStocks.collectAsState().value

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        liveStocks.forEach { stock ->
                            val isIndian = stock.symbol == "IREDA"
                            val currency = if (isIndian) "₹" else "$"
                            val exchange = if (isIndian) "NSE" else "NYSE"
                            val company = if (isIndian) "IREDA" else "Raytheon Technologies"

                                val isIndian = stock.symbol?.contains(".BSE") == true || stock.symbol?.contains(".NS") == true
                                val resolvedExchange = if (isIndian) "NSE" else "NYSE"
                                val priceFormatted = stock.price?.let { if (isIndian) "₹$it" else "$$it" } ?: "Data unavailable"

                            PortfolioStockCard(
                                exchange = exchange,
                                ticker = stock.symbol,
                                companyName = company,
                                marketPrice = displayPrice,
                                isPositive = isPos,
                                percentageChange = displayChange,
                                onValueClick = { focus -> onNavigateToDetail(stock.symbol, focus) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Crypto Assets")
                if (cryptoStates.isNotEmpty()) {
                    val availableCryptoIds = cryptoIds.filter { cryptoStates.containsKey(it) }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(availableCryptoIds) { id ->
                            cryptoStates[id]?.let { crypto ->
                                CryptoCard(id = id, data = crypto)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Loading crypto...",
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
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CryptoCard(id: String, data: CryptoData) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val isPositive = data.change24h >= 0
    val color = if (isPositive) EmeraldGreen else VibrantRed

    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                runCatching {
                    uriHandler.openUri("https://www.google.com/search?q=$id+crypto+price")
                }
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

@Composable
fun CuratedNewsCard(title: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { runCatching { uriHandler.openUri(url) } }
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Read News",
                tint = PrimarySky,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
