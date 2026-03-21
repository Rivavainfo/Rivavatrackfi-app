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
import com.trackfi.ui.components.SectionHeader
import com.trackfi.ui.theme.PremiumGradientStart
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings

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
    PortfolioItem("Nasdaq 100", "RTX", "RTX Corporation", "$207.00"),
    PortfolioItem("Nasdaq 100", "WMT", "Walmart Inc.", "$125.12")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(onNavigateToDetail: (String) -> Unit) {
    Scaffold(
        containerColor = Color(0xFF131313) // Dark background from design
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Anchor
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "WealthCurator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF98CBFF) // primary color from design
                            )
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF98CBFF)) // primary color
                    }
                }
            }

            // Hero Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "TOTAL NET WORTH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color(0xFFBEC7D4), // on-surface-variant
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$142,850",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-2).sp
                                    ),
                                    color = Color(0xFFE2E2E2) // on-surface
                                )
                                Text(
                                    text = ".42",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color(0xFF98CBFF), // primary
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF00E471).copy(alpha = 0.1f), RoundedCornerShape(50)) // tertiary/10
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00E471), modifier = Modifier.size(16.dp)) // tertiary
                            Text(
                                text = "+12.4%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00E471) // tertiary
                            )
                        }
                    }
                }
            }

            // Market Overview Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Market Overview",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE2E2E2) // on-surface
                    )
                    Row(
                        modifier = Modifier.clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "See all analytics",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF98CBFF) // primary
                        )
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFF98CBFF),
                            modifier = Modifier.size(12.dp).padding(start = 4.dp)
                        )
                    }
                }
            }

            // NSE Market Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF98CBFF).copy(alpha = 0.3f), Color.Transparent)
                            )
                        ))
                        Text(
                            text = "NSE MARKET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFF98CBFF),
                            modifier = Modifier
                                .background(Color(0xFF98CBFF).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF98CBFF).copy(alpha = 0.3f), Color.Transparent)
                            )
                        ))
                    }

                    val nseItems = portfolioItems.filter { it.exchange == "NSE" }
                    nseItems.forEach { item ->
                        PortfolioStockCard(
                            exchange = item.exchange,
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = item.marketPrice,
                            isPremium = false,
                            modifier = Modifier.padding(bottom = 16.dp).clickable { onNavigateToDetail(item.ticker) }
                        )
                    }
                }
            }

            // NYSE Market Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)
                            )
                        ))
                        Text(
                            text = "NYSE MARKET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFFFFD700),
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)
                            )
                        ))
                    }

                    val nyseItems = portfolioItems.filter { it.exchange != "NSE" }
                    nyseItems.forEach { item ->
                        // Nyse items should use custom style
                        NysePortfolioStockCard(
                            exchange = "NYSE", // Override Nasdaq to NYSE
                            ticker = item.ticker,
                            companyName = item.companyName,
                            marketPrice = item.marketPrice,
                            modifier = Modifier.padding(bottom = 16.dp).clickable { onNavigateToDetail(item.ticker) }
                        )
                    }
                }
            }

            // Featured Analysis / "Master Your Assets" section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF1B1B1B), Color(0xFF0E0E0E))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Text(
                            text = "Master Your Assets",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFAEDB), // secondary
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Curate your financial legacy with real-time insights and institutional-grade portfolio management tools.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFBEC7D4), // on-surface-variant
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                        Button(
                            onClick = { },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF98CBFF), Color(0xFF00A3FF))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Upgrade Pro",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF003354) // on-primary
                                )
                            }
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
                    modifier = Modifier
                        .background(Color(0xFF98CBFF).copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF00A3FF)
                    )
                    Text(
                        text = "HOME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color(0xFF00A3FF),
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = "Insights",
                        tint = Color.Gray
                    )
                    Text(
                        text = "INSIGHTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = Color.Gray,
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

@Composable
fun NysePortfolioStockCard(
    exchange: String,
    ticker: String,
    companyName: String,
    marketPrice: String,
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
                        color = Color(0xFFBEC7D4)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE2E2E2)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowDropUp,
                        contentDescription = null,
                        tint = Color(0xFF00E471),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+2.4%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00E471)
                    )
                }
            }
        }
    }
}
