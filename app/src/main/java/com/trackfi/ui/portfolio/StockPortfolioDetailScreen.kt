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
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                    Row {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Hero Section: Stock Identity
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "TECHNOLOGY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = exchange.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                modifier = Modifier
                                    .background(com.trackfi.ui.theme.EmeraldGreen.copy(alpha = 0.1f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                color = com.trackfi.ui.theme.EmeraldGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ticker,
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
                        )
                        Text(
                            text = if (ticker == "RTX") "Raytheon Tech" else "Company Corp",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format("%.2f", lastPrice)}",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (isPositive) com.trackfi.ui.theme.EmeraldGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if(isPositive) "+" else ""}${String.format("%.2f", pnlPercent)}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isPositive) com.trackfi.ui.theme.EmeraldGreen else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Intraday Live Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .glassMorphism(cornerRadius = 24f, alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Market Performance", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Real-time intraday tracking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "1D",
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "1W",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "1M",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Abstract Line Chart Representation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            // Abstract graph visually replacing SVG
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, height * 0.8f)
                                    quadraticBezierTo(width * 0.1f, height * 0.6f, width * 0.2f, height * 0.7f)
                                    quadraticBezierTo(width * 0.4f, height * 0.4f, width * 0.6f, height * 0.5f)
                                    quadraticBezierTo(width * 0.8f, height * 0.1f, width, height * 0.2f)
                                }

                                drawPath(
                                    path = path,
                                    color = com.trackfi.ui.theme.DeepBlueVariant,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                                )

                                drawCircle(
                                    color = com.trackfi.ui.theme.DeepBlueVariant,
                                    radius = 6.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(width, height * 0.2f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("09:30 AM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Text("12:00 PM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Text("02:30 PM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Text("04:00 PM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            // Bento Grid: Full Metrics
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Metric Card 1
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SESSION HIGH", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("$${String.format("%.2f", lastPrice + 8.75)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))) {
                                Box(modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight().background(com.trackfi.ui.theme.EmeraldGreen, RoundedCornerShape(50)))
                            }
                        }
                    }
                    // Metric Card 2
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SESSION LOW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("$${String.format("%.2f", lastPrice - 17.40)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))) {
                                Box(modifier = Modifier.fillMaxWidth(0.2f).fillMaxHeight().background(MaterialTheme.colorScheme.error, RoundedCornerShape(50)))
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Metric Card 3
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("MARKET OPEN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("$${String.format("%.2f", lastPrice - 12.15)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("09:30:01 EST", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // Metric Card 4
                    Card(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("52W CHANGE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("+284.15%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = com.trackfi.ui.theme.EmeraldGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Exponential Trend", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        }
    }
}
