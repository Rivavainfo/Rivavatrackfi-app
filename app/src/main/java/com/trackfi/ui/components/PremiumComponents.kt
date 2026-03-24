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
import androidx.compose.ui.graphics.Brush
import com.trackfi.ui.theme.EmeraldGreen
import com.trackfi.ui.theme.PremiumGradientStart
import com.trackfi.ui.theme.PremiumGradientEnd
import com.trackfi.ui.theme.VibrantRed
import com.trackfi.ui.theme.bounceClick
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
    errorMessage: String? = null,
    latestNews: FinnhubNewsResponse? = null,
    onValueClick: ((String) -> Unit)? = null
) {
    val priceColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPositive) EmeraldGreen else VibrantRed,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
    )

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val openUrl: () -> Unit = {
        val exchangeSuffix = if (exchange.equals("NSE", ignoreCase = true)) "NSE" else "NYSE"
        val url = "https://www.google.com/search?q=$ticker+stock+price+$exchangeSuffix"
        try {
            uriHandler.openUri(url)
        } catch (e: Exception) {}
    }

    val isNyse = exchange.equals("NYSE", ignoreCase = true)

    val cardModifier = if (isNyse) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF121212)) // Dark/Black background for NYSE
    } else {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .glassMorphism(cornerRadius = 20f, alpha = if (isPremium) 0.2f else 0.1f)
    }

    val exchangeBgColor = if (isNyse) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary // Gold for NYSE
    val exchangeTextColor = if (isNyse) Color.Black else MaterialTheme.colorScheme.onPrimary

    val tickerColor = if (isNyse) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
    val companyNameColor = if (isNyse) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant
    val priceTextColor = if (isNyse) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = cardModifier.bounceClick { openUrl() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPremium) 8.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exchange,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = exchangeTextColor,
                    modifier = Modifier
                        .background(
                            color = exchangeBgColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = tickerColor
                    )
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = companyNameColor
                    )
                }
            }

                if (errorMessage != null) {
                    Column(horizontalAlignment = Alignment.End) {
                    Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                    )
                }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.bounceClick {
                                if (onValueClick != null) {
                                    onValueClick("lastPrice")
                                } else {
                                    openUrl()
                                }
                        }
                        ) {
                            Text(
                                text = marketPrice,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = priceTextColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Details",
                                tint = exchangeBgColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                    }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.bounceClick {
                                if (onValueClick != null) {
                                    onValueClick("pnlPercent")
                                } else {
                                    openUrl()
                                }
                            }
                        ) {
                            Text(
                                text = percentageChange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = priceColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Details",
                                tint = priceColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (latestNews != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = companyNameColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        try { uriHandler.openUri(latestNews.url) } catch(e: Exception) {}
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = companyNameColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                    Text(
                            text = latestNews.headline,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = priceTextColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                        Text(
                            text = "${latestNews.source} • ${getRelativeTimeString(latestNews.datetime)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = companyNameColor
                        )
                    }
                }
            }
        }
    }
}
