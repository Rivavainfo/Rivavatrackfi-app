import re

with open('app/src/main/java/com/trackfi/ui/portfolio/RivavaPortfolioScreen.kt', 'r') as f:
    content = f.read()

# Fix NysePortfolioStockCard
nyse_card = """            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = percentageChange,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Details",
                        tint = (if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error).copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }"""

nyse_card_fixed = """            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = marketPrice,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = percentageChange,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Details",
                        tint = (if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error).copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }"""

content = content.replace(nyse_card, nyse_card_fixed)

with open('app/src/main/java/com/trackfi/ui/portfolio/RivavaPortfolioScreen.kt', 'w') as f:
    f.write(content)
