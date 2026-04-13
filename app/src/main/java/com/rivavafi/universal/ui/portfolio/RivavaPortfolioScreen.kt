package com.rivavafi.universal.ui.portfolio

import androidx.compose.runtime.Composable

@Composable
fun RivavaPortfolioScreen(
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit
) {
    DashboardScreen(onNavigateToDetail = onNavigateToDetail)
}
