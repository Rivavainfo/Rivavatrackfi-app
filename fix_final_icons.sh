#!/bin/bash
# For each remaining warning, replace Icons.Default or Icons.Outlined with Icons.AutoMirrored.Filled or Icons.AutoMirrored.Outlined.
# To avoid the missing import error, we explicitly append the required imports to all files first.

find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/import androidx.compose.material.icons.Icons/import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.*\nimport androidx.compose.material.icons.automirrored.outlined.*/g' {} +

# Replace specific deprecated icons with their AutoMirrored equivalents based on the warning log
find app/src/main/java/com/rivavafi/ -type f -name "*.kt" -exec sed -i \
  -e 's/Icons.Outlined.ListAlt/Icons.AutoMirrored.Outlined.ListAlt/g' \
  -e 's/Icons.Default.OpenInNew/Icons.AutoMirrored.Filled.OpenInNew/g' \
  -e 's/Icons.Default.ArrowBack/Icons.AutoMirrored.Filled.ArrowBack/g' \
  -e 's/Icons.Default.Sort/Icons.AutoMirrored.Filled.Sort/g' \
  -e 's/Icons.Default.ReceiptLong/Icons.AutoMirrored.Filled.ReceiptLong/g' \
  -e 's/Icons.Default.Chat/Icons.AutoMirrored.Filled.Chat/g' \
  -e 's/Icons.Outlined.ArrowForward/Icons.AutoMirrored.Outlined.ArrowForward/g' \
  -e 's/Icons.Default.TrendingUp/Icons.AutoMirrored.Filled.TrendingUp/g' \
  -e 's/Icons.Default.ArrowForward/Icons.AutoMirrored.Filled.ArrowForward/g' \
  -e 's/Icons.Default.ShowChart/Icons.AutoMirrored.Filled.ShowChart/g' \
  -e 's/Icons.Default.ArrowForwardIos/Icons.AutoMirrored.Filled.ArrowForwardIos/g' \
  -e 's/Icons.Default.Article/Icons.AutoMirrored.Filled.Article/g' \
  -e 's/Icons.Default.HelpOutline/Icons.AutoMirrored.Filled.HelpOutline/g' \
  -e 's/Icons.Default.Logout/Icons.AutoMirrored.Filled.Logout/g' \
  -e 's/Icons.Outlined.TrendingDown/Icons.AutoMirrored.Outlined.TrendingDown/g' \
  -e 's/Icons.Outlined.TrendingUp/Icons.AutoMirrored.Outlined.TrendingUp/g' \
  -e 's/Icons.Outlined.HelpOutline/Icons.AutoMirrored.Outlined.HelpOutline/g' \
  -e 's/Icons.Outlined.ReceiptLong/Icons.AutoMirrored.Outlined.ReceiptLong/g' {} +

# Remove unused variable todaySpending in HomeScreen
sed -i '/var todaySpending = 0.0/d' app/src/main/java/com/rivavafi/ui/home/HomeScreen.kt
sed -i '/todaySpending += it.amount/d' app/src/main/java/com/rivavafi/ui/home/HomeScreen.kt
