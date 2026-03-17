package com.trackfi.ui.portfolio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.theme.EmeraldGreen
import com.trackfi.ui.theme.VibrantRed
import com.trackfi.ui.theme.glassMorphism

@Composable
fun PortfolioMetricsTable(
    ticker: String,
    exchange: String,
    change: String,
    position: String,
    avgVolume: String,
    avgPrice: String,
    lastPrice: String,
    costBasis: String,
    pnl: String,
    pnlPercent: String,
    unrealizedPnl: String,
    isPositive: Boolean
) {
    val valueColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPositive) EmeraldGreen else VibrantRed,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
    )
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val openUrl: () -> Unit = {
        val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
        val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
        try {
            uriHandler.openUri(url)
        } catch (e: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .glassMorphism(cornerRadius = 16f, alpha = 0.1f)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ticker,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "($exchange)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MetricRow("Change:", change, valueColor, openUrl)
        MetricRow("Position:", position, onClick = openUrl)
        MetricRow("Avg Volume:", avgVolume, onClick = openUrl)
        MetricRow("Avg Price:", avgPrice, onClick = openUrl)
        MetricRow("Last Price:", lastPrice, onClick = openUrl)
        MetricRow("Cost Basis:", costBasis, onClick = openUrl)
        MetricRow("P&L:", pnl, valueColor, openUrl)
        MetricRow("P&L %:", pnlPercent, valueColor, openUrl)
        MetricRow("Unrealized P&L %:", unrealizedPnl, valueColor, openUrl)
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = valueColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                if (onClick != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Details",
                        tint = valueColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .bounceClick { onClick() }
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun CashBalanceSection(usdCash: String, totalCash: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Cash Balances",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .glassMorphism(cornerRadius = 16f, alpha = 0.1f)
                .padding(16.dp)
        ) {
            BalanceRow("USD Cash", usdCash)
            BalanceRow("Total Cash", totalCash, showDivider = false)
        }
    }
}

@Composable
fun BalanceRow(label: String, value: String, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Market Value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}
