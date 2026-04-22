package com.rivavafi.universal.ui.portfolio

import android.content.Context
import android.content.Intent
import android.content.ContextWrapper
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OpenInNew
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.TertiaryEmerald
import com.rivavafi.universal.ui.theme.SecondaryPink
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.VibrantRed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import kotlinx.coroutines.delay
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    onBack: () -> Unit = {},
    viewModel: StockViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
    var isUnlocked by remember { mutableStateOf(prefs.getBoolean("portfolio_unlocked", false)) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    val paymentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isUnlocked = true
            showUnlockDialog = false
        }
    }



    if (!isUnlocked) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFFF4C91).copy(alpha = 0.18f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Rivava Portfolio Locked",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Unlock premium insights and advanced portfolio tracking.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showUnlockDialog = true },
                        modifier = Modifier
                            .widthIn(min = 220.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4C91),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                    ) {
                        Text(
                            "Unlock Premium",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
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
                    val intent = Intent(context, PaymentActivity::class.java)
                    paymentLauncher.launch(intent)
                    showUnlockDialog = false
                }
            )
        }
        return
    }

    val stockStates by viewModel.stockStates.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(listOf("IREDA.NS", "RTX"))
        cryptoViewModel.startPolling(cryptoIds)
    }

    // moved down

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


            }



            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    var showLogsDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "My Portfolio")

                        val hasErrors = stockStates.values.any { it.error != null }
                        val statusColor = if (hasErrors) VibrantRed else EmeraldGreen

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showLogsDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasErrors) "Degraded" else "Live",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                    }

                    if (showLogsDialog) {
                        var screenshotsAllowed by remember { mutableStateOf(false) }
                        AlertDialog(
                            onDismissRequest = { showLogsDialog = false },
                            title = { Text("API Diagnostic Logs") },
                            text = {
                                LazyColumn {
                                    items(stockStates.entries.toList()) { (symbol, state) ->
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            Text(text = "Symbol: $symbol", fontWeight = FontWeight.Bold)
                                            Text(text = "Source: ${state.source.name}")
                                            Text(text = "Error: ${state.error ?: "None"}", color = if (state.error != null) VibrantRed else PrimarySky)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val view = androidx.compose.ui.platform.LocalView.current
                                    TextButton(onClick = {
                                        try {
                                            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
                                            val canvas = android.graphics.Canvas(bitmap)
                                            view.draw(canvas)
                                            val file = java.io.File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.png")
                                            val fos = java.io.FileOutputStream(file)
                                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                                            fos.close()
                                            android.widget.Toast.makeText(context, "Saved to ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Capture failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Text("Capture Screenshot")
                                    }

                                    val activity = context as? android.app.Activity ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
                                    TextButton(onClick = {
                                        screenshotsAllowed = !screenshotsAllowed
                                        if (screenshotsAllowed) {
                                            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                                            android.widget.Toast.makeText(context, "Screenshots Enabled", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            activity?.window?.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
                                            android.widget.Toast.makeText(context, "Screenshots Disabled", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Text(if (screenshotsAllowed) "Disable System SS" else "Enable System SS")
                                    }

                                    TextButton(onClick = { showLogsDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(100)
                                visible = true
                            }

                            val nseStocks = listOf("IREDA.NS")
                            val nyseStocks = listOf("RTX")

                            // Helper to render a stock card
                            @Composable
                            fun RenderStockCard(symbol: String, index: Int) {
                                val normalizedSymbol = if (symbol == "IREDA") "IREDA.NS" else symbol
                                val stockState = stockStates[normalizedSymbol]
                                val quote = stockState?.data

                                val isIreda = normalizedSymbol == "IREDA.NS"
                                val ticker = if (isIreda) "IREDA" else "RTX"
                                val companyName = if (isIreda) "IREDA" else "Raytheon Technologies"
                                val exchange = if (isIreda) "NSE" else "NYSE"
                                val currency = "₹"

                                val price = quote?.c ?: if (isIreda) 150.0 else 100.0
                                val previousClose = quote?.pc ?: if (isIreda) 148.0 else 99.0
                                val change = price - previousClose
                                val changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
                                val isPositive = change >= 0

                                val displayPrice = currency + String.format(Locale.getDefault(), "%.2f", price)
                                val displayAbsChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", change)}"
                                val displayPctChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", changePercent)}%"

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(
                                        initialOffsetY = { 50 },
                                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100)
                                    ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = index * 100))
                                ) {
                                    PortfolioStockCard(
                                        exchange = exchange,
                                        ticker = ticker,
                                        companyName = companyName,
                                        marketPrice = displayPrice,
                                        isPositive = isPositive,
                                        absoluteChange = displayAbsChange,
                                        percentageChange = displayPctChange,
                                        onValueClick = { focus ->
                                            onNavigateToDetail(ticker, focus)
                                        },
                                        modifier = Modifier.width(300.dp)
                                    )
                                }
                            }

                            // NSE Section
                            Column {
                                Text(
                                    "NSE Stocks",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(nseStocks.size) { index ->
                                        RenderStockCard(nseStocks[index], index)
                                    }
                                }
                            }

                            // NYSE Section
                            Column {
                                Text(
                                    "NYSE Stocks",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(nyseStocks.size) { index ->
                                        RenderStockCard(nyseStocks[index], index + nseStocks.size)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Crypto Assets"
                )
                val fallbackCryptoData = mapOf(
                    "bitcoin" to CryptoData(price = 7330590.0, change24h = 3.47),
                    "ethereum" to CryptoData(price = 224359.0, change24h = 3.77),
                    "solana" to CryptoData(price = 8252.12, change24h = 3.11)
                )
                val cryptoToDisplay = if (cryptoStates.isNotEmpty() && !cryptoStates.values.all { it.price <= 0.0 }) {
                    fallbackCryptoData + cryptoStates
                } else {
                    fallbackCryptoData
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cryptoIds.forEach { id ->
                        CryptoCard(id = id, data = cryptoToDisplay[id] ?: fallbackCryptoData.getValue(id))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                SectionHeader(title = "Market News")
                Spacer(modifier = Modifier.height(16.dp))
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                val indianMarket = listOf(
                    NewsItem("Business Standard", "Latest Financial News from India", "https://www.business-standard.com/", "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600&q=80"),
                    NewsItem("The Economic Times", "Market Updates and Business News", "https://economictimes.indiatimes.com/", "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80")
                )
                val usMarket = listOf(
                    NewsItem("The Wall Street Journal", "US Markets and Global Business", "https://www.wsj.com/", "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=600&q=80"),
                    NewsItem("Bloomberg", "Finance, Stock Market, and Business News", "https://www.bloomberg.com/", "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=600&q=80"),
                    NewsItem("The New York Times", "Business and Economy Updates", "https://www.nytimes.com/section/business", "https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?w=600&q=80")
                )
                val internationalMarket = listOf(
                    NewsItem("Financial Times", "Global Economy and Market News", "https://www.ft.com/", "https://images.unsplash.com/photo-1444653614773-995cb1ef9efa?w=600&q=80"),
                    NewsItem("The Economist", "World News, Politics, Economics", "https://www.economist.com/", "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600&q=80")
                )

                MarketNewsSection("🇮🇳 INDIAN MARKET", Color(0xFFFF4C91), indianMarket, uriHandler)
                Spacer(modifier = Modifier.height(24.dp))
                MarketNewsSection("🇺🇸 US MARKET", Color(0xFF3B82F6), usMarket, uriHandler)
                Spacer(modifier = Modifier.height(24.dp))
                MarketNewsSection("🌍 INTERNATIONAL MARKET", Color(0xFF34D399), internationalMarket, uriHandler)
            }


            item {
                Text(
                    text = "Live prices refresh every 30 seconds. Tap a card to cross-check on market sources.",
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
fun CryptoCard(id: String, data: CryptoData) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val isPositive = data.change24h >= 0
    val color = if (isPositive) Color(0xFF34D399) else Color(0xFFFF4C91)
    val inrFormatter = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val symbol = when (id.lowercase()) {
        "bitcoin" -> "BTC"
        "ethereum" -> "ETH"
        "solana" -> "SOL"
        else -> id.uppercase(Locale.getDefault())
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable {
                val url = "https://www.google.com/search?q=$id+crypto+price"
                try {
                    uriHandler.openUri(url)
                } catch(e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = id.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = inrFormatter.format(data.price),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
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
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
@Composable
fun NewsCard(news: com.rivavafi.universal.domain.api.FinnhubNewsResponse) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(news.url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            coil.compose.AsyncImage(
                model = if (news.image.isBlank()) "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80" else news.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

data class NewsItem(
    val source: String,
    val title: String,
    val url: String,
    val imageUrl: String
)

@Composable
fun MarketNewsSection(
    heading: String,
    headingColor: Color,
    items: List<NewsItem>,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            heading,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = headingColor
        )
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { news ->
                    Box(modifier = Modifier.weight(1f)) {
                        StaticNewsCard(news.source, news.title, news.url, news.imageUrl, uriHandler)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StaticNewsCard(source: String, title: String, url: String, imageUrl: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = source,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF00E471) // Green accent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
