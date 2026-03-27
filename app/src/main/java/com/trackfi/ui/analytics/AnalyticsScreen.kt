package com.trackfi.ui.analytics

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.border
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131313).copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            coil.compose.AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBILzZlxVWZCMc98EAwl3BEa7-p13umhpLAR3__VgG8tkDN9T6JyrzdsojE82ke5Dx_JSHc9V-ON1R_qIsbTi1_xoAIebboUz9I-WPcA9NLlsntD22v50cvJohKY2fTGvwVdafd2hNiuQ6f4igDjL58278Ht0Vn0zAnjT8Udo7j3twOefnS5_9SYa3ysvGSUtDW7ZJPHVlmAMyULVqDXZH-PZtX2qKleQj7XZJfCcSfhs_hj-ExLEZVzb40eC0L7MTfkIl2OlMwlpKz",
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "RIVAVA",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = com.trackfi.ui.theme.PrimaryContainerSky,
                            letterSpacing = 2.sp
                        )
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Insights into your financial ecosystem",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (val state = uiState) {
                is AnalyticsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AnalyticsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AnalyticsUiState.Empty -> {
                    // Screen 8 Empty State Bento Layout
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        // Large Focus Card
                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).glassMorphism(cornerRadius = 24f, alpha = 0.05f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), Color.Transparent))))
                                    Box(modifier = Modifier.size(80.dp).border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(androidx.compose.material.icons.Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("No data available.", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "We haven't detected enough transaction activity to generate your intelligence reports. Start trading or fund your wallet to unlock real-time insights.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(
                                        onClick = { /* Deposit */ },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(48.dp).weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Deposit Assets", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary, letterSpacing = 1.sp)
                                        }
                                    }
                                    Button(
                                        onClick = { /* Learn More */ },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.height(48.dp).weight(1f)
                                    ) {
                                        Text("Learn More", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }

                        // Side Cards (Stacked horizontally if space permits, but vertically is safer for mobile)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).glassMorphism(cornerRadius = 16f, alpha = 0.05f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                        }
                                        Text("PROJECTED\nYIELD", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) {
                                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(50)))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Waiting for historical data...", style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).glassMorphism(cornerRadius = 16f, alpha = 0.05f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("NETWORK STATUS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(modifier = Modifier.size(56.dp).border(4.dp, MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(androidx.compose.material.icons.Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Offline Sync", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Analytics engine standby", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Bottom Action Rows
                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).glassMorphism(cornerRadius = 16f, alpha = 0.05f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(androidx.compose.material.icons.Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Predictive Models", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Unlock via Pro tier", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).glassMorphism(cornerRadius = 16f, alpha = 0.05f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("AI Curator", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Ready to initialize", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).glassMorphism(cornerRadius = 16f, alpha = 0.05f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Deep Privacy", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Encrypted processing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                is AnalyticsUiState.Success -> {
                    // Restoring Original Success Logic
                    OverviewCards(summary = state.summary)
                    Spacer(modifier = Modifier.height(24.dp))
                    SpendingChartCard(transactions = state.transactions)
                    Spacer(modifier = Modifier.height(24.dp))
                    SubscriptionTrackerCard(transactions = state.transactions)
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryBreakdown(transactions = state.transactions)
                }
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun OverviewCards(summary: FinancialSummaryState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total Balance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹${String.format(java.util.Locale.getDefault(), "%.0f", summary.totalIncome - summary.totalExpense)}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Card(
            modifier = Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Monthly Spent", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹${String.format(java.util.Locale.getDefault(), "%.0f", summary.totalExpense)}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SpendingChartCard(transactions: List<TransactionEntity>) {
    var selectedViewMode by remember { mutableStateOf("Weekly") }
    var selectedChartType by remember { mutableStateOf("Bar") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Trends",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge
                )
                // View Mode Toggle
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    listOf("Weekly", "Monthly").forEach { mode ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selectedViewMode == mode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedViewMode = mode }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode,
                                color = if (selectedViewMode == mode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                listOf("Bar", "Line").forEach { type ->
                    Text(
                        text = type,
                        modifier = Modifier
                            .clickable { selectedChartType = type }
                            .padding(start = 16.dp),
                        color = if (selectedChartType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (selectedChartType == type) FontWeight.Bold else FontWeight.Normal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedChartCard(transactions = transactions, viewMode = selectedViewMode, chartType = selectedChartType, modifier = Modifier.height(220.dp))
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
