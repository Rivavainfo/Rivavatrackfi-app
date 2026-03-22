import re

with open('app/src/main/java/com/trackfi/ui/portfolio/StockPortfolioDetailScreen.kt', 'r') as f:
    content = f.read()

# Make sure proper icons are used
content = content.replace("Icons.Default.ArrowForwardIos", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForwardIos")
content = content.replace("Icons.Default.OpenInNew", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew")
content = content.replace("Icons.Default.TrendingUp", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.TrendingUp")
content = content.replace("Icons.Default.TrendingDown", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.TrendingDown")

with open('app/src/main/java/com/trackfi/ui/portfolio/StockPortfolioDetailScreen.kt', 'w') as f:
    f.write(content)
