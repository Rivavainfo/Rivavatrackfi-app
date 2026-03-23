package com.trackfi.ui.analytics

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.trackfi.data.local.TransactionEntity
import com.trackfi.ui.theme.bounceClick
import com.trackfi.domain.usecase.FinancialSummaryState
import java.util.Calendar
import java.util.Locale
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // User selection for chart view
    var selectedChartView by remember { mutableStateOf("Weekly") }
    var selectedChartType by remember { mutableStateOf("Bar") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 60.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.displayLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AnalyticsUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is AnalyticsUiState.Success -> {
                // Toggles for charts
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedChartView == "Weekly",
                        onClick = { selectedChartView = "Weekly" },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = selectedChartView == "Monthly",
                        onClick = { selectedChartView = "Monthly" },
                        label = { Text("Monthly") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = selectedChartType == "Bar",
                        onClick = { selectedChartType = "Bar" },
                        label = { Text("Bar") }
                    )
                    FilterChip(
                        selected = selectedChartType == "Line",
                        onClick = { selectedChartType = "Line" },
                        label = { Text("Line") }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        AnimatedChartCard(
                            transactions = state.transactions,
                            viewMode = selectedChartView,
                            chartType = selectedChartType
                        )
                    }
                    item {
                        CategoryBreakdown(transactions = state.transactions)
                    }
                    item {
                        SubscriptionTrackerCard(transactions = state.transactions)
                    }
                }
            }
            is AnalyticsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AnimatedChartCard(transactions: List<TransactionEntity>, viewMode: String, chartType: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick { expanded = !expanded }
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "$viewMode Spending Trend",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))

            val height by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (expanded) 350.dp else 200.dp,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "chartHeight"
            )

            if (chartType == "Bar") {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(height),
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setDrawGridBackground(false)
                            animateY(1000)

                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                textColor = AndroidColor.WHITE
                            }

                            axisLeft.apply {
                                textColor = AndroidColor.WHITE
                                axisMinimum = 0f
                            }
                            axisRight.isEnabled = false
                        }
                    },
                    update = { barChart ->
                        val dataSet = generateBarData(transactions, viewMode)
                        val newData = BarData(dataSet)
                        barChart.data = newData

                        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(generateLabels(viewMode).toList())
                        barChart.invalidate()
                    }
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(height),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setDrawGridBackground(false)
                            animateX(1000)

                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                textColor = AndroidColor.WHITE
                            }

                            axisLeft.apply {
                                textColor = AndroidColor.WHITE
                                axisMinimum = 0f
                            }
                            axisRight.isEnabled = false
                        }
                    },
                    update = { lineChart ->
                        val dataSet = generateLineData(transactions, viewMode)
                        val newData = LineData(dataSet)
                        lineChart.data = newData

                        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(generateLabels(viewMode).toList())
                        lineChart.invalidate()
                    }
                )
            }
        }
    }
}

fun generateLabels(viewMode: String): Array<String> {
    return if (viewMode == "Weekly") {
        arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        arrayOf("W1", "W2", "W3", "W4", "W5")
    }
}

fun generateBarData(transactions: List<TransactionEntity>, viewMode: String): BarDataSet {
    val expenses = transactions.filter { it.type == "EXPENSE" || it.type == "BILL_PENDING" }
    val isWeekly = viewMode == "Weekly"
    val size = if (isWeekly) 7 else 5
    val totals = FloatArray(size) { 0f }

    val currentStart = Calendar.getInstance().apply {
        if (isWeekly) {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        } else {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    expenses.forEach { txn ->
        if (txn.date >= currentStart) {
            val cal = Calendar.getInstance().apply { timeInMillis = txn.date }
            val index = if (isWeekly) {
                cal.get(Calendar.DAY_OF_WEEK) - 1
            } else {
                (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7
            }
            if (index in 0 until size) {
                totals[index] += txn.amount.toFloat()
            }
        }
    }

    val entries = totals.mapIndexed { index, total ->
        BarEntry(index.toFloat(), total)
    }

    return BarDataSet(entries, "Spending").apply {
        color = AndroidColor.parseColor("#007AFF") // Primary Blue
        valueTextColor = AndroidColor.WHITE
        valueTextSize = 10f
    }
}

fun generateLineData(transactions: List<TransactionEntity>, viewMode: String): LineDataSet {
    val barData = generateBarData(transactions, viewMode)
    val entries = barData.values.map { Entry(it.x, it.y) }

    return LineDataSet(entries, "Spending").apply {
        color = AndroidColor.parseColor("#007AFF") // Primary Blue
        valueTextColor = AndroidColor.WHITE
        valueTextSize = 10f
        lineWidth = 2f
        setCircleColor(AndroidColor.parseColor("#007AFF"))
        circleRadius = 4f
        setDrawValues(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }
}


@Composable
fun SubscriptionTrackerCard(transactions: List<TransactionEntity>) {
    val subscriptions = transactions.filter { it.category == "SUBSCRIPTION" }.distinctBy { it.merchantName }

    if (subscriptions.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { expanded = !expanded }
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Active Subscriptions",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            subscriptions.forEach { sub ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔄", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = sub.merchantName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                            Text(text = sub.billingCycle ?: "Monthly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.0f", sub.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryBreakdown(transactions: List<TransactionEntity>) {
    val expenses = transactions.filter { it.type == "EXPENSE" || it.type == "BILL_PENDING" }

    // Group by category/subcategory
    val breakdown = expenses.groupBy {
        if (it.subcategory != null) it.subcategory else it.category
    }.mapValues { entry ->
        entry.value.sumOf { it.amount }
    }.toList().sortedByDescending { it.second }.take(10) // Top 10

    if (breakdown.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { expanded = !expanded }
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Breakdown by Category",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            val displayCount = if (expanded) breakdown.size else minOf(3, breakdown.size)

            androidx.compose.animation.AnimatedContent(targetState = displayCount, label = "categoryBreakdown") { count ->
                Column {
                    breakdown.take(count).forEach { (category, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category ?: "Other",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.0f", amount)}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}
