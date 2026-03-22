package com.trackfi.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.delay
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    onBack: () -> Unit
) {
    // Mock Data based on ticker
    val exchange = if (ticker == "RTX" || ticker == "WMT") "Nasdaq 100" else "NSE"

    // States that will simulate real-time updates
    var lastPrice by remember { mutableStateOf(if (ticker == "RTX") 205.00 else 150.00) }
    var changeValue by remember { mutableStateOf(if (ticker == "RTX") 1.96 else -1.20) }
    var pnl by remember { mutableStateOf(if (ticker == "RTX") 5.86 else -2.30) }
    var pnlPercent by remember { mutableStateOf(if (ticker == "RTX") 0.95 else -0.40) }

    val isPositive = changeValue >= 0

    // Simulate 10-15 second updates
    LaunchedEffect(ticker) {
        while (true) {
            delay(12000)
            val fluctuation = (Math.random() - 0.5) * 2.0 // Random value between -1.0 and 1.0
            lastPrice += fluctuation
            changeValue += fluctuation
            pnl += fluctuation * 2
            pnlPercent += fluctuation * 0.1
        }
    }

    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = if (ticker == "RTX") "RTX (NYSE)" else "HAL (NSE)",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuBVEOCkzgGbSYRa1wSlzolgJ1-WMq59HG4QaAKwVS2bZh2D5z4rAyUGY7g8AXYD4beuqhhIDrRE0PHiGbKe9lJEzGMTWZFxbA7yI5Rcn348ZifqltSAkiZtvQ-lL4AZb0_UqdxObVP57NdXE9qn2SW4R_XcHgvep4TEWwgUjq31nmyZDcI6RIjrHUxO3gkWkB0Y_sfVvZj7mMV3mN8KWI2S3afsg6f00fs9V0cVUAX5XN7yPuvRP7HA6l1jpvug2ys-TyNJcGt4An8N"),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Hero Section: Stock Identity
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT HOLDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%.2f", lastPrice),
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if(isPositive) "+" else ""}${String.format("%.2f", changeValue)} (${String.format("%.2f", pnlPercent)}%)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Statistics List Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column {
                        // Unrealized P&L
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Unrealized P&L", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("71.8%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        // Daily P&L
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily P&L", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-2.30", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        // Position
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Position", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("2.99", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        // Avg Price
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Avg Price", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("119.31", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        // Cost Basis
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Cost Basis", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("356.74", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        // Avg Volume
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Avg Volume", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("6.10M", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }



            // Market Insights Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MARKET OPEN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$193.69", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("09:30:01 EST", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("52W CHANGE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("+284.15%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Exponential Trend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            // CTA Button
            item {
                Button(
                    onClick = {
                        val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                        val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                        try { uriHandler.openUri(url) } catch (e: Exception) { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF98CBFF), contentColor = Color(0xFF003354)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("VIEW FULL STOCK PRICE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp), color = Color(0xFF001D33))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF001D33), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
