package com.trackfi.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackfi.data.local.TransactionEntity
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.theme.glassMorphism
import com.trackfi.ui.home.TransactionDetailsBottomSheet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 24.dp)
    ) {
        // TopAppBar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Rivava",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Review and curate your financial journey.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Date (Newest)" + if (sortOrder == SortOrder.DATE_DESC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.DATE_DESC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Date (Oldest)" + if (sortOrder == SortOrder.DATE_ASC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.DATE_ASC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Amount (High)" + if (sortOrder == SortOrder.AMOUNT_DESC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.AMOUNT_DESC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Amount (Low)" + if (sortOrder == SortOrder.AMOUNT_ASC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.AMOUNT_ASC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Merchant" + if (sortOrder == SortOrder.MERCHANT_ASC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.MERCHANT_ASC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Category" + if (sortOrder == SortOrder.CATEGORY_ASC) " ✓" else "") },
                        onClick = {
                            viewModel.onSortOrderChanged(SortOrder.CATEGORY_ASC)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search merchants, categories, amounts...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = MaterialTheme.colorScheme.primaryContainer) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        when (val state = uiState) {
            is TransactionsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TransactionsUiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Empty State Curator Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 64.dp, horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Icon Stack
                            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .shadow(24.dp, RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = com.trackfi.ui.theme.CategoryVisuals.getCategoryVisual("").icon, // Receipt fallback
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "We couldn't find any results for your search. Try adjusting your filters or keywords.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { viewModel.onSearchQueryChanged("") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Text("Clear Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Decorative Insights (Asymmetric Layout)
                    Spacer(modifier = Modifier.height(48.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("TOP SPEND", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lifestyle", style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold))
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("SAVINGS", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("+12.4%", style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
            is TransactionsUiState.Success -> {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TransactionList(transactions = state.transactions, viewModel = viewModel)
                }
            }
            is TransactionsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionList(transactions: List<TransactionEntity>, viewModel: TransactionsViewModel) {
    val showDetails by viewModel.showSmsDetails.collectAsState()
    var selectedTransactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToView by remember { mutableStateOf<TransactionEntity?>(null) }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            val swipeableState = rememberSwipeToDismissBoxState(
                confirmValueChange = { state ->
                    if (state == SwipeToDismissBoxValue.StartToEnd) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTransactionToEdit = transaction
                        false // don't dismiss
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = swipeableState,
                backgroundContent = {
                    val color = if (swipeableState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (swipeableState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                content = {
                    TransactionItem(
                        transaction = transaction,
                        showDetails = showDetails,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            transactionToView = transaction
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transactionToDelete = transaction
                        }
                    )
                }
            )
        }
    }

    val categories by viewModel.categories.collectAsState()

    selectedTransactionToEdit?.let { transaction ->
        TransactionCategoryBottomSheet(
            transaction = transaction,
            customCategories = categories,
            onDismiss = { selectedTransactionToEdit = null },
            onSave = { updatedTransaction, createRule, ruleKeyword ->
                viewModel.updateTransaction(updatedTransaction, createRule, ruleKeyword)
                selectedTransactionToEdit = null
            },
            onAddCategory = { name, type ->
                viewModel.addCategory(name, type)
            }
        )
    }

    transactionToView?.let { transaction ->
        TransactionDetailsBottomSheet(
            transaction = transaction,
            onDismiss = { transactionToView = null },
            onEdit = {
                transactionToView = null
                selectedTransactionToEdit = transaction
            },
            onDelete = {
                transactionToView = null
                transactionToDelete = transaction
            }
        )
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(transaction: TransactionEntity, showDetails: Boolean = true, onClick: () -> Unit, onLongClick: () -> Unit) {
    val isCredit = transaction.type == "INCOME" || transaction.type == "REWARD"

    val catVisual = if (transaction.subcategory != null)
        com.trackfi.ui.theme.CategoryVisuals.getSubcategoryVisual(transaction.subcategory)
    else
        com.trackfi.ui.theme.CategoryVisuals.getCategoryVisual(transaction.category)

    val iconColor = catVisual.color

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.15f)
            .bounceClick { onClick() }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catVisual.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (showDetails) transaction.merchantName else "Hidden",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val dateString = if (showDetails) formatter.format(Date(transaction.date)) else "****"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (showDetails) catVisual.title else "Hidden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = iconColor,
                        modifier = Modifier
                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.getDefault(), "%.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}
