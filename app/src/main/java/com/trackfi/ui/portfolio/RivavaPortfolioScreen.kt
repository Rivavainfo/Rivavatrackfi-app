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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.trackfi.ui.components.PortfolioStockCard
import com.trackfi.ui.theme.PremiumGradientStart
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: Double,
    val prefix: String = "",
    val purchasePrice: String = "—",
    val date: String = "—",
    val change: Double = 2.4
)

val initialPortfolioItems = listOf(
    PortfolioItem("NSE", "HAL", "Hindustan Aeronautics Ltd", 3995.00, "₹"),
    PortfolioItem("NSE", "TATAMOTORS", "Tata Motors (PV/CV)", 335.35, "₹"),
    PortfolioItem("Nasdaq 100", "RTX", "RTX Corporation", 207.00, "$"),
    PortfolioItem("Nasdaq 100", "WMT", "Walmart Inc.", 125.12, "$")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(onNavigateToDetail: (String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var portfolioItems by remember { mutableStateOf(initialPortfolioItems) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            portfolioItems = portfolioItems.map { item ->
                val fluctuation = (Math.random() - 0.5) * (if (item.marketPrice > 1000) 10.0 else 2.0)
                item.copy(
                    marketPrice = item.marketPrice + fluctuation,
                    change = item.change + (fluctuation * 0.1)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------- TOP BAR ----------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF353535)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Rivava",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF98CBFF)
                            )
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF98CBFF)
                        )
                    }
                }
            }

            // ---------------- HERO ----------------
            item {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {

                    Text(
                        "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFBEC7D4)
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$142,850",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFFE2E2E2)
                        )
                        Text(
                            ".42",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF98CBFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .background(
                                Color(0xFF00E471).copy(alpha = 0.1f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            null,
                            tint = Color(0xFF00E471),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+12.4%", color = Color(0xFF00E471))
                    }
                }
            }

            // ---------------- HEADER ----------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Market Overview",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "See all",
                            color = Color(0xFF98CBFF)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            null,
                            tint = Color(0xFF98CBFF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // ---------------- NSE ----------------
            item {
                Column {
                    Text(
                        "NSE MARKET",
                        color = Color(0xFF98CBFF),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val nseItems = portfolioItems.filter { it.exchange == "NSE" }

                    nseItems.forEach { item ->
                        PortfolioStockCard(
                            exchange = item.exchange,
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = "${item.prefix}${String.format(java.util.Locale.getDefault(), "%.2f", item.marketPrice)}",
                            isPremium = false,
                            isPositive = item.change >= 0,
                            percentageChange = "${if(item.change >= 0) "+" else ""}${String.format(java.util.Locale.getDefault(), "%.1f", item.change)}%",
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .clickable {
                                    onNavigateToDetail(item.ticker)
                                    val url = "https://www.google.com/finance/quote/${item.ticker}:NSE"
                                    try { uriHandler.openUri(url) } catch (e: Exception) { }
                                }
                        )
                    }
                }
            }

            // ---------------- NYSE ----------------
            item {
                Column {
                    Text(
                        "NYSE MARKET",
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val nyseItems = portfolioItems.filter { it.exchange != "NSE" }

                    nyseItems.forEach { item ->
                        NysePortfolioStockCard(
                            exchange = "NYSE",
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = "${item.prefix}${String.format(java.util.Locale.getDefault(), "%.2f", item.marketPrice)}",
                            isPositive = item.change >= 0,
                            percentageChange = "${if(item.change >= 0) "+" else ""}${String.format(java.util.Locale.getDefault(), "%.1f", item.change)}%",
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .clickable {
                                    onNavigateToDetail(item.ticker)
                                    val url = "https://www.google.com/finance/quote/${item.ticker}:NYSE"
                                    try { uriHandler.openUri(url) } catch (e: Exception) { }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NysePortfolioStockCard(
    exchange: String,
    ticker: String,
    companyName: String,
    marketPrice: String,
    isPositive: Boolean = true,
    percentageChange: String = "+2.4%",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exchange,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = percentageChange,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Details",
                        tint = (if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error).copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
