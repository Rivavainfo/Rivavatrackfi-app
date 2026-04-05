package com.rivavafi.universal.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.ui.theme.CategoryVisuals
import com.rivavafi.universal.ui.theme.glassMorphism
import java.text.SimpleDateFormat
import java.util.*

import com.rivavafi.universal.domain.repository.TransactionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    // Collect specific transaction from database through ViewModel (requires a new property or flow lookup)
    val transactionFlow = remember(transactionId) { viewModel.getTransactionById(transactionId) }
    val transactionState = transactionFlow.collectAsState(initial = null)
    val transaction = transactionState.value

    val categories by viewModel.categories.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (transaction != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TransactionInfoCard(transaction!!)

                if (!transaction!!.rawMessage.isNullOrBlank()) {
                    RawMessageCard(transaction!!.rawMessage!!)
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = {
                            viewModel.deleteTransaction(transaction!!)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }

            if (showEditDialog) {
                com.rivavafi.universal.ui.history.TransactionCategoryBottomSheet(
                    transaction = transaction!!,
                    customCategories = categories,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedTx, createRule, ruleKw ->
                        viewModel.updateTransaction(updatedTx, createRule, ruleKw)
                        showEditDialog = false
                    },
                    onAddCategory = { name, type ->
                        viewModel.addCategory(name, type)
                    }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Transaction not found")
            }
        }
    }
}

@Composable
fun TransactionInfoCard(transaction: TransactionEntity) {
    val isCredit = transaction.type == "INCOME" || transaction.type == "REWARD"
    val color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val sign = if (isCredit) "+" else "-"

    val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(transaction.date))

    val categoryVisual = CategoryVisuals.getCategoryVisual(transaction.category)
    val subCategoryVisual = transaction.subcategory?.let { CategoryVisuals.getSubcategoryVisual(it) }
    val visualToUse = subCategoryVisual ?: categoryVisual

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.15f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = transaction.merchantName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(visualToUse.color.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visualToUse.icon,
                        contentDescription = null,
                        tint = visualToUse.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = visualToUse.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = visualToUse.color
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            DetailItem("Amount", "$sign₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}", valueColor = color, isBold = true)
            DetailItem("Date", dateString)
            DetailItem("Type", if (isCredit) "Income" else "Expense")

            if (transaction.bankName != null) {
                DetailItem("Bank Account", transaction.bankName)
            }
            if (transaction.availableBalance != null) {
                DetailItem("Available Balance", "₹${String.format(Locale.getDefault(), "%.2f", transaction.availableBalance)}")
            }
        }
    }
}

@Composable
fun RawMessageCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Original Message",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
            ),
            color = valueColor
        )
    }
}
