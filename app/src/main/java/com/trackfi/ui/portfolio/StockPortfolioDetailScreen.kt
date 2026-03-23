package com.trackfi.ui.portfolio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.trackfi.ui.components.SectionHeader
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioDetailScreen(
    ticker: String,
    initialFocus: String? = null,
    onBack: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val exchange = if (ticker == "RTX" || ticker == "WMT") "NYSE" else "NSE"
    val stockStates by viewModel.stockStates.collectAsState()
    val companyProfiles by viewModel.companyProfiles.collectAsState()
    val companyNews by viewModel.companyNews.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(ticker) {
        viewModel.startPolling(listOf(ticker))
    }

    val stockData = stockStates[ticker]?.data
    val profile = companyProfiles[ticker]?.profile
    val newsList = companyNews[ticker] ?: emptyList()

    // Price Logic
    val lastPrice = stockData?.c ?: 0.0
    val changeValue = stockData?.d ?: 0.0
    val pnlPercent = stockData?.dp ?: 0.0
    val isPositive = changeValue >= 0

    Scaffold(
        containerColor = Color(0xFF131313),
        topBar = {
            TopAppBar(
                title = { Text(ticker, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    Icon(Icons.Default.Star, null, modifier = Modifier.padding(end = 16.dp), tint = Color.Gray)
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.DarkGray)) {
                        Image(
                            painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuBVEOCkzgGbSYRa1wSlzolgJ1-WMq59HG4QaAKwVS2bZh2D5z4rAyUGY7g8AXYD4beuqhhIDrRE0PHiGbKe9lJEzGMTWZFxbA7yI5Rcn348ZifqltSAkiZtvQ-lL4AZb0_UqdxObVP57NdXE9qn2SW4R_XcHgvep4TEWwgUjq31nmyZDcI6RIjrHUxO3gkWkB0Y_sfVvZj7mMV3mN8KWI2S3afsg6f00fs9V0cVUAX5XN7yPuvRP7HA6l1jpvug2ys-TyNJcGt4An8N"),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Hero Header
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (profile != null) {
                        AsyncImage(
                            model = profile.logo,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(profile.name ?: ticker, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(profile.finnhubIndustry ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("CURRENT HOLDING", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 2.sp)
                    Text(
                        String.format(Locale.getDefault(), "%.2f", lastPrice),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    Surface(
                        color = (if (isPositive) Color(0xFF00E471) else Color.Red).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null,
                                tint = if (isPositive) Color(0xFF00E471) else Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                String.format(Locale.getDefault(), "%+1.2f (%+1.2f%%)", changeValue, pnlPercent),
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) Color(0xFF00E471) else Color.Red
                            )
                        }
                    }
                }
            }

            // Statistics Table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        MetricRow("Position", "2.99")
                        MetricRow("Avg Price", "119.31")
                        MetricRow("Cost Basis", "356.74")
                        MetricRow("Day High", String.format("%.2f", stockData?.h ?: 0.0))
                        MetricRow("Day Low", String.format("%.2f", stockData?.l ?: 0.0), isLast = true)
                    }
                }
            }

            // Market News
            if (newsList.isNotEmpty()) {
                item {
                    SectionHeader(title = "Company News")
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(newsList) { news ->
                            NewsCard(news = news)
                        }
                    }
                }
            }

            // CTA Button
            item {
                Button(
                    onClick = {
                        val url = "https://www.google.com/finance/quote/$ticker:$exchange"
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF98CBFF), contentColor = Color.Black)
                ) {
                    Text("VIEW FULL STOCK PRICE", fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, isLast: Boolean = false) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (!isLast) HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
    }
}