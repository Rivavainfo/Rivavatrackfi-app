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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OpenInNew
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🔒 Portfolio Locked", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Unlock for ₹11 or Enter Key", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { showUnlockDialog = true }) {
                    Text("Unlock Portfolio")
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
                SectionHeader(title = "Market News")
                Spacer(modifier = Modifier.height(16.dp))
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // INDIAN MARKET
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🇮🇳 INDIAN MARKET", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimarySky)
                        StaticNewsCard("Business Standard", "Latest Financial News from India", "https://www.business-standard.com/", uriHandler)
                        StaticNewsCard("The Economic Times", "Market Updates and Business News", "https://economictimes.indiatimes.com/", uriHandler)
                    }

                    // US MARKET
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🇺🇸 US MARKET", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimarySky)
                        StaticNewsCard("The Wall Street Journal", "US Markets and Global Business", "https://www.wsj.com/", uriHandler)
                        StaticNewsCard("Bloomberg", "Finance, Stock Market, and Business News", "https://www.bloomberg.com/", uriHandler)
                        StaticNewsCard("The New York Times", "Business and Economy Updates", "https://www.nytimes.com/section/business", uriHandler)
                    }

                    // INTERNATIONAL MARKET
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🌍 INTERNATIONAL MARKET", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimarySky)
                        StaticNewsCard("Financial Times", "Global Economy and Market News", "https://www.ft.com/", uriHandler)
                        StaticNewsCard("The Economist", "World News, Politics, Economics", "https://www.economist.com/", uriHandler)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(title = "My Portfolio")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val requiredStocks = listOf("IREDA.NS", "RTX")
                        requiredStocks.forEach { symbol ->
                            val normalizedSymbol = if (symbol == "IREDA") "IREDA.NS" else symbol
                            val stockState = stockStates[normalizedSymbol]
                            val quote = stockState?.data

                            val isIreda = normalizedSymbol == "IREDA.NS"
                            val ticker = if (isIreda) "IREDA" else "RTX"
                            val companyName = if (isIreda) "IREDA" else "Raytheon Technologies"
                            val exchange = if (isIreda) "NSE" else "NYSE"
                            val currency = if (isIreda) "₹" else "$"

                            val price = quote?.c ?: if (isIreda) 150.0 else 100.0
                            val previousClose = quote?.pc ?: if (isIreda) 148.0 else 99.0
                            val change = price - previousClose
                            val changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
                            val isPositive = change >= 0

                            val displayPrice = currency + String.format(Locale.getDefault(), "%.2f", price)
                            val displayAbsChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", change)}"
                            val displayPctChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", changePercent)}%"

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
                                }
                            )
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

@Composable
fun CuratedNewsCard(title: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            }
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
@Composable
fun StaticNewsCard(source: String, title: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            }
            .glassMorphism(cornerRadius = 16f, alpha = 0.15f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source,
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimarySky
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
