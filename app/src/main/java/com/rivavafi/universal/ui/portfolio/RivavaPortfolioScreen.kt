package com.rivavafi.universal.ui.portfolio

import androidx.compose.runtime.Composable

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

val stocksToLoad = listOf("IREDA.NS", "RTX")

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit
) {
    DashboardScreen(onNavigateToDetail = onNavigateToDetail)
}
