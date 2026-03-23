package com.trackfi.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.trackfi.data.local.TransactionEntity
import com.trackfi.ui.add.AddTransactionBottomSheet
import com.trackfi.ui.portfolio.PasswordDialog
import com.trackfi.ui.theme.CategoryVisuals
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.theme.glassMorphism
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTransactionDetail: (Long) -> Unit = {}
) {
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val showDetails by viewModel.showSmsDetails.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showVideoCallDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TopAppBar
            Surface(
                color = Color(0xFF131313),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBBjANjvVTJAwCuIEzqpOGqmarYPRvZnQVkQJx54L43CqAUi_pc4O7sZDw5K8TGWaP5nI0kTE-mwudhufM9oyOzq6elZlmiD9M_p_5NcUsiLbuYSlrGUtYfQMmEX4uhpJ6TjZ0AMeTLoB2Q6yFXMBCnKAlFU8ZV8HAQ17koJO4T757BC7tfQKeunLSHhoWynDL9ar6xFSX3l5Ajo8KvoN0QgWwTG98l4l0AgFZWF_RjkO2weIX9mBbGBSVMee-b2n9sszX-mgaeCt7u",
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "Rivava",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38bdf8)
                        )
                    )
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Premium Portfolio Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(colors = listOf(Color(0xFF2A2A2A), Color(0xFF1B1B1B))))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .bounceClick { if (!isPremiumUser) showPasswordDialog = true }
                    ) {
                        Column(modifier = Modifier.padding(32.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "PREMIUM TIER",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Rivava Premium Portfolio",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                if (!isPremiumUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(48.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = { if (!isPremiumUser) showPasswordDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(if (isPremiumUser) "Manage" else "Unlock Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Budget Grid
                item {
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val spentToday = transactions.filter { (it.type == "EXPENSE" || it.type == "BILL_PENDING") && it.date >= todayStart }.sumOf { it.amount }
                    val remaining = (dailyBudget - spentToday).coerceAtLeast(0.0)
                    val progress = if (dailyBudget > 0) (spentToday / dailyBudget).toFloat().coerceIn(0f, 1f) else 1f

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BudgetMiniCard("DAILY BUDGET", "₹${String.format("%.0f", dailyBudget)}", progress, "Remaining: ₹${String.format("%.0f", remaining)}", Modifier.weight(1f))
                        BudgetMiniCard("TODAY SPENDING", "₹${String.format("%.0f", spentToday)}", null, SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date()), Modifier.weight(1f))
                    }
                }

                // Recent Insights Header
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Column {
                            Text(text = "Recent Insights", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Your financial health at a glance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = "View All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                    }
                }

                // Transaction List
                if (transactions.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(items = transactions.take(5), key = { it.id }) { transaction ->
                        AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                            TransactionItem(transaction, showDetails = showDetails, onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToTransactionDetail(transaction.id)
                            })
                        }
                    }
                }

                // Support Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Support & Advice", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        SupportRow("Chat with Us", "Instant response", Icons.Default.Chat, MaterialTheme.colorScheme.primary) { showChatDialog = true }
                        SupportRow("Schedule Call", "15-min voice session", Icons.Default.Call, MaterialTheme.colorScheme.tertiary) { Toast.makeText(context, "Redirecting...", Toast.LENGTH_SHORT).show() }
                        SupportRow("Schedule Video Call", "Deep dive session", Icons.Default.VideoCall, MaterialTheme.colorScheme.secondary) { showVideoCallDialog = true }
                    }
                }
            }
        }

        // Dialogs
        if (showVideoCallDialog) {
            ScheduleDialog(onDismiss = { showVideoCallDialog = false }) { Toast.makeText(context, "Requested!", Toast.LENGTH_SHORT).show() }
        }

        if (showChatDialog) {
            ChatDialog(onDismiss = { showChatDialog = false }) { Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show() }
        }

        if (showPasswordDialog) {
            PasswordDialog(
                onDismiss = { showPasswordDialog = false },
                onUnlock = { password ->
                    if (password.trim() == userName?.trim()) {
                        viewModel.setPremiumUser(true)
                        showPasswordDialog = false
                    } else {
                        Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetMiniCard(label: String, amount: String, progress: Float?, subtext: String, modifier: Modifier) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (progress != null) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().clip(CircleShape))
            }
            Text(subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SupportRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun EmptyState() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No transactions yet", fontWeight = FontWeight.Bold)
        Text("Tap + to get started", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity, showDetails: Boolean, onClick: () -> Unit) {
    val isCredit = transaction.type == "INCOME" || transaction.type == "REWARD"
    val visual = CategoryVisuals.getCategoryVisual(transaction.category)

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(visual.icon, contentDescription = null, tint = visual.color, modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(8.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(if (showDetails) transaction.merchantName else "Hidden", fontWeight = FontWeight.Bold)
            Text(if (showDetails) SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(transaction.date)) else "****", style = MaterialTheme.typography.bodySmall)
        }
        Text("${if (isCredit) "+" else "-"}₹${transaction.amount.toInt()}", fontWeight = FontWeight.Bold, color = if (isCredit) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ScheduleDialog(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule") },
        text = { Text("Request a consultation session.") },
        confirmButton = { TextButton(onClick = { onSubmit(); onDismiss() }) { Text("Submit") } }
    )
}

@Composable
fun ChatDialog(onDismiss: () -> Unit, onSend: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat") },
        text = { Text("Send a message to support.") },
        confirmButton = { TextButton(onClick = { onSend(); onDismiss() }) { Text("Send") } }
    )
}