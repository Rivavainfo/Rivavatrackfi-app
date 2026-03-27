package com.trackfi.ui.components

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
import com.trackfi.ui.theme.glassMorphism
import com.trackfi.ui.theme.glowEffect
import androidx.compose.ui.graphics.Brush
import com.trackfi.ui.theme.EmeraldGreen
import com.trackfi.ui.theme.PremiumGradientStart
import com.trackfi.ui.theme.PremiumGradientEnd
import com.trackfi.ui.theme.VibrantRed
import com.trackfi.ui.theme.bounceClick
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Article
import androidx.compose.foundation.clickable
import com.trackfi.domain.api.FinnhubNewsResponse
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
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PremiumGradientStart, EmeraldGreen)
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
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
    latestNews: FinnhubNewsResponse? = null,
    onValueClick: ((String) -> Unit)? = null
) {
    val isNyse = exchange.equals("NYSE", ignoreCase = true)

    val containerColor = if (isNyse) com.trackfi.ui.theme.NyseBlack else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    val glassAlpha = if (isNyse) 0.6f else 0.03f
    val glassStrokeAlpha = if (isNyse) 0.15f else 0.2f
    val glassColor = if (isNyse) Color.Black else Color.White

    val accentColor = if (isNyse) com.trackfi.ui.theme.NyseGold else MaterialTheme.colorScheme.primary
    val tickerColor = if (isNyse) Color.White else MaterialTheme.colorScheme.primary

    val priceColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPositive) com.trackfi.ui.theme.TertiaryEmerald else com.trackfi.ui.theme.VibrantRed,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
    )

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val openUrl: () -> Unit = {
        val exchangeSuffix = if (isNyse) "NYSE" else "NSE"
        val url = "https://www.google.com/search?q=$ticker+stock+price+$exchangeSuffix"
        try {
            uriHandler.openUri(url)
        } catch (e: Exception) {}
    }

    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = glassAlpha, strokeAlpha = glassStrokeAlpha, color = glassColor)
            .bounceClick {
                onValueClick?.invoke("market_price") ?: openUrl()
            },
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = tickerColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "($exchange)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .background(priceColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = percentageChange,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = priceColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "POSITION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LAST PRICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        modifier = Modifier.glowEffect(color = accentColor, radius = 10f, isSelected = true)
                    )
                }
            }

            if (latestNews != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        try { uriHandler.openUri(latestNews.url) } catch(e: Exception) {}
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Article,
                        contentDescription = "News",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = latestNews.headline,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${latestNews.source} • ${getRelativeTimeString(latestNews.datetime)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
