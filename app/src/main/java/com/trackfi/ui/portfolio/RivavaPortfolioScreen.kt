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
import com.trackfi.ui.components.PortfolioStockCard
import com.trackfi.ui.components.SectionHeader
import com.trackfi.ui.theme.PremiumGradientStart
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

val portfolioItems = listOf(
    PortfolioItem("NSE", "IREDA", "Indian Renewable Energy Development Agency", "Loading..."),
    PortfolioItem("NSE", "TATAMOTORS", "Tata Motors (PV/CV)", "₹335.35"),
    PortfolioItem("Nasdaq 100", "RTX", "RTX Corporation", "$207.00"),
    PortfolioItem("Nasdaq 100", "WMT", "Walmart Inc.", "$125.12")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val iredaPrice = viewModel.iredaPrice.collectAsState(initial = 0.0).value
    val iredaPreviousClose = viewModel.iredaPreviousClose.collectAsState(initial = 0.0).value
    val isLoading = viewModel.isLoading.collectAsState(initial = true).value
    val isError = viewModel.isError.collectAsState(initial = false).value
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "RIVAVA+",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Text(
                    text = "Rivava Portfolio",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Strictly For Educational Purposes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = com.trackfi.ui.theme.LightPink
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(portfolioItems) { item ->
                var displayPrice = item.marketPrice
                var isPositive = true
                var percentageChange = ""

                if (item.ticker == "IREDA") {
                    if (isLoading) {
                        displayPrice = "Loading..."
                    } else if (isError && iredaPrice == 0.0) {
                        displayPrice = "Error"
                    } else {
                        displayPrice = "₹%.2f".format(iredaPrice)
                        val change = iredaPrice - iredaPreviousClose
                        val changePercent = if (iredaPreviousClose > 0) (change / iredaPreviousClose) * 100 else 0.0
                        isPositive = change >= 0
                        percentageChange = "${if (isPositive) "+" else ""}${String.format("%.2f", changePercent)}%"
                    }
                }

                PortfolioStockCard(
                    exchange = item.exchange,
                    ticker = item.ticker,
                    companyName = item.companyName,
                    marketPrice = displayPrice,
                    isPositive = isPositive,
                    percentageChange = percentageChange,
                    isPremium = true,
                    modifier = Modifier.clickable { onNavigateToDetail(item.ticker) }
                )
            }
        }
    }
}
