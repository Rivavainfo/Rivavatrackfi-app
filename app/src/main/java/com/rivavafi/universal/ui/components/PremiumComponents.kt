package com.rivavafi.universal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rivavafi.universal.ui.theme.glassMorphism
import com.rivavafi.universal.ui.theme.glowEffect
import androidx.compose.ui.graphics.Brush
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.PremiumGradientStart
import com.rivavafi.universal.ui.theme.PremiumGradientEnd
import com.rivavafi.universal.ui.theme.VibrantRed
import com.rivavafi.universal.ui.theme.bounceClick
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.foundation.clickable
import com.rivavafi.universal.domain.api.FinnhubNewsResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.15f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    colors: List<Color> = listOf(Color(0xFF1E293B), Color(0xFF0F172A)) // Dark blue-grey gradient default
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = colors
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.9f),
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.5f, androidx.compose.ui.unit.TextUnitType.Sp)
        )
    }
}

fun getRelativeTimeString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    // Finnhub timestamps are in seconds
    val timeMillis = timestamp * 1000
    val diff = now - timeMillis

    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "Yesterday"
        else -> "$days days ago"
    }
}

@Composable
fun PortfolioStockCard(
    exchange: String,
    ticker: String,
    companyName: String,
    marketPrice: String,
    isPremium: Boolean = true,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true,
    percentageChange: String = "+2.4%",
    latestNews: FinnhubNewsResponse? = null, // kept for backward compatibility if needed elsewhere
    onValueClick: ((String) -> Unit)? = null
) {
    val isNyse = exchange.equals("NYSE", ignoreCase = true)

    val primaryColor = if (isNyse) com.rivavafi.universal.ui.theme.NyseGold else com.rivavafi.universal.ui.theme.PrimaryContainerSky
    val badgeBgColor = primaryColor.copy(alpha = 0.1f)

    val priceColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPositive) com.rivavafi.universal.ui.theme.TertiaryEmerald else com.rivavafi.universal.ui.theme.VibrantRed,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "priceColor"
    )

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val openUrl: () -> Unit = {
        val url = if (ticker.equals("IREDA", ignoreCase = true)) {
            "https://www.google.com/search?q=IREDA+share+price"
        } else {
            val exchangeSuffix = if (isNyse) "NYSE" else "NSE"
            "https://www.google.com/search?q=$ticker+stock+price+$exchangeSuffix"
        }
        try {
            uriHandler.openUri(url)
        } catch (e: Exception) {}
    }

    val arrowIcon = if (isPositive) Icons.Default.NorthEast else Icons.Default.SouthEast

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.6f, strokeAlpha = 0.0f, color = Color(0xFF1B1B1B))
            .clickable {
                onValueClick?.invoke("market_price")
            },
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(badgeBgColor)
                            .clickable { openUrl() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (ticker.length > 4) ticker.take(4).uppercase() else ticker.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }

                    Column {
                        Text(
                            text = ticker.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = arrowIcon,
                            contentDescription = null,
                            tint = priceColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = percentageChange.replace("+", "").replace("-", ""),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = priceColor
                        )
                    }
                }
            }

            androidx.compose.material3.IconButton(
                onClick = { openUrl() },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.OpenInNew,
                    contentDescription = "Search Ticker",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
