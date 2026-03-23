import re

with open('app/src/main/java/com/trackfi/ui/portfolio/StockPortfolioDetailScreen.kt', 'r') as f:
    content = f.read()

# Remove CTA button before Market Insights section to put it at the bottom.
cta_button = """            // CTA Button
            item {
                Button(
                    onClick = {
                        val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                        val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                        try { uriHandler.openUri(url) } catch (e: Exception) { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text("View Full Stock Price", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }"""

if cta_button in content:
    content = content.replace(cta_button, "")

    # Also remove cash balances as per reference images.
    content = re.sub(r'// Cash Balances Section[\s\S]*?// Market Insights Section', '// Market Insights Section', content)

    # Re-insert CTA at bottom.
    cta_bottom = """            // CTA Button
            item {
                Button(
                    onClick = {
                        val exchangePrefix = if (exchange == "NSE") "NSE" else "NYSE"
                        val url = "https://www.google.com/finance/quote/$ticker:$exchangePrefix"
                        try { uriHandler.openUri(url) } catch (e: Exception) { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF98CBFF), contentColor = Color(0xFF003354)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("VIEW FULL STOCK PRICE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp), color = Color(0xFF001D33))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF001D33), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}"""
    content = content.replace("        }\n    }\n}", cta_bottom)

with open('app/src/main/java/com/trackfi/ui/portfolio/StockPortfolioDetailScreen.kt', 'w') as f:
    f.write(content)
