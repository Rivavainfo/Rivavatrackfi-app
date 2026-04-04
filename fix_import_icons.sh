#!/bin/bash
for file in app/src/main/java/com/rivavafi/ui/history/TransactionDetailScreen.kt app/src/main/java/com/rivavafi/ui/portfolio/RivavaPortfolioScreen.kt app/src/main/java/com/rivavafi/ui/portfolio/StockPortfolioDetailScreen.kt app/src/main/java/com/rivavafi/ui/portfolio/components/PortfolioComponents.kt app/src/main/java/com/rivavafi/ui/profile/ProfileScreen.kt; do
    sed -i '/import androidx.compose.material.icons.Icons/a import androidx.compose.material.icons.filled.*' "$file"
done
