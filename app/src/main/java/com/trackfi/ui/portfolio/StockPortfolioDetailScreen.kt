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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFFBEC7D4) // on-surface-variant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    onBack: () -> Unit
) {
    // Mock Data based on ticker
    val exchange = if (ticker == "RTX" || ticker == "WMT") "NYSE" else "NSE"

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
        containerColor = Color(0xFF131313) // Dark background from design
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Top Navigation Anchor
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF98CBFF)) // primary
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "$ticker ($exchange)",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF98CBFF) // primary color
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF353535)), // surface-container-highest
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Hero Price Display
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT HOLDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp),
                        color = Color(0xFFBEC7D4), // on-surface-variant
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = String.format("%.2f", lastPrice),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-2).sp,
                            fontSize = 60.sp
                        ),
                        color = Color(0xFFE2E2E2), // on-surface
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .background(
                                if (isPositive) Color(0xFF00E471).copy(alpha = 0.2f) else Color(0xFFFFB4AB).copy(alpha = 0.2f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF00E471) else Color(0xFFFFB4AB),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if(isPositive) "+" else ""}${String.format("%.2f", changeValue)} (${String.format("%.2f", pnlPercent)}%)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPositive) Color(0xFF00E471) else Color(0xFFFFB4AB)
                        )
                    }
                }
            }

            // Statistics List Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1B1B1B)) // surface-container-low
                        .padding(vertical = 8.dp)
                ) {
                    StatRow("Unrealized P&L", "71.8%", Color(0xFF00E471))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    StatRow("Daily P&L", "-2.30", Color(0xFFFFB4AB))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    StatRow("Position", "2.99", Color(0xFFE2E2E2))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    StatRow("Avg Price", "119.31", Color(0xFFE2E2E2))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    StatRow("Cost Basis", "356.74", Color(0xFFE2E2E2))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    StatRow("Avg Volume", "6.10M", Color(0xFFE2E2E2))
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
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00A3FF), Color(0xFF98CBFF)) // primary-container to primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = "View Full Stock Price",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF003354) // on-primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFF003354))
                        }
                    }
                }
            }

            // Wealth Insights Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(com.trackfi.ui.theme.LightPink.copy(alpha = 0.7f), com.trackfi.ui.theme.DeepBlueVariant)
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Premium Analysis", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Color.White))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Unlock Rivava AI price predictions for 2024", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = com.trackfi.ui.theme.LightPink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("UPGRADE NOW", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Redirect to External
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                            val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                            try { uriHandler.openUri(url) } catch (e: Exception) { }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp)
                    ) {
                        Text("VIEW FULL STOCK PRICE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    }
                }
            }

            // Cash Balances Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(
                        text = "CASH BALANCES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = Color(0xFFBEC7D4), // on-surface-variant
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1F1F1F)) // surface-container
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF353535)), // surface-container-highest
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFF98CBFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("USD Cash", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE2E2E2))
                                    Text("Available for trade", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBEC7D4))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("8.90", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE2E2E2))
                                Text("Settled", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E471))
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF353535).copy(alpha = 0.3f))
                                .padding(20.dp),
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
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFFFFAEDB), // secondary
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Total Cash", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE2E2E2))
                                    Text("Market Value", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBEC7D4))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("8.90", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE2E2E2))
                            }
                        }
                    }
                }
            }

            // Market Insights Preview Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 48.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MARKET INSIGHTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                            color = Color(0xFFBEC7D4)
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF98CBFF)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1B1B1B)) // surface-container-low
                            .padding(24.dp)
                    ) {
                        // Background icon placeholder
                        Icon(
                            Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = Color(0xFF98CBFF).copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 16.dp, y = (-16).dp)
                        )

                        Column {
                            Text(
                                text = "BULLISH TREND",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = Color(0xFF00A3FF), // primary-container
                                modifier = Modifier
                                    .background(Color(0xFF98CBFF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "$ticker maintains strong support at 115.00 level.",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE2E2E2)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quarterly results show a 12% increase in institutional holdings despite recent volatility.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFBEC7D4),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(Color(0xFF1B1B1B).copy(alpha = 0.8f)) // #1B1B1B/80
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.Gray
                    )
                    Text(
                        text = "HOME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // History
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.Gray
                    )
                    Text(
                        text = "HISTORY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Insights
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color(0xFF98CBFF).copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = "Insights",
                        tint = Color(0xFF00A3FF)
                    )
                    Text(
                        text = "INSIGHTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color(0xFF00A3FF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Settings
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.Gray
                    )
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
